package ph.edu.asiatech.goananas.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ph.edu.asiatech.goananas.GoAnanasApp
import ph.edu.asiatech.goananas.data.AccountEntity
import ph.edu.asiatech.goananas.data.AccountStatus
import ph.edu.asiatech.goananas.data.Msg
import ph.edu.asiatech.goananas.data.NotificationType
import ph.edu.asiatech.goananas.data.Role
import ph.edu.asiatech.goananas.data.Thumbs
import java.io.File
import java.util.concurrent.TimeUnit

/** uploads the scans saved on the phone as soon as there is internet */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as GoAnanasApp
        if (!Cloud.enabled(app) || Cloud.currentUid() == null) return Result.success()
        val me = app.accounts.current() ?: return Result.success()
        if (me.status != AccountStatus.ACTIVE || me.farmCode.isBlank()) return Result.success()
        val dao = app.db.scanDao()
        return try {
            for (s in dao.unsyncedOf(me.uid)) {
                val firstUpload = s.remoteImageUrl == null
                var url = s.remoteImageUrl
                val file = File(s.imagePath)
                // full photo in Cloud Storage when it is enabled; a small copy inside the document otherwise (free plan)
                if (url == null && file.exists()) url = runCatching { Cloud.uploadScanImage(me.farmCode, s.uuid, file) }.getOrNull()
                if (url == null) url = Thumbs.fromFile(file)
                Cloud.saveScan(s.copy(farmCode = me.farmCode, userName = me.name, userRole = me.role), url)
                val latest = dao.getByUuid(s.uuid) ?: s
                dao.update(latest.copy(synced = true, remoteImageUrl = url, farmCode = me.farmCode))
                if (firstUpload && !s.pineappleClass.isHealthy && s.batchId.isBlank()) {
                    val staff = app.accounts.staffOf(me.farmCode).filter { it.uid != me.uid }
                    app.community.notify(staff, NotificationType.NEW_SCAN, s.uuid) { lang ->
                        Msg.newScan(lang, me.name, s.pineappleClass.title(lang), s.block, (s.confidence * 100).toInt())
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 6) Result.retry() else Result.failure()
        }
    }

    companion object {
        fun request(context: Context) {
            if (!Cloud.enabled(context)) return
            val req = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork("sync_scans", ExistingWorkPolicy.APPEND_OR_REPLACE, req)
        }
    }
}

/** checks the user's inbox every 15 minutes, so alerts arrive even when the app is closed (free Firebase plan) */
class InboxWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as GoAnanasApp
        if (!Cloud.enabled(app) || Cloud.currentUid() == null) return Result.success()
        val me = app.accounts.current() ?: return Result.success()
        return try {
            val since = app.settings.current().lastInboxCheck
            val now = System.currentTimeMillis()
            app.community.mergeInbox(Cloud.inboxSince(me.uid, since - 60_000L))
            app.settings.setLastInboxCheck(now)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context) {
            if (!Cloud.enabled(context)) return
            val req = PeriodicWorkRequestBuilder<InboxWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("inbox_check", ExistingPeriodicWorkPolicy.KEEP, req)
        }
    }
}

/**
 * real-time listeners while the app is running: the records, users, announcements, and notifications
 * that the signed-in role is allowed to see are copied into the phone database as soon as they change.
 */
class LiveSync(private val app: GoAnanasApp) {
    private val regs = mutableListOf<ListenerRegistration>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var key = ""

    fun update(me: AccountEntity?) {
        if (!Cloud.enabled(app)) return
        val newKey = if (me == null) "" else "${me.uid}|${me.role}|${me.farmCode}|${me.status}"
        if (newKey == key) return
        stop()
        key = newKey
        if (me == null || Cloud.currentUid() == null) return

        regs += Cloud.listenUser(me.uid) { a -> scope.launch { app.accounts.cacheAccounts(listOf(a)) } }
        regs += Cloud.listenInbox(me.uid) { list -> scope.launch { app.community.mergeInbox(list) } }
        if (me.status != AccountStatus.ACTIVE) return

        when (Role.of(me.role)) {
            Role.ADMIN -> {
                regs += Cloud.listenAllScans { list -> scope.launch { app.scans.mergeRemote(list) } }
                regs += Cloud.listenAllUsers { list -> scope.launch { app.accounts.cacheAccounts(list) } }
                regs += Cloud.listenDataset { list -> scope.launch { app.community.mergeDataset(list) } }
            }
            Role.OWNER, Role.TECHNICIAN -> {
                regs += Cloud.listenFarmScans(me.farmCode) { list ->
                    scope.launch {
                        val fresh = app.scans.mergeRemote(list)
                        val recent = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)
                        fresh.filter { !it.pineappleClass.isHealthy && it.createdAt > recent }.forEach { app.scans.checkOutbreak(me, it) }
                    }
                }
                regs += Cloud.listenFarmUsers(me.farmCode) { list -> scope.launch { app.accounts.cacheAccounts(list) } }
                if (me.role == Role.TECHNICIAN.key) regs += Cloud.listenDataset { list -> scope.launch { app.community.mergeDataset(list) } }
            }
            else -> regs += Cloud.listenMyScans(me.farmCode, me.uid) { list -> scope.launch { app.scans.mergeRemote(list) } }
        }
        if (me.farmCode.isNotBlank()) {
            regs += Cloud.listenAnnouncements(me.farmCode) { list -> scope.launch { app.community.mergeAnnouncements(list) } }
        }
        SyncWorker.request(app)
    }

    fun stop() {
        regs.forEach { it.remove() }
        regs.clear()
        key = ""
    }
}

package ph.edu.asiatech.goananas.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import ph.edu.asiatech.goananas.GoAnanasApp
import ph.edu.asiatech.goananas.MainActivity
import ph.edu.asiatech.goananas.R
import ph.edu.asiatech.goananas.data.AccountEntity
import ph.edu.asiatech.goananas.data.Msg
import ph.edu.asiatech.goananas.data.NotificationType
import ph.edu.asiatech.goananas.data.ScanStatus
import java.util.concurrent.TimeUnit

/** notification channels and the phone notifications of the app */
object Notifier {
    const val CH_RECHECK = "recheck"
    const val CH_INSPECTION = "inspection"
    const val CH_ALERTS = "alerts"
    const val EXTRA_OPEN_ALERTS = "open_alerts"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return   // channels exist only on Android 8.0 and later
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannel(NotificationChannel(CH_RECHECK, "Re-check reminders", NotificationManager.IMPORTANCE_DEFAULT))
        nm.createNotificationChannel(NotificationChannel(CH_INSPECTION, "Monthly inspection", NotificationManager.IMPORTANCE_LOW))
        nm.createNotificationChannel(NotificationChannel(CH_ALERTS, "Farm alerts and messages", NotificationManager.IMPORTANCE_HIGH))
    }

    private fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun channelFor(type: String) = when (type) {
        NotificationType.RECHECK -> CH_RECHECK
        NotificationType.MONTHLY -> CH_INSPECTION
        else -> CH_ALERTS
    }

    fun post(context: Context, type: String, id: Int, title: String, body: String) {
        if (!canPost(context)) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_ALERTS, true)
        }
        val pi = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(context, channelFor(type))
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF1E5631.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        try { NotificationManagerCompat.from(context).notify(id, n) } catch (_: SecurityException) { }
    }
}

/** background reminders with WorkManager; they survive app restarts and phone reboots */
object ReminderScheduler {
    private fun recheckName(id: Long) = "recheck_$id"

    fun scheduleRecheck(context: Context, scanId: Long, uuid: String, days: Long) {
        val req = OneTimeWorkRequestBuilder<RecheckWorker>()
            .setInitialDelay(days, TimeUnit.DAYS)
            .setInputData(workDataOf("scan_id" to scanId, "uuid" to uuid))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(recheckName(scanId), ExistingWorkPolicy.REPLACE, req)
    }

    fun cancelRecheck(context: Context, scanId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(recheckName(scanId))
    }

    fun setMonthly(context: Context, on: Boolean) {
        val wm = WorkManager.getInstance(context)
        if (!on) { wm.cancelUniqueWork("monthly_inspection"); return }
        val req = PeriodicWorkRequestBuilder<MonthlyInspectionWorker>(30, TimeUnit.DAYS).setInitialDelay(30, TimeUnit.DAYS).build()
        wm.enqueueUniquePeriodicWork("monthly_inspection", ExistingPeriodicWorkPolicy.KEEP, req)
    }
}

class RecheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as GoAnanasApp
        val scan = app.db.scanDao().getById(inputData.getLong("scan_id", -1)) ?: return Result.success()
        if (scan.status != ScanStatus.OPEN || !app.settings.current().recheckReminders) return Result.success()
        val owner = app.db.accountDao().get(scan.userId) ?: AccountEntity(uid = scan.userId, email = "", name = scan.userName)
        app.community.notify(listOf(owner), NotificationType.RECHECK, scan.uuid) { lang ->
            Msg.recheck(lang, scan.pineappleClass.title(lang), scan.block)
        }
        return Result.success()
    }
}

class MonthlyInspectionWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as GoAnanasApp
        if (!app.settings.current().monthlyReminder) return Result.success()
        val me = app.accounts.current() ?: return Result.success()
        app.community.notify(listOf(me), NotificationType.MONTHLY) { lang -> Msg.monthly(lang) }
        return Result.success()
    }
}

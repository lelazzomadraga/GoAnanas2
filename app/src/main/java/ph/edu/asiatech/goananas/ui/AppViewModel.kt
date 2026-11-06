package ph.edu.asiatech.goananas.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ph.edu.asiatech.goananas.GoAnanasApp
import ph.edu.asiatech.goananas.data.AccountEntity
import ph.edu.asiatech.goananas.data.AccountStatus
import ph.edu.asiatech.goananas.data.AnnouncementEntity
import ph.edu.asiatech.goananas.data.AppSettings
import ph.edu.asiatech.goananas.data.BatchEntity
import ph.edu.asiatech.goananas.data.DatasetEntity
import ph.edu.asiatech.goananas.data.Msg
import ph.edu.asiatech.goananas.data.NotificationEntity
import ph.edu.asiatech.goananas.data.NotificationType
import ph.edu.asiatech.goananas.data.Role
import ph.edu.asiatech.goananas.data.ScanEntity
import ph.edu.asiatech.goananas.data.UserMessage
import ph.edu.asiatech.goananas.ml.Diagnosis
import ph.edu.asiatech.goananas.notify.ReminderScheduler
import ph.edu.asiatech.goananas.sync.SyncWorker
import java.util.UUID

/** state of the photograph that is being diagnosed */
sealed interface AnalysisState {
    data object Idle : AnalysisState
    data object Working : AnalysisState
    data class Done(val photo: Bitmap, val diagnosis: Diagnosis) : AnalysisState
    /** "no_image" when the file could not be read */
    data class Failed(val reason: String) : AnalysisState
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as GoAnanasApp
    private val ctx: Context get() = getApplication()

    val settings: StateFlow<AppSettings?> = app.settings.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val me: StateFlow<AccountEntity?> = app.accounts.session.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val cloudEnabled: Boolean get() = app.accounts.cloud
    val modelInfo get() = app.classifier.info

    private fun <T> perUser(empty: T, block: (AccountEntity) -> Flow<T>): StateFlow<T> =
        app.accounts.session.flatMapLatest { a -> if (a == null) flowOf(empty) else block(a) }.stateIn(viewModelScope, SharingStarted.Eagerly, empty)

    /** scans the user can see: own scans (farmer, vendor), the whole farm (owner, technician), every farm (administrator) */
    val scans: StateFlow<List<ScanEntity>> = perUser(emptyList()) { a ->
        when (a.roleEnum) {
            Role.ADMIN -> app.scans.all()
            Role.OWNER, Role.TECHNICIAN -> app.scans.farm(a.farmCode)
            else -> app.scans.mine(a.uid)
        }
    }
    val myScans: StateFlow<List<ScanEntity>> = perUser(emptyList()) { a -> app.scans.mine(a.uid) }
    val notifications: StateFlow<List<NotificationEntity>> = perUser(emptyList()) { a -> app.community.notifications(a.uid) }
    val unread: StateFlow<Int> = perUser(0) { a -> app.community.unread(a.uid) }
    val announcements: StateFlow<List<AnnouncementEntity>> = perUser(emptyList()) { a -> app.community.announcements(a.farmCode) }
    val members: StateFlow<List<AccountEntity>> = perUser(emptyList()) { a ->
        if (a.roleEnum == Role.ADMIN) app.accounts.observeAllUsers() else app.accounts.observeFarmMembers(a.farmCode)
    }
    val dataset: StateFlow<List<DatasetEntity>> = perUser(emptyList()) { app.community.dataset() }
    val batches: StateFlow<List<BatchEntity>> = perUser(emptyList()) { a -> app.db.batchDao().observeFor(a.uid) }

    fun scan(id: Long) = app.scans.scan(id)

    // ------------------------------------------------------------------ messages and busy state
    var busy by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)

    private fun lang() = settings.value?.language?.ifBlank { "en" } ?: "en"

    private fun act(onDone: () -> Unit = {}, block: suspend () -> Unit) {
        viewModelScope.launch {
            busy = true
            try { block(); onDone() } catch (e: UserMessage) { message = e.text(lang()) } catch (e: Exception) {
                message = (if (lang() == "tl") "May problema: " else "Something went wrong: ") + (e.message ?: e.javaClass.simpleName)
            }
            busy = false
        }
    }

    // ------------------------------------------------------------------ first use
    fun setLanguage(l: String) = viewModelScope.launch {
        app.settings.setLanguage(l)
        me.value?.let { app.accounts.saveProfile(it.copy(language = l)) }
    }
    fun finishTutorial() = viewModelScope.launch { app.settings.setTutorialDone(true) }
    fun showTutorialAgain() = viewModelScope.launch { app.settings.setTutorialDone(false) }

    fun login(email: String, password: String) = act { app.accounts.login(email, password) }

    fun register(email: String, password: String, role: Role, farmCode: String, farmName: String) = act {
        val a = app.accounts.register(email, password, role, farmCode, farmName, lang())
        if (a.status == AccountStatus.PENDING) {
            val owners = app.accounts.membersOf(a.farmCode).filter { it.role == Role.OWNER.key && it.uid != a.uid }
            app.community.notify(owners, NotificationType.ACCOUNT) { l -> Msg.joinRequest(l, a.email, a.roleEnum.label(l)) }
        }
    }

    fun resetPassword(email: String) = act({ message = if (lang() == "tl") "Napadala na ang link sa iyong e-mail." else "A reset link was sent to your e-mail." }) {
        app.accounts.resetPassword(email)
    }

    fun logout() = viewModelScope.launch {
        app.liveSync.stop()
        app.accounts.logout()
        resetAnalysis()
    }

    fun saveProfile(a: AccountEntity, onDone: () -> Unit = {}) = act(onDone) {
        app.accounts.saveProfile(a)
        ReminderScheduler.setMonthly(ctx, app.settings.current().monthlyReminder)
    }

    fun setPhoto(uri: Uri) = act {
        val a = me.value ?: return@act
        app.accounts.savePhoto(a, uri)
    }

    // ------------------------------------------------------------------ scanning
    private val _analysis = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysis: StateFlow<AnalysisState> = _analysis.asStateFlow()
    var selectedPart: String = "Leaf"
    /** vendor batch the next scans belong to ("" for ordinary field scans) */
    var activeBatch by mutableStateOf("")

    fun analyze(uri: Uri) {
        _analysis.value = AnalysisState.Working
        viewModelScope.launch {
            _analysis.value = try {
                val photo = withContext(Dispatchers.IO) { loadBitmap(ctx, uri) }
                val diagnosis = withContext(Dispatchers.Default) { app.classifier.classify(photo) }
                AnalysisState.Done(photo, diagnosis)
            } catch (e: Exception) {
                AnalysisState.Failed("no_image")
            }
        }
    }

    fun resetAnalysis() { _analysis.value = AnalysisState.Idle }

    fun save(block: String, notes: String, onSaved: (Long) -> Unit) {
        val done = _analysis.value as? AnalysisState.Done ?: return
        val a = me.value ?: return
        viewModelScope.launch {
            val id = app.scans.save(a, done.photo, done.diagnosis, selectedPart, block, notes, activeBatch)
            _analysis.value = AnalysisState.Idle
            onSaved(id)
        }
    }

    fun setStatus(id: Long, status: String) = viewModelScope.launch { app.scans.setStatus(id, status) }
    fun updateNotes(id: Long, notes: String) = viewModelScope.launch { app.scans.updateNotes(id, notes) }
    fun delete(id: Long) = viewModelScope.launch { app.scans.delete(id) }

    fun review(id: Long, label: String, advice: String, onDone: () -> Unit) = act(onDone) {
        val a = me.value ?: return@act
        app.scans.review(a, id, label, advice)
    }

    fun setInDataset(id: Long, include: Boolean) = act {
        val a = me.value ?: return@act
        app.scans.setInDataset(a, id, include)
    }

    // ------------------------------------------------------------------ vendor batches
    fun startBatch(name: String) = viewModelScope.launch {
        val a = me.value ?: return@launch
        val b = BatchEntity(uuid = UUID.randomUUID().toString(), name = name.trim().ifBlank { "Batch " + dateOnly(System.currentTimeMillis()) }, ownerUid = a.uid)
        app.db.batchDao().upsert(b)
        activeBatch = b.uuid
    }
    fun endBatch() { activeBatch = "" }
    fun deleteBatch(uuid: String) = viewModelScope.launch { app.db.batchDao().delete(uuid); if (activeBatch == uuid) activeBatch = "" }

    // ------------------------------------------------------------------ team, users, announcements, notifications
    fun approve(target: AccountEntity) = act {
        app.accounts.setStatus(target, AccountStatus.ACTIVE)
        val farm = me.value?.farmName?.ifBlank { null } ?: target.farmCode
        app.community.notify(listOf(target), NotificationType.ACCOUNT) { l -> Msg.approved(l, farm) }
    }
    fun setAccountStatus(target: AccountEntity, status: String) = act { app.accounts.setStatus(target, status) }
    fun changeRole(target: AccountEntity, role: Role) = act {
        app.accounts.setRole(target, role)
        app.community.notify(listOf(target), NotificationType.ACCOUNT) { l -> Msg.roleChanged(l, role.label(l)) }
    }

    fun postAnnouncement(title: String, body: String, onDone: () -> Unit) = act(onDone) {
        val a = me.value ?: return@act
        if (title.isBlank()) throw UserMessage("Write a title first.", "Sumulat muna ng pamagat.")
        app.community.postAnnouncement(a, title, body, app.accounts.membersOf(a.farmCode))
    }

    fun markRead(n: NotificationEntity) = viewModelScope.launch { app.community.markRead(n) }
    fun markAllRead() = viewModelScope.launch { me.value?.let { app.community.markAllRead(it.uid) } }
    suspend fun scanIdByUuid(uuid: String): Long? = app.scans.byUuid(uuid)?.id

    fun setRecheck(on: Boolean) = viewModelScope.launch { app.settings.setRecheckReminders(on) }
    fun setMonthly(on: Boolean) = viewModelScope.launch { app.settings.setMonthlyReminder(on); ReminderScheduler.setMonthly(ctx, on) }
    fun setOutbreak(on: Boolean) = viewModelScope.launch { app.settings.setOutbreakAlerts(on) }
    fun syncNow() { SyncWorker.request(ctx); message = if (lang() == "tl") "Sini-sync ang mga scan…" else "Syncing your scans…" }

    companion object {
        /** decodes a photo at a manageable size and turns it upright using its EXIF orientation */
        fun loadBitmap(context: Context, uri: Uri, maxSide: Int = 1600): Bitmap {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, bounds) }
            require(bounds.outWidth > 0) { "not an image" }
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2
            val bmp = resolver.openInputStream(uri).use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: error("the image could not be read")
            val rotation = resolver.openInputStream(uri).use { stream ->
                if (stream == null) 0 else when (ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            }
            if (rotation == 0) return bmp
            val m = Matrix().apply { postRotate(rotation.toFloat()) }
            return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
        }
    }
}

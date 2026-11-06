package ph.edu.asiatech.goananas.data

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.edu.asiatech.goananas.ml.Diagnosis
import ph.edu.asiatech.goananas.notify.ReminderScheduler
import ph.edu.asiatech.goananas.sync.Cloud
import ph.edu.asiatech.goananas.sync.SyncWorker
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/** keeps the scan records and starts the reminders, alerts, reviews, and cloud sync that follow a diagnosis */
class ScanRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val settings: SettingsStore,
    private val accounts: AccountRepository,
    private val community: CommunityRepository,
) {
    private val dao get() = db.scanDao()
    private val cloud get() = Cloud.enabled(context)

    fun mine(uid: String) = dao.observeMine(uid)
    fun farm(code: String) = dao.observeFarm(code)
    fun all() = dao.observeAll()
    fun scan(id: Long) = dao.observeById(id)
    suspend fun byUuid(uuid: String) = withContext(Dispatchers.IO) { dao.getByUuid(uuid) }

    /** saves the photograph and the diagnosis; returns the new scan id */
    suspend fun save(
        me: AccountEntity, bitmap: Bitmap, diagnosis: Diagnosis, plantPart: String, block: String, notes: String, batchId: String,
    ): Long = withContext(Dispatchers.IO) {
        val uuid = UUID.randomUUID().toString()
        val dir = File(context.filesDir, "scans").apply { mkdirs() }
        val file = File(dir, "$uuid.jpg")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it) }

        val cls = diagnosis.top
        val prefs = settings.current()
        val now = System.currentTimeMillis()
        val recheckAt = if (!cls.isHealthy && cls.recheckDays > 0 && batchId.isBlank()) now + TimeUnit.DAYS.toMillis(cls.recheckDays.toLong()) else null
        val entity = ScanEntity(
            uuid = uuid, imagePath = file.absolutePath, plantPart = plantPart, block = block.trim(),
            predictedLabel = cls.label, confidence = diagnosis.confidence,
            probabilities = diagnosis.probabilities.joinToString(",") { "%.5f".format(Locale.US, it) },
            inferenceMs = diagnosis.inferenceMs, notes = notes.trim(),
            status = if (cls.isHealthy) ScanStatus.HEALTHY else ScanStatus.OPEN,
            createdAt = now, recheckAt = recheckAt,
            userId = me.uid, userName = me.name, userRole = me.role, farmCode = me.farmCode,
            reviewStatus = if (cls.isHealthy && !diagnosis.lowConfidence) ReviewStatus.NONE else ReviewStatus.PENDING,
            batchId = batchId,
        )
        val id = dao.insert(entity)

        if (recheckAt != null && prefs.recheckReminders) {
            ReminderScheduler.scheduleRecheck(context, id, uuid, cls.recheckDays.toLong())
        }
        if (!cls.isHealthy) {
            checkOutbreak(me, entity)
            if (!cloud) {   // offline mode: the owner and technician on this phone see it when they sign in
                val staff = accounts.staffOf(me.farmCode).filter { it.uid != me.uid }
                community.notify(staff, NotificationType.NEW_SCAN, uuid) { lang ->
                    Msg.newScan(lang, me.name, cls.title(lang), entity.block, (diagnosis.confidence * 100).toInt())
                }
            }
        }
        SyncWorker.request(context)
        id
    }

    /** three or more scans of the same disease in one block within 14 days */
    suspend fun checkOutbreak(me: AccountEntity, s: ScanEntity) {
        if (s.block.isBlank() || !settings.current().outbreakAlerts) return
        val since = s.createdAt - TimeUnit.DAYS.toMillis(OUTBREAK_WINDOW_DAYS)
        val n = withContext(Dispatchers.IO) { dao.countSimilar(s.predictedLabel, s.block, s.farmCode, since) }
        if (n >= OUTBREAK_THRESHOLD) {
            val cls = PineappleClass.fromLabel(s.predictedLabel)
            val day = s.createdAt / TimeUnit.DAYS.toMillis(1)
            val (title, body) = Msg.outbreak(me.language, cls.title(me.language), s.block, n)
            community.notifyOnce(me, "local:outbreak:${s.farmCode}:${s.predictedLabel}:${s.block}:$day", NotificationType.OUTBREAK, s.uuid, title, body)
        }
    }

    suspend fun setStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        val scan = dao.getById(id) ?: return@withContext
        val updated = scan.copy(status = status)
        dao.update(updated)
        if (status != ScanStatus.OPEN) ReminderScheduler.cancelRecheck(context, id)
        if (cloud && scan.synced) Cloud.updateScan(scan.farmCode, scan.uuid, mapOf("status" to status))
        Unit
    }

    suspend fun updateNotes(id: Long, notes: String) = withContext(Dispatchers.IO) {
        val scan = dao.getById(id) ?: return@withContext
        dao.update(scan.copy(notes = notes.trim()))
        if (cloud && scan.synced) Cloud.updateScan(scan.farmCode, scan.uuid, mapOf("notes" to notes.trim()))
        Unit
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        val scan = dao.getById(id) ?: return@withContext
        if (scan.imagePath.isNotBlank()) File(scan.imagePath).delete()
        ReminderScheduler.cancelRecheck(context, id)
        dao.delete(id)
        if (cloud && scan.synced) Cloud.deleteScan(scan.farmCode, scan.uuid)
        Unit
    }

    /** review by the technician or owner: confirm the result or correct it, and add advice */
    suspend fun review(reviewer: AccountEntity, id: Long, correctLabel: String, advice: String) = withContext(Dispatchers.IO) {
        val scan = dao.getById(id) ?: return@withContext
        val confirmed = correctLabel == scan.predictedLabel
        val now = System.currentTimeMillis()
        val cls = PineappleClass.fromLabel(correctLabel)
        val updated = scan.copy(
            reviewStatus = if (confirmed) ReviewStatus.CONFIRMED else ReviewStatus.CORRECTED,
            confirmedLabel = correctLabel, reviewerName = reviewer.name, advice = advice.trim(), reviewedAt = now,
            status = if (cls.isHealthy) ScanStatus.HEALTHY else scan.status.let { if (it == ScanStatus.HEALTHY) ScanStatus.OPEN else it },
        )
        dao.update(updated)
        if (cloud) Cloud.updateScan(scan.farmCode, scan.uuid, mapOf(
            "reviewStatus" to updated.reviewStatus, "confirmedLabel" to correctLabel, "finalLabel" to correctLabel,
            "isHealthy" to cls.isHealthy, "reviewerName" to reviewer.name, "advice" to updated.advice, "reviewedAt" to now, "status" to updated.status,
        ))
        if (scan.userId.isNotBlank() && scan.userId != reviewer.uid) {
            val owner = accounts.membersOf(scan.farmCode).firstOrNull { it.uid == scan.userId }
                ?: AccountEntity(uid = scan.userId, email = "", name = scan.userName)
            community.notify(listOf(owner), NotificationType.REVIEW, scan.uuid) { lang -> Msg.reviewed(lang, reviewer.name, cls.title(lang), confirmed) }
        }
        Unit
    }

    /** adds a reviewed image to the dataset used for retraining the model */
    suspend fun setInDataset(reviewer: AccountEntity, id: Long, include: Boolean) = withContext(Dispatchers.IO) {
        val scan = dao.getById(id) ?: return@withContext
        dao.update(scan.copy(inDataset = include))
        if (include) {
            val d = DatasetEntity(scanUuid = scan.uuid, imageUrl = scan.remoteImageUrl ?: "", imagePath = scan.imagePath,
                label = scan.finalLabel, verifiedBy = reviewer.name, farmCode = scan.farmCode)
            db.datasetDao().upsert(d)
            if (cloud) { Cloud.addDataset(d); Cloud.updateScan(scan.farmCode, scan.uuid, mapOf("inDataset" to true)) }
        } else {
            db.datasetDao().delete(scan.uuid)
            if (cloud) { Cloud.removeDataset(scan.uuid); Cloud.updateScan(scan.farmCode, scan.uuid, mapOf("inDataset" to false)) }
        }
    }

    /**
     * copies scans received from Firestore into the local table. Unsynced local changes are kept, and the
     * review fields from the technician are always taken. Returns the scans that were new on this phone.
     */
    suspend fun mergeRemote(list: List<ScanEntity>): List<ScanEntity> = withContext(Dispatchers.IO) {
        val fresh = mutableListOf<ScanEntity>()
        for (r in list) {
            val local = dao.getByUuid(r.uuid)
            if (local == null) {
                if (dao.insert(r) > 0) fresh += r
            } else if (!local.synced) {
                dao.update(local.copy(reviewStatus = r.reviewStatus, confirmedLabel = r.confirmedLabel, reviewerName = r.reviewerName,
                    advice = r.advice, reviewedAt = r.reviewedAt))
            } else {
                dao.update(r.copy(id = local.id, imagePath = local.imagePath))
            }
        }
        fresh
    }

    companion object {
        const val OUTBREAK_THRESHOLD = 3
        const val OUTBREAK_WINDOW_DAYS = 14L
    }
}

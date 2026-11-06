package ph.edu.asiatech.goananas.sync

import android.content.Context
import android.net.Uri
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import ph.edu.asiatech.goananas.BuildConfig
import ph.edu.asiatech.goananas.data.AccountEntity
import ph.edu.asiatech.goananas.data.AnnouncementEntity
import ph.edu.asiatech.goananas.data.DatasetEntity
import ph.edu.asiatech.goananas.data.NotificationEntity
import ph.edu.asiatech.goananas.data.PineappleClass
import ph.edu.asiatech.goananas.data.ScanEntity
import java.io.File

/**
 * Firebase part of the app. Everything here is skipped when the group has not added google-services.json,
 * and the app then works in offline mode with accounts stored on the phone.
 *
 * Cloud Firestore
 *   users/{uid}                          profile, role, farm code, status
 *   users/{uid}/inbox/{id}               notifications for that user (read by the app and the web dashboard)
 *   farms/{farmCode}                     farm name and owner
 *   farms/{farmCode}/scans/{uuid}        one document per diagnosis, with the review of the technician
 *   farms/{farmCode}/announcements/{id}  messages from the owner or technician to the farm
 *   dataset/{scanUuid}                   images confirmed by technicians for retraining the model
 * Cloud Storage
 *   farms/{farmCode}/scans/{uuid}.jpg    scan photographs
 *   avatars/{uid}.jpg                    profile pictures
 */
object Cloud {
    fun enabled(context: Context): Boolean = BuildConfig.FIREBASE_ENABLED && FirebaseApp.getApps(context).isNotEmpty()

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage get() = FirebaseStorage.getInstance()

    fun currentUid(): String? = auth.currentUser?.uid

    suspend fun register(email: String, password: String): String =
        auth.createUserWithEmailAndPassword(email.trim(), password).await().user?.uid ?: error("Registration failed")

    suspend fun signIn(email: String, password: String): String =
        auth.signInWithEmailAndPassword(email.trim(), password).await().user?.uid ?: error("Sign-in failed")

    suspend fun resetPassword(email: String) { auth.sendPasswordResetEmail(email.trim()).await() }

    fun signOut() { auth.signOut() }

    // ------------------------------------------------------------------ profiles and farms
    fun toAccount(d: DocumentSnapshot): AccountEntity = AccountEntity(
        uid = d.id,
        email = d.getString("email") ?: "",
        name = d.getString("name") ?: "",
        role = d.getString("role") ?: "farmer",
        farmCode = d.getString("farmCode") ?: "",
        farmName = d.getString("farmName") ?: "",
        gender = d.getString("gender") ?: "",
        ageRange = d.getString("ageRange") ?: "",
        yearsFarming = d.getString("yearsFarming") ?: "",
        location = d.getString("location") ?: "",
        phone = d.getString("phone") ?: "",
        language = d.getString("language") ?: "en",
        photoUrl = d.getString("photoUrl") ?: "",
        status = d.getString("status") ?: "pending",
        createdAt = d.getLong("createdAt") ?: 0L,
    )

    fun accountMap(a: AccountEntity): Map<String, Any> = mapOf(
        "email" to a.email, "name" to a.name, "role" to a.role, "farmCode" to a.farmCode, "farmName" to a.farmName,
        "gender" to a.gender, "ageRange" to a.ageRange, "yearsFarming" to a.yearsFarming, "location" to a.location,
        "phone" to a.phone, "language" to a.language, "photoUrl" to a.photoUrl, "status" to a.status, "createdAt" to a.createdAt,
    )

    suspend fun getProfile(uid: String): AccountEntity? {
        val d = db.collection("users").document(uid).get().await()
        return if (d.exists()) toAccount(d) else null
    }

    suspend fun saveProfile(a: AccountEntity) {
        db.collection("users").document(a.uid).set(accountMap(a), SetOptions.merge()).await()
    }

    /** profile fields a user may change on their own account (role and status are changed by the owner or administrator) */
    suspend fun updateOwnProfile(a: AccountEntity) {
        db.collection("users").document(a.uid).set(
            mapOf("name" to a.name, "gender" to a.gender, "ageRange" to a.ageRange, "yearsFarming" to a.yearsFarming,
                "location" to a.location, "phone" to a.phone, "language" to a.language, "photoUrl" to a.photoUrl, "lastSeen" to System.currentTimeMillis()),
            SetOptions.merge(),
        ).await()
    }

    suspend fun setUserFields(uid: String, fields: Map<String, Any>) {
        db.collection("users").document(uid).set(fields, SetOptions.merge()).await()
    }

    suspend fun farmName(code: String): String? {
        val d = db.collection("farms").document(code).get().await()
        return if (d.exists()) (d.getString("name") ?: code) else null
    }

    suspend fun createFarm(code: String, name: String, ownerUid: String, location: String) {
        db.collection("farms").document(code).set(
            mapOf("name" to name, "ownerUid" to ownerUid, "location" to location, "createdAt" to System.currentTimeMillis()),
        ).await()
    }

    suspend fun farmMembers(farm: String): List<AccountEntity> =
        db.collection("users").whereEqualTo("farmCode", farm).get().await().documents.map { toAccount(it) }

    suspend fun uploadAvatar(uid: String, uri: Uri): String {
        val ref = storage.reference.child("avatars/$uid.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    // ------------------------------------------------------------------ scans
    fun scanMap(s: ScanEntity, imageUrl: String?): Map<String, Any?> {
        val cls = s.pineappleClass
        return mapOf(
            "uuid" to s.uuid, "userId" to s.userId, "userName" to s.userName, "role" to s.userRole, "farmCode" to s.farmCode,
            "block" to s.block, "plantPart" to s.plantPart, "predictedLabel" to s.predictedLabel,
            "predictedClass" to PineappleClass.fromLabel(s.predictedLabel).displayName, "finalLabel" to s.finalLabel,
            "isHealthy" to cls.isHealthy, "confidence" to s.confidence.toDouble(),
            "probabilities" to PineappleClass.modelOrder.map { it.label }.zip(s.probabilityList.map { it.toDouble() }).toMap(),
            "recommendedAction" to cls.actionEn, "inferenceMs" to s.inferenceMs, "notes" to s.notes, "status" to s.status,
            "createdAt" to s.createdAt, "recheckAt" to s.recheckAt, "imageUrl" to imageUrl, "batchId" to s.batchId,
            "reviewStatus" to s.reviewStatus, "confirmedLabel" to s.confirmedLabel, "reviewerName" to s.reviewerName,
            "advice" to s.advice, "reviewedAt" to s.reviewedAt, "inDataset" to s.inDataset, "updatedAt" to System.currentTimeMillis(),
        )
    }

    fun toScan(d: DocumentSnapshot): ScanEntity {
        @Suppress("UNCHECKED_CAST")
        val probs = d.get("probabilities") as? Map<String, Any?>
        val probText = PineappleClass.modelOrder.joinToString(",") { c -> ((probs?.get(c.label) as? Number)?.toFloat() ?: 0f).toString() }
        return ScanEntity(
            uuid = d.id,
            remoteImageUrl = d.getString("imageUrl"),
            plantPart = d.getString("plantPart") ?: "",
            block = d.getString("block") ?: "",
            predictedLabel = d.getString("predictedLabel") ?: "healthy",
            confidence = (d.getDouble("confidence") ?: 0.0).toFloat(),
            probabilities = probText,
            inferenceMs = d.getLong("inferenceMs") ?: 0L,
            notes = d.getString("notes") ?: "",
            status = d.getString("status") ?: "Open",
            createdAt = d.getLong("createdAt") ?: 0L,
            recheckAt = d.getLong("recheckAt"),
            userId = d.getString("userId") ?: "",
            userName = d.getString("userName") ?: "",
            userRole = d.getString("role") ?: "",
            farmCode = d.getString("farmCode") ?: "",
            reviewStatus = d.getString("reviewStatus") ?: "none",
            confirmedLabel = d.getString("confirmedLabel") ?: "",
            reviewerName = d.getString("reviewerName") ?: "",
            advice = d.getString("advice") ?: "",
            reviewedAt = d.getLong("reviewedAt"),
            inDataset = d.getBoolean("inDataset") ?: false,
            batchId = d.getString("batchId") ?: "",
            synced = true,
        )
    }

    suspend fun uploadScanImage(farm: String, uuid: String, file: File): String {
        val ref = storage.reference.child("farms/$farm/scans/$uuid.jpg")
        ref.putFile(Uri.fromFile(file)).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun saveScan(s: ScanEntity, imageUrl: String?) {
        db.collection("farms").document(s.farmCode).collection("scans").document(s.uuid).set(scanMap(s, imageUrl), SetOptions.merge()).await()
    }

    /** changes made by the technician or owner; queued by Firestore when the phone is offline */
    fun updateScan(farm: String, uuid: String, fields: Map<String, Any?>) {
        db.collection("farms").document(farm).collection("scans").document(uuid)
            .set(fields + ("updatedAt" to System.currentTimeMillis()), SetOptions.merge())
    }

    fun deleteScan(farm: String, uuid: String) {
        db.collection("farms").document(farm).collection("scans").document(uuid).delete()
    }

    // ------------------------------------------------------------------ notifications, announcements, dataset
    fun sendInbox(toUids: Collection<String>, title: String, body: String, type: String, scanUuid: String = "") {
        if (toUids.isEmpty()) return
        val batch = db.batch()
        val now = System.currentTimeMillis()
        for (uid in toUids.toSet()) {
            val ref = db.collection("users").document(uid).collection("inbox").document()
            batch.set(ref, mapOf("title" to title, "body" to body, "type" to type, "scanUuid" to scanUuid, "createdAt" to now, "read" to false))
        }
        batch.commit()
    }

    fun toNotification(uid: String, d: DocumentSnapshot) = NotificationEntity(
        remoteId = d.id, toUid = uid,
        title = d.getString("title") ?: "", body = d.getString("body") ?: "",
        type = d.getString("type") ?: "", scanUuid = d.getString("scanUuid") ?: "",
        createdAt = d.getLong("createdAt") ?: 0L, read = d.getBoolean("read") ?: false,
    )

    suspend fun inboxSince(uid: String, since: Long): List<NotificationEntity> =
        db.collection("users").document(uid).collection("inbox").whereGreaterThan("createdAt", since).get().await()
            .documents.map { toNotification(uid, it) }

    fun markInboxRead(uid: String, remoteId: String) {
        db.collection("users").document(uid).collection("inbox").document(remoteId).set(mapOf("read" to true), SetOptions.merge())
    }

    fun postAnnouncement(a: AnnouncementEntity) {
        db.collection("farms").document(a.farmCode).collection("announcements").document().set(
            mapOf("title" to a.title, "body" to a.body, "fromName" to a.fromName, "fromRole" to a.fromRole, "createdAt" to a.createdAt),
        )
    }

    fun addDataset(d: DatasetEntity) {
        db.collection("dataset").document(d.scanUuid).set(
            mapOf("scanUuid" to d.scanUuid, "imageUrl" to d.imageUrl, "label" to d.label, "verifiedBy" to d.verifiedBy,
                "farmCode" to d.farmCode, "createdAt" to d.createdAt),
        )
    }

    fun removeDataset(scanUuid: String) { db.collection("dataset").document(scanUuid).delete() }

    // ------------------------------------------------------------------ live listeners
    fun listenUser(uid: String, onChange: (AccountEntity) -> Unit): ListenerRegistration =
        db.collection("users").document(uid).addSnapshotListener { d, _ -> if (d != null && d.exists()) onChange(toAccount(d)) }

    fun listenInbox(uid: String, onChange: (List<NotificationEntity>) -> Unit): ListenerRegistration =
        db.collection("users").document(uid).collection("inbox").orderBy("createdAt", Query.Direction.DESCENDING).limit(100)
            .addSnapshotListener { s, _ -> if (s != null) onChange(s.documents.map { toNotification(uid, it) }) }

    fun listenFarmScans(farm: String, onChange: (List<ScanEntity>) -> Unit): ListenerRegistration =
        db.collection("farms").document(farm).collection("scans").orderBy("createdAt", Query.Direction.DESCENDING).limit(500)
            .addSnapshotListener { s, _ -> if (s != null) onChange(s.documents.map { toScan(it) }) }

    fun listenMyScans(farm: String, uid: String, onChange: (List<ScanEntity>) -> Unit): ListenerRegistration =
        db.collection("farms").document(farm).collection("scans").whereEqualTo("userId", uid)
            .addSnapshotListener { s, _ -> if (s != null) onChange(s.documents.map { toScan(it) }) }

    fun listenAllScans(onChange: (List<ScanEntity>) -> Unit): ListenerRegistration =
        db.collectionGroup("scans").limit(1000)
            .addSnapshotListener { s, _ -> if (s != null) onChange(s.documents.map { toScan(it) }) }

    fun listenFarmUsers(farm: String, onChange: (List<AccountEntity>) -> Unit): ListenerRegistration =
        db.collection("users").whereEqualTo("farmCode", farm)
            .addSnapshotListener { s, _ -> if (s != null) onChange(s.documents.map { toAccount(it) }) }

    fun listenAllUsers(onChange: (List<AccountEntity>) -> Unit): ListenerRegistration =
        db.collection("users").addSnapshotListener { s, _ -> if (s != null) onChange(s.documents.map { toAccount(it) }) }

    fun listenAnnouncements(farm: String, onChange: (List<AnnouncementEntity>) -> Unit): ListenerRegistration =
        db.collection("farms").document(farm).collection("announcements").orderBy("createdAt", Query.Direction.DESCENDING).limit(50)
            .addSnapshotListener { s, _ ->
                if (s != null) onChange(s.documents.map { d ->
                    AnnouncementEntity(remoteId = d.id, farmCode = farm, title = d.getString("title") ?: "", body = d.getString("body") ?: "",
                        fromName = d.getString("fromName") ?: "", fromRole = d.getString("fromRole") ?: "", createdAt = d.getLong("createdAt") ?: 0L)
                })
            }

    fun listenDataset(onChange: (List<DatasetEntity>) -> Unit): ListenerRegistration =
        db.collection("dataset").addSnapshotListener { s, _ ->
            if (s != null) onChange(s.documents.map { d ->
                DatasetEntity(scanUuid = d.id, imageUrl = d.getString("imageUrl") ?: "", label = d.getString("label") ?: "",
                    verifiedBy = d.getString("verifiedBy") ?: "", farmCode = d.getString("farmCode") ?: "", createdAt = d.getLong("createdAt") ?: 0L)
            })
        }
}

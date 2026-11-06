package ph.edu.asiatech.goananas.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** status of a scanned plant after the diagnosis */
object ScanStatus {
    const val OPEN = "Open"            // diseased, waiting for action
    const val RECHECKED = "Rechecked"  // plant was checked again
    const val RESOLVED = "Resolved"    // plant treated or removed
    const val HEALTHY = "Healthy"      // no action needed
}

/** review of a scan by the agricultural technician (or farm owner) */
object ReviewStatus {
    const val NONE = "none"            // healthy scans do not need a review
    const val PENDING = "pending"
    const val CONFIRMED = "confirmed"
    const val CORRECTED = "corrected"
}

/**
 * One diagnosis with its photograph. Saved on the phone first (works without signal) and copied to
 * Cloud Firestore and Cloud Storage when the phone is online. Scans of the same farm made by other users
 * are copied back into this table, so every screen reads from one place.
 */
@Entity(tableName = "scans", indices = [Index("uuid", unique = true), Index("farmCode"), Index("userId")])
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: String,
    val imagePath: String = "",
    val remoteImageUrl: String? = null,
    val plantPart: String,
    val block: String,
    val predictedLabel: String,
    val confidence: Float,
    /** probabilities of the six conditions in PineappleClass.modelOrder, comma-separated */
    val probabilities: String,
    val inferenceMs: Long,
    val notes: String = "",
    val status: String,
    val createdAt: Long,
    val recheckAt: Long? = null,
    val userId: String = "",
    val userName: String = "",
    val userRole: String = "",
    val farmCode: String = "",
    val reviewStatus: String = ReviewStatus.NONE,
    val confirmedLabel: String = "",
    val reviewerName: String = "",
    val advice: String = "",
    val reviewedAt: Long? = null,
    val inDataset: Boolean = false,
    val batchId: String = "",
    val synced: Boolean = false,
) {
    /** the final condition: the technician's correction if there is one, otherwise the model result */
    val finalLabel: String get() = confirmedLabel.ifBlank { predictedLabel }
    val pineappleClass: PineappleClass get() = PineappleClass.fromLabel(finalLabel)
    val probabilityList: List<Float> get() = probabilities.split(",").mapNotNull { it.trim().toFloatOrNull() }
    /** local file if it is on this phone, otherwise the link to the cloud copy */
    val imageModel: Any? get() {
        if (imagePath.isNotBlank() && java.io.File(imagePath).exists()) return java.io.File(imagePath)
        val remote = remoteImageUrl ?: return null
        return if (remote.startsWith("data:")) Thumbs.decode(remote) else remote
    }
}

/** account profile; in offline mode it also holds the password hash */
@Entity(tableName = "accounts", indices = [Index("email"), Index("farmCode")])
data class AccountEntity(
    @PrimaryKey val uid: String,
    val email: String,
    val passwordHash: String = "",
    val name: String = "",
    val role: String = Role.FARMER.key,
    val farmCode: String = "",
    val farmName: String = "",
    val gender: String = "",
    val ageRange: String = "",
    val yearsFarming: String = "",
    val location: String = "",
    val phone: String = "",
    val language: String = "en",
    val photoUrl: String = "",
    val status: String = AccountStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val roleEnum: Role get() = Role.of(role)
    val profileComplete: Boolean get() = name.isNotBlank() && gender.isNotBlank() && ageRange.isNotBlank()
}

/** notification shown in the Alerts screen (and as a phone notification) */
@Entity(tableName = "notifications", indices = [Index("toUid"), Index("remoteId", unique = true)])
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String? = null,
    val toUid: String,
    val title: String,
    val body: String,
    val type: String,
    val scanUuid: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val read: Boolean = false,
)

object NotificationType {
    const val RECHECK = "recheck"
    const val MONTHLY = "monthly"
    const val OUTBREAK = "outbreak"
    const val NEW_SCAN = "new_scan"
    const val REVIEW = "review"
    const val ANNOUNCEMENT = "announcement"
    const val ACCOUNT = "account"
}

@Entity(tableName = "announcements", indices = [Index("farmCode"), Index("remoteId", unique = true)])
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String? = null,
    val farmCode: String,
    val title: String,
    val body: String,
    val fromName: String,
    val fromRole: String,
    val createdAt: Long = System.currentTimeMillis(),
)

/** image confirmed by a technician and added to the dataset used to retrain the model */
@Entity(tableName = "dataset", indices = [Index("scanUuid", unique = true)])
data class DatasetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scanUuid: String,
    val imageUrl: String = "",
    val imagePath: String = "",
    val label: String,
    val verifiedBy: String,
    val farmCode: String,
    val createdAt: Long = System.currentTimeMillis(),
)

/** a vendor's batch of fruits checked together (for example, one delivery from a supplier) */
@Entity(tableName = "batches", indices = [Index("ownerUid")])
data class BatchEntity(
    @PrimaryKey val uuid: String,
    val name: String,
    val ownerUid: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Dao
interface ScanDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(scan: ScanEntity): Long
    @Update suspend fun update(scan: ScanEntity)
    @Query("DELETE FROM scans WHERE id = :id") suspend fun delete(id: Long)
    @Query("SELECT * FROM scans ORDER BY createdAt DESC") fun observeAll(): Flow<List<ScanEntity>>
    @Query("SELECT * FROM scans WHERE userId = :uid ORDER BY createdAt DESC") fun observeMine(uid: String): Flow<List<ScanEntity>>
    @Query("SELECT * FROM scans WHERE farmCode = :farm ORDER BY createdAt DESC") fun observeFarm(farm: String): Flow<List<ScanEntity>>
    @Query("SELECT * FROM scans WHERE id = :id") fun observeById(id: Long): Flow<ScanEntity?>
    @Query("SELECT * FROM scans WHERE id = :id") suspend fun getById(id: Long): ScanEntity?
    @Query("SELECT * FROM scans WHERE uuid = :uuid LIMIT 1") suspend fun getByUuid(uuid: String): ScanEntity?
    @Query("SELECT * FROM scans WHERE synced = 0 AND userId = :uid") suspend fun unsyncedOf(uid: String): List<ScanEntity>
    @Query("SELECT COUNT(*) FROM scans WHERE predictedLabel = :label AND block = :block AND farmCode = :farm AND createdAt >= :since")
    suspend fun countSimilar(label: String, block: String, farm: String, since: Long): Int
}

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(a: AccountEntity)
    @Query("SELECT * FROM accounts WHERE uid = :uid") suspend fun get(uid: String): AccountEntity?
    @Query("SELECT * FROM accounts WHERE uid = :uid") fun observe(uid: String): Flow<AccountEntity?>
    @Query("SELECT * FROM accounts WHERE lower(email) = lower(:email) LIMIT 1") suspend fun byEmail(email: String): AccountEntity?
    @Query("SELECT * FROM accounts WHERE farmCode = :farm ORDER BY name") fun observeFarm(farm: String): Flow<List<AccountEntity>>
    @Query("SELECT * FROM accounts WHERE farmCode = :farm") suspend fun farmMembers(farm: String): List<AccountEntity>
    @Query("SELECT * FROM accounts ORDER BY createdAt DESC") fun observeAll(): Flow<List<AccountEntity>>
    @Query("SELECT COUNT(*) FROM accounts") suspend fun count(): Int
    @Query("SELECT COUNT(*) FROM accounts WHERE farmCode = :farm AND role = 'owner'") suspend fun ownersOf(farm: String): Int
}

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(n: NotificationEntity): Long
    @Query("SELECT * FROM notifications WHERE toUid = :uid ORDER BY createdAt DESC") fun observeFor(uid: String): Flow<List<NotificationEntity>>
    @Query("SELECT COUNT(*) FROM notifications WHERE toUid = :uid AND read = 0") fun unread(uid: String): Flow<Int>
    @Query("UPDATE notifications SET read = 1 WHERE toUid = :uid") suspend fun markAllRead(uid: String)
    @Query("UPDATE notifications SET read = 1 WHERE id = :id") suspend fun markRead(id: Long)
    @Query("SELECT * FROM notifications WHERE id = :id") suspend fun get(id: Long): NotificationEntity?
}

@Dao
interface AnnouncementDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(a: AnnouncementEntity): Long
    @Query("SELECT * FROM announcements WHERE farmCode = :farm ORDER BY createdAt DESC") fun observeFarm(farm: String): Flow<List<AnnouncementEntity>>
}

@Dao
interface DatasetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(d: DatasetEntity): Long
    @Query("SELECT * FROM dataset ORDER BY createdAt DESC") fun observeAll(): Flow<List<DatasetEntity>>
    @Query("DELETE FROM dataset WHERE scanUuid = :uuid") suspend fun delete(uuid: String)
}

@Dao
interface BatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(b: BatchEntity)
    @Query("SELECT * FROM batches WHERE ownerUid = :uid ORDER BY createdAt DESC") fun observeFor(uid: String): Flow<List<BatchEntity>>
    @Query("DELETE FROM batches WHERE uuid = :uuid") suspend fun delete(uuid: String)
}

@Database(
    entities = [ScanEntity::class, AccountEntity::class, NotificationEntity::class, AnnouncementEntity::class, DatasetEntity::class, BatchEntity::class],
    version = 2, exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun accountDao(): AccountDao
    abstract fun notificationDao(): NotificationDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun datasetDao(): DatasetDao
    abstract fun batchDao(): BatchDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "go_ananas.db")
                .fallbackToDestructiveMigration()
                .build().also { instance = it }
        }
    }
}

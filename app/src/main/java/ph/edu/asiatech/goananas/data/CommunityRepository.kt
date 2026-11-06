package ph.edu.asiatech.goananas.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.edu.asiatech.goananas.notify.Notifier
import ph.edu.asiatech.goananas.sync.Cloud

/** texts of the notifications in English and Tagalog */
object Msg {
    fun newScan(lang: String, who: String, disease: String, block: String, pct: Int) = if (lang == "tl")
        "May bagong scan: $disease" to "Si $who ay nag-scan ng halaman${if (block.isNotBlank()) " sa $block" else ""} ($pct% kumpiyansa). Pakisuri."
    else "New scan: $disease" to "$who scanned a plant${if (block.isNotBlank()) " in $block" else ""} ($pct% confidence). Please review it."

    fun reviewed(lang: String, reviewer: String, disease: String, confirmed: Boolean) = if (lang == "tl")
        "Nasuri na ang iyong scan" to "${if (confirmed) "Kinumpirma" else "Itinama"} ni $reviewer: $disease. Buksan para makita ang payo."
    else "Your scan was reviewed" to "$reviewer ${if (confirmed) "confirmed" else "corrected"} the result: $disease. Open it to read the advice."

    fun outbreak(lang: String, disease: String, block: String, n: Int) = if (lang == "tl")
        "Posibleng pagkalat ng $disease" to "$n halaman sa $block ang may $disease sa nakaraang 14 na araw. Ipaalam sa may-ari o technician."
    else "Possible $disease outbreak" to "$n plants in $block were found with $disease in the last 14 days. Inform the farm owner or technician."

    fun recheck(lang: String, disease: String, block: String) = if (lang == "tl")
        "Oras na para suriin muli ang halaman" to "May $disease na nakita${if (block.isNotBlank()) " sa $block" else ""}. Suriin muli at i-update ang kalagayan."
    else "Time to re-check a plant" to "$disease was found${if (block.isNotBlank()) " in $block" else ""}. Check the plant again and update its status."

    fun monthly(lang: String) = if (lang == "tl")
        "Buwanang pagsusuri ng pinya" to "Libutin ang mga hanay at i-scan ang halamang naninilaw, namumula, nalalanta, o may bulok."
    else "Monthly pineapple inspection" to "Walk the rows and scan plants with yellowing, reddish, or wilting leaves, rotting hearts, or damaged fruits."

    fun joinRequest(lang: String, who: String, role: String) = if (lang == "tl")
        "May gustong sumali sa taniman" to "Si $who ($role) ay humihiling na sumali. Buksan ang Team para aprubahan."
    else "New member request" to "$who ($role) asked to join your farm. Open Team to approve."

    fun approved(lang: String, farm: String) = if (lang == "tl")
        "Naaprubahan ang iyong account" to "Kasapi ka na ng $farm. Maaari nang ma-sync ang iyong mga scan."
    else "Your account was approved" to "You are now a member of $farm. Your scans can now be synced."

    fun roleChanged(lang: String, role: String) = if (lang == "tl")
        "Binago ang iyong role" to "Ang iyong role ay $role na."
    else "Your role was changed" to "Your role is now $role."

    fun announcement(lang: String, from: String, title: String) = if (lang == "tl")
        "Anunsiyo mula kay $from" to title
    else "Announcement from $from" to title
}

/** notifications, announcements, and the dataset of confirmed images */
class CommunityRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val settings: SettingsStore,
) {
    private val cloud get() = Cloud.enabled(context)

    fun notifications(uid: String) = db.notificationDao().observeFor(uid)
    fun unread(uid: String) = db.notificationDao().unread(uid)
    fun announcements(farm: String) = db.announcementDao().observeFarm(farm)
    fun dataset() = db.datasetDao().observeAll()

    /**
     * sends a notification to each user in [to]; [text] builds the title and body in the language of each user.
     * The current user also gets a phone notification; other users receive it through Firestore (or when they sign in, offline).
     */
    suspend fun notify(to: List<AccountEntity>, type: String, scanUuid: String = "", text: (String) -> Pair<String, String>) =
        withContext(Dispatchers.IO) {
            val me = settings.current().sessionUid
            for (u in to.distinctBy { it.uid }) {
                val (title, body) = text(u.language.ifBlank { "en" })
                if (u.uid == me || !cloud) {
                    val id = db.notificationDao().insert(NotificationEntity(toUid = u.uid, title = title, body = body, type = type, scanUuid = scanUuid))
                    if (u.uid == me && id > 0) Notifier.post(context, type, id.toInt(), title, body)
                } else {
                    Cloud.sendInbox(listOf(u.uid), title, body, type, scanUuid)
                }
            }
        }

    /** local alert with a fixed key, so the same alert is not shown twice */
    suspend fun notifyOnce(me: AccountEntity, key: String, type: String, scanUuid: String, title: String, body: String) = withContext(Dispatchers.IO) {
        val id = db.notificationDao().insert(NotificationEntity(remoteId = key, toUid = me.uid, title = title, body = body, type = type, scanUuid = scanUuid))
        if (id > 0) Notifier.post(context, type, id.toInt(), title, body)
        Unit
    }

    /** stores notifications that arrived from Firestore and shows the new ones on the phone */
    suspend fun mergeInbox(list: List<NotificationEntity>) = withContext(Dispatchers.IO) {
        val recent = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        for (n in list) {
            val id = db.notificationDao().insert(n)
            if (id > 0 && !n.read && n.createdAt > recent) Notifier.post(context, n.type, id.toInt(), n.title, n.body)
        }
    }

    suspend fun markAllRead(uid: String) = withContext(Dispatchers.IO) { db.notificationDao().markAllRead(uid) }

    suspend fun markRead(n: NotificationEntity) = withContext(Dispatchers.IO) {
        db.notificationDao().markRead(n.id)
        val rid = n.remoteId
        if (cloud && rid != null && !rid.startsWith("local:")) Cloud.markInboxRead(n.toUid, rid)
        Unit
    }

    suspend fun postAnnouncement(me: AccountEntity, title: String, body: String, members: List<AccountEntity>) = withContext(Dispatchers.IO) {
        val a = AnnouncementEntity(remoteId = if (cloud) null else "local:" + System.nanoTime(), farmCode = me.farmCode,
            title = title.trim(), body = body.trim(), fromName = me.name, fromRole = me.role)
        if (cloud) Cloud.postAnnouncement(a) else db.announcementDao().insert(a)
        notify(members.filter { it.uid != me.uid && it.status == AccountStatus.ACTIVE }, NotificationType.ANNOUNCEMENT) { lang ->
            Msg.announcement(lang, me.name, title.trim()).let { it.first to (title.trim() + " — " + body.trim()) }
        }
    }

    suspend fun mergeAnnouncements(list: List<AnnouncementEntity>) = withContext(Dispatchers.IO) {
        for (a in list) db.announcementDao().insert(a)
    }

    suspend fun mergeDataset(list: List<DatasetEntity>) = withContext(Dispatchers.IO) {
        for (d in list) db.datasetDao().upsert(d)
    }
}

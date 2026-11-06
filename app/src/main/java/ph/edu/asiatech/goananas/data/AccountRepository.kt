package ph.edu.asiatech.goananas.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ph.edu.asiatech.goananas.sync.Cloud
import java.io.File
import java.security.MessageDigest
import java.util.UUID

/** a message the screens can show to the user (English and Tagalog) */
class UserMessage(val en: String, val tl: String) : Exception(en) {
    fun text(lang: String) = if (lang == "tl") tl else en
}

/**
 * Accounts and sign-in. With Firebase (google-services.json present) the accounts are Firebase accounts
 * shared with the web dashboard; without it, the accounts are kept on the phone (offline mode) and five
 * sample accounts, one per role, are created for demonstration.
 */
class AccountRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val settings: SettingsStore,
) {
    private val dao get() = db.accountDao()
    val cloud: Boolean get() = Cloud.enabled(context)

    @OptIn(ExperimentalCoroutinesApi::class)
    val session: Flow<AccountEntity?> = settings.settings.map { it.sessionUid }.distinctUntilChanged()
        .flatMapLatest { uid -> if (uid.isBlank()) flowOf(null) else dao.observe(uid) }

    suspend fun current(): AccountEntity? {
        val uid = settings.current().sessionUid
        return if (uid.isBlank()) null else dao.get(uid)
    }

    private fun hash(email: String, password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest("${email.lowercase()}:$password:goananas".toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun normalizeFarm(code: String) = code.trim().uppercase().replace(Regex("\\s+"), "-")

    /** offline mode only: one sample account per role so the panel can try every role on one phone */
    suspend fun seedDemoAccounts() = withContext(Dispatchers.IO) {
        if (cloud || dao.count() > 0) return@withContext
        val farm = "DAKO-TAGAYTAY"
        val people = listOf(
            Triple("farmer@demo.ph", "Juan Dela Cruz", Role.FARMER),
            Triple("vendor@demo.ph", "Maria Santos", Role.VENDOR),
            Triple("tech@demo.ph", "Ramon Villanueva", Role.TECHNICIAN),
            Triple("owner@demo.ph", "Rosa Mendoza", Role.OWNER),
            Triple("admin@demo.ph", "System Administrator", Role.ADMIN),
        )
        for ((email, name, role) in people) {
            dao.upsert(AccountEntity(
                uid = "local-" + role.key, email = email, passwordHash = hash(email, DEMO_PASSWORD), name = name,
                role = role.key, farmCode = farm, farmName = "Dako Pineapple Farm",
                gender = if (role == Role.VENDOR || role == Role.OWNER) "Female" else "Male",
                ageRange = "26–35", yearsFarming = if (role == Role.ADMIN) "Not applicable" else "6–10 years",
                location = "Tagaytay City, Cavite", status = AccountStatus.ACTIVE,
            ))
        }
    }

    suspend fun register(
        email: String, password: String, role: Role, farmCodeIn: String, farmName: String, lang: String,
    ): AccountEntity = withContext(Dispatchers.IO) {
        val mail = email.trim().lowercase()
        val farm = normalizeFarm(farmCodeIn)
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) throw UserMessage("Enter a valid e-mail address.", "Maglagay ng tamang e-mail.")
        if (password.length < 6) throw UserMessage("The password must have at least 6 characters.", "Dapat may 6 o higit pang karakter ang password.")
        if (farm.isBlank()) throw UserMessage("Enter the farm code.", "Ilagay ang farm code.")
        if (role == Role.OWNER && farmName.isBlank()) throw UserMessage("Enter the name of your farm.", "Ilagay ang pangalan ng iyong taniman.")
        val status = if (role == Role.OWNER) AccountStatus.ACTIVE else AccountStatus.PENDING

        val account: AccountEntity
        if (cloud) {
            val uid = try { Cloud.register(mail, password) } catch (e: Exception) { throw friendly(e) }
            val existing = Cloud.farmName(farm)
            if (role == Role.OWNER && existing != null) { Cloud.signOut(); throw UserMessage("That farm code is already used. Choose another one.", "May gumagamit na ng farm code na iyan. Pumili ng iba.") }
            if (role != Role.OWNER && existing == null) { Cloud.signOut(); throw UserMessage("Farm code not found. Ask your farm owner for the correct code.", "Walang ganitong farm code. Itanong sa may-ari ng taniman ang tamang code.") }
            account = AccountEntity(uid = uid, email = mail, role = role.key, farmCode = farm, farmName = existing ?: farmName.trim(), language = lang, status = status)
            Cloud.saveProfile(account)
            if (role == Role.OWNER) Cloud.createFarm(farm, farmName.trim(), uid, "")
        } else {
            if (dao.byEmail(mail) != null) throw UserMessage("This e-mail already has an account. Please sign in.", "May account na ang e-mail na ito. Mag-sign in na lang.")
            val hasOwner = dao.ownersOf(farm) > 0
            if (role == Role.OWNER && hasOwner) throw UserMessage("That farm code is already used. Choose another one.", "May gumagamit na ng farm code na iyan. Pumili ng iba.")
            if (role != Role.OWNER && !hasOwner) throw UserMessage("Farm code not found. Ask your farm owner for the correct code.", "Walang ganitong farm code. Itanong sa may-ari ng taniman ang tamang code.")
            val farmTitle = if (role == Role.OWNER) farmName.trim() else (dao.farmMembers(farm).firstOrNull { it.role == Role.OWNER.key }?.farmName ?: farm)
            account = AccountEntity(uid = "local-" + UUID.randomUUID(), email = mail, passwordHash = hash(mail, password), role = role.key,
                farmCode = farm, farmName = farmTitle, language = lang, status = status)
        }
        dao.upsert(account)
        settings.setSession(account.uid)
        account
    }

    suspend fun login(email: String, password: String): AccountEntity = withContext(Dispatchers.IO) {
        val mail = email.trim().lowercase()
        val account: AccountEntity = if (cloud) {
            val uid = try { Cloud.signIn(mail, password) } catch (e: Exception) { throw friendly(e) }
            val fetched: AccountEntity? = try { Cloud.getProfile(uid) } catch (e: Exception) { dao.get(uid) }
            val profile = fetched ?: throw UserMessage("Your profile was not found. Please register again.", "Hindi makita ang iyong profile. Magparehistro muli.")
            profile
        } else {
            val a = dao.byEmail(mail)
            if (a == null || a.passwordHash != hash(mail, password)) throw UserMessage("Wrong e-mail or password.", "Mali ang e-mail o password.")
            a
        }
        if (account.status == AccountStatus.DISABLED) {
            if (cloud) Cloud.signOut()
            throw UserMessage("This account was disabled by the administrator.", "Ang account na ito ay hindi na pinapagana ng administrator.")
        }
        dao.upsert(if (cloud) account.copy(passwordHash = "") else account)
        settings.setSession(account.uid)
        account
    }

    suspend fun resetPassword(email: String) {
        if (!cloud) throw UserMessage("In offline mode, ask the farm owner or administrator to create a new account.", "Sa offline mode, magpagawa ng bagong account sa may-ari o administrator.")
        try { Cloud.resetPassword(email) } catch (e: Exception) { throw friendly(e) }
    }

    suspend fun logout() {
        if (cloud) Cloud.signOut()
        settings.setSession("")
    }

    suspend fun saveProfile(a: AccountEntity) = withContext(Dispatchers.IO) {
        dao.upsert(a)
        if (cloud) runCatching { Cloud.updateOwnProfile(a) }
        Unit
    }

    /** keeps a copy of the picture on the phone and uploads it when Firebase is set up */
    suspend fun savePhoto(a: AccountEntity, uri: Uri): AccountEntity = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "avatars").apply { mkdirs() }
        val file = File(dir, "${a.uid}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input -> file.outputStream().use { input.copyTo(it) } }
        var url = file.absolutePath
        if (cloud) {
            // Cloud Storage when enabled; otherwise a small copy is kept in the profile document
            url = runCatching { Cloud.uploadAvatar(a.uid, Uri.fromFile(file)) }.getOrNull()
                ?: Thumbs.fromFile(file, maxSide = 160, quality = 75) ?: file.absolutePath
        }
        val updated = a.copy(photoUrl = url)
        saveProfile(updated)
        updated
    }

    // ---------------------------------------------------------------- team and user management
    fun observeFarmMembers(farm: String) = dao.observeFarm(farm)
    fun observeAllUsers() = dao.observeAll()

    suspend fun setStatus(target: AccountEntity, status: String) = withContext(Dispatchers.IO) {
        dao.upsert(target.copy(status = status))
        if (cloud) Cloud.setUserFields(target.uid, mapOf("status" to status))
        Unit
    }

    suspend fun setRole(target: AccountEntity, role: Role) = withContext(Dispatchers.IO) {
        dao.upsert(target.copy(role = role.key))
        if (cloud) Cloud.setUserFields(target.uid, mapOf("role" to role.key))
        Unit
    }

    suspend fun cacheAccounts(list: List<AccountEntity>) = withContext(Dispatchers.IO) {
        for (a in list) {
            val old = dao.get(a.uid)
            dao.upsert(a.copy(passwordHash = old?.passwordHash ?: ""))
        }
    }

    suspend fun staffOf(farm: String): List<AccountEntity> = withContext(Dispatchers.IO) {
        val list = if (cloud) runCatching { Cloud.farmMembers(farm) }.getOrElse { dao.farmMembers(farm) } else dao.farmMembers(farm)
        list.filter { it.status == AccountStatus.ACTIVE && (it.role == Role.OWNER.key || it.role == Role.TECHNICIAN.key) }
    }

    suspend fun membersOf(farm: String): List<AccountEntity> = withContext(Dispatchers.IO) {
        if (cloud) runCatching { Cloud.farmMembers(farm) }.getOrElse { dao.farmMembers(farm) } else dao.farmMembers(farm)
    }

    private fun friendly(e: Exception): UserMessage {
        val m = e.message ?: ""
        return when {
            "already in use" in m -> UserMessage("This e-mail already has an account. Please sign in.", "May account na ang e-mail na ito. Mag-sign in na lang.")
            "password is invalid" in m || "credential" in m || "no user record" in m ->
                UserMessage("Wrong e-mail or password.", "Mali ang e-mail o password.")
            "network" in m.lowercase() -> UserMessage("No internet connection. Please try again.", "Walang internet. Subukan muli.")
            else -> UserMessage("Something went wrong: $m", "May problema: $m")
        }
    }

    companion object {
        const val DEMO_PASSWORD = "goananas123"
    }
}

package ph.edu.asiatech.goananas.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "go_ananas_settings")

/** settings kept on the phone (the profile itself is in the accounts table) */
data class AppSettings(
    val language: String = "",          // "" until the user chooses English ("en") or Tagalog ("tl")
    val tutorialDone: Boolean = false,
    val sessionUid: String = "",        // signed-in account; "" when signed out
    val recheckReminders: Boolean = true,
    val monthlyReminder: Boolean = true,
    val outbreakAlerts: Boolean = true,
    val lastInboxCheck: Long = 0L,
)

class SettingsStore(private val context: Context) {
    private object K {
        val language = stringPreferencesKey("language")
        val tutorial = booleanPreferencesKey("tutorial_done")
        val session = stringPreferencesKey("session_uid")
        val recheck = booleanPreferencesKey("recheck_reminders")
        val monthly = booleanPreferencesKey("monthly_reminder")
        val outbreak = booleanPreferencesKey("outbreak_alerts")
        val inbox = longPreferencesKey("last_inbox_check")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            language = p[K.language] ?: "",
            tutorialDone = p[K.tutorial] ?: false,
            sessionUid = p[K.session] ?: "",
            recheckReminders = p[K.recheck] ?: true,
            monthlyReminder = p[K.monthly] ?: true,
            outbreakAlerts = p[K.outbreak] ?: true,
            lastInboxCheck = p[K.inbox] ?: 0L,
        )
    }

    suspend fun current(): AppSettings = settings.first()
    suspend fun setLanguage(lang: String) { context.dataStore.edit { it[K.language] = lang } }
    suspend fun setTutorialDone(done: Boolean) { context.dataStore.edit { it[K.tutorial] = done } }
    suspend fun setSession(uid: String) { context.dataStore.edit { it[K.session] = uid } }
    suspend fun setRecheckReminders(on: Boolean) { context.dataStore.edit { it[K.recheck] = on } }
    suspend fun setMonthlyReminder(on: Boolean) { context.dataStore.edit { it[K.monthly] = on } }
    suspend fun setOutbreakAlerts(on: Boolean) { context.dataStore.edit { it[K.outbreak] = on } }
    suspend fun setLastInboxCheck(t: Long) { context.dataStore.edit { it[K.inbox] = t } }
}

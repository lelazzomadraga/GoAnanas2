package ph.edu.asiatech.goananas

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import ph.edu.asiatech.goananas.data.AccountRepository
import ph.edu.asiatech.goananas.data.AppDatabase
import ph.edu.asiatech.goananas.data.CommunityRepository
import ph.edu.asiatech.goananas.data.ScanRepository
import ph.edu.asiatech.goananas.data.SettingsStore
import ph.edu.asiatech.goananas.ml.PineappleClassifier
import ph.edu.asiatech.goananas.notify.Notifier
import ph.edu.asiatech.goananas.sync.InboxWorker
import ph.edu.asiatech.goananas.sync.LiveSync

/** creates the shared objects once (simple manual dependency injection) */
class GoAnanasApp : Application() {
    lateinit var db: AppDatabase
    lateinit var settings: SettingsStore
    lateinit var accounts: AccountRepository
    lateinit var community: CommunityRepository
    lateinit var scans: ScanRepository
    lateinit var liveSync: LiveSync
    val classifier: PineappleClassifier by lazy { PineappleClassifier(this) }
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        OpenCVLoader.initLocal()
        db = AppDatabase.get(this)
        settings = SettingsStore(this)
        accounts = AccountRepository(this, db, settings)
        community = CommunityRepository(this, db, settings)
        scans = ScanRepository(this, db, settings, accounts, community)
        liveSync = LiveSync(this)
        Notifier.createChannels(this)
        InboxWorker.schedule(this)
        appScope.launch { accounts.seedDemoAccounts() }
        appScope.launch { accounts.session.collect { liveSync.update(it) } }
    }
}

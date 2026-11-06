package ph.edu.asiatech.goananas

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableIntStateOf
import ph.edu.asiatech.goananas.notify.Notifier
import ph.edu.asiatech.goananas.ui.GoAnanasRoot

class MainActivity : ComponentActivity() {
    /** increases each time a notification is tapped, so the app opens the Alerts screen */
    private val openAlerts = mutableIntStateOf(0)

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        readIntent(intent)
        setContent { GoAnanasRoot(openAlerts = openAlerts.intValue) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        readIntent(intent)
    }

    private fun readIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(Notifier.EXTRA_OPEN_ALERTS, false) == true) openAlerts.intValue += 1
    }
}

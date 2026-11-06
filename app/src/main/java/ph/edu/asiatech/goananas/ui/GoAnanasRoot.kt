package ph.edu.asiatech.goananas.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ph.edu.asiatech.goananas.R
import ph.edu.asiatech.goananas.data.AccountEntity
import ph.edu.asiatech.goananas.ui.screens.AnnouncementsScreen
import ph.edu.asiatech.goananas.ui.screens.AuthScreen
import ph.edu.asiatech.goananas.ui.screens.BatchesScreen
import ph.edu.asiatech.goananas.ui.screens.CaptureScreen
import ph.edu.asiatech.goananas.ui.screens.DashboardScreen
import ph.edu.asiatech.goananas.ui.screens.DatasetScreen
import ph.edu.asiatech.goananas.ui.screens.GuideDetailScreen
import ph.edu.asiatech.goananas.ui.screens.GuideScreen
import ph.edu.asiatech.goananas.ui.screens.HistoryScreen
import ph.edu.asiatech.goananas.ui.screens.HomeScreen
import ph.edu.asiatech.goananas.ui.screens.LanguageScreen
import ph.edu.asiatech.goananas.ui.screens.NotificationsScreen
import ph.edu.asiatech.goananas.ui.screens.ProfileScreen
import ph.edu.asiatech.goananas.ui.screens.ProfileSetupScreen
import ph.edu.asiatech.goananas.ui.screens.ResultScreen
import ph.edu.asiatech.goananas.ui.screens.ReviewQueueScreen
import ph.edu.asiatech.goananas.ui.screens.ScanDetailScreen
import ph.edu.asiatech.goananas.ui.screens.TeamScreen
import ph.edu.asiatech.goananas.ui.screens.TutorialScreen
import ph.edu.asiatech.goananas.ui.theme.GoAnanasTheme
import ph.edu.asiatech.goananas.ui.theme.LeafGreen
import ph.edu.asiatech.goananas.ui.theme.MintGreen

object Routes {
    const val HOME = "home"
    const val RECORDS = "records?filter={filter}"
    const val ALERTS = "alerts"
    const val PROFILE = "profile"
    const val CAPTURE = "capture?mode={mode}"
    const val RESULT = "result"
    const val SCAN = "scan/{id}"
    const val DASHBOARD = "dashboard"
    const val GUIDE = "guide"
    const val GUIDE_DETAIL = "guide/{label}"
    const val REVIEW = "review"
    const val TEAM = "team"
    const val ANNOUNCEMENTS = "announcements"
    const val DATASET = "dataset"
    const val BATCHES = "batches"
    const val EDIT_PROFILE = "edit_profile"
    fun records(filter: String = "all") = "records?filter=$filter"
    fun capture(mode: String = "field") = "capture?mode=$mode"
    fun scan(id: Long) = "scan/$id"
    fun guide(label: String) = "guide/$label"
}

private data class Tab(val route: String, val nav: String, val en: String, val tl: String, val icon: ImageVector)
private val tabs = listOf(
    Tab(Routes.HOME, Routes.HOME, "Home", "Home", Icons.Filled.Home),
    Tab(Routes.RECORDS, Routes.records(), "Records", "Talaan", Icons.Filled.PhotoLibrary),
    Tab(Routes.ALERTS, Routes.ALERTS, "Alerts", "Abiso", Icons.Filled.Notifications),
    Tab(Routes.PROFILE, Routes.PROFILE, "Profile", "Profile", Icons.Filled.AccountCircle),
)

@Composable
fun GoAnanasRoot(openAlerts: Int, vm: AppViewModel = viewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val me by vm.me.collectAsStateWithLifecycle()
    val s = settings
    val language = s?.language?.ifBlank { "en" } ?: "en"
    CompositionLocalProvider(LocalLang provides language) {
        GoAnanasTheme {
            val stage = when {
                s == null -> "loading"
                s.language.isBlank() -> "language"
                !s.tutorialDone -> "tutorial"
                s.sessionUid.isBlank() -> "auth"
                me == null -> "loading"
                me?.profileComplete != true -> "setup"
                else -> "main"
            }
            AnimatedContent(targetState = stage, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "stage") { st ->
                when (st) {
                    "loading" -> Splash()
                    "language" -> LanguageScreen(onPick = { vm.setLanguage(it) })
                    "tutorial" -> TutorialScreen(onDone = { vm.finishTutorial() })
                    "auth" -> AuthScreen(vm)
                    "setup" -> me?.let { ProfileSetupScreen(vm, it, firstTime = true, onDone = {}) } ?: Splash()
                    else -> me?.let { MainNav(vm, it, openAlerts) } ?: Splash()
                }
            }
        }
    }
}

@Composable
private fun Splash() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.ic_logo), null, Modifier.size(96.dp))
            Spacer(Modifier.height(12.dp))
            Text("Go Ananas!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = LeafGreen)
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(color = LeafGreen)
        }
    }
}

@Composable
private fun MainNav(vm: AppViewModel, me: AccountEntity, openAlerts: Int) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val unread by vm.unread.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(openAlerts) { if (openAlerts > 0) nav.goTab(Routes.ALERTS) }
    LaunchedEffect(vm.message) {
        vm.message?.let { snack.showSnackbar(it); vm.message = null }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            if (route in tabs.map { it.route }) {
                NavigationBar(containerColor = Color.White) {
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected = route == t.route,
                            onClick = { nav.goTab(t.nav) },
                            icon = {
                                if (t.route == Routes.ALERTS && unread > 0) {
                                    BadgedBox(badge = { Badge { Text("$unread") } }) { Icon(t.icon, contentDescription = t.en) }
                                } else Icon(t.icon, contentDescription = t.en)
                            },
                            label = { Text(tr(t.en, t.tl)) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = MintGreen, selectedIconColor = LeafGreen, selectedTextColor = LeafGreen),
                        )
                    }
                }
            }
        },
    ) { pad ->
        NavHost(nav, startDestination = Routes.HOME, modifier = Modifier.padding(pad)) {
            composable(Routes.HOME) { HomeScreen(vm, me, nav) }
            composable(Routes.RECORDS, arguments = listOf(navArgument("filter") { type = NavType.StringType; defaultValue = "all" })) { e ->
                HistoryScreen(vm, me, e.arguments?.getString("filter") ?: "all", onOpenScan = { nav.navigate(Routes.scan(it)) })
            }
            composable(Routes.ALERTS) { NotificationsScreen(vm, onOpenScan = { nav.navigate(Routes.scan(it)) }) }
            composable(Routes.PROFILE) { ProfileScreen(vm, me, onEdit = { nav.navigate(Routes.EDIT_PROFILE) }) }
            composable(Routes.EDIT_PROFILE) { ProfileSetupScreen(vm, me, firstTime = false, onDone = { nav.popBackStack() }) }
            composable(Routes.CAPTURE, arguments = listOf(navArgument("mode") { type = NavType.StringType; defaultValue = "field" })) { e ->
                CaptureScreen(vm, e.arguments?.getString("mode") ?: "field", onBack = { nav.popBackStack() }, onAnalyzed = { nav.navigate(Routes.RESULT) })
            }
            composable(Routes.RESULT) {
                ResultScreen(vm, me, onBack = { vm.resetAnalysis(); nav.popBackStack() },
                    onSaved = { id -> nav.popBackStack(Routes.HOME, inclusive = false); nav.navigate(Routes.scan(id)) },
                    onGuide = { nav.navigate(Routes.guide(it)) })
            }
            composable(Routes.SCAN, arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                ScanDetailScreen(vm, me, e.arguments?.getLong("id") ?: 0L, onBack = { nav.popBackStack() }, onGuide = { nav.navigate(Routes.guide(it)) })
            }
            composable(Routes.DASHBOARD) { DashboardScreen(vm, me, onBack = { nav.popBackStack() }, onOpenScan = { nav.navigate(Routes.scan(it)) }) }
            composable(Routes.GUIDE) { GuideScreen(onBack = { nav.popBackStack() }, onOpen = { nav.navigate(Routes.guide(it)) }) }
            composable(Routes.GUIDE_DETAIL, arguments = listOf(navArgument("label") { type = NavType.StringType })) { e ->
                GuideDetailScreen(e.arguments?.getString("label") ?: "", onBack = { nav.popBackStack() })
            }
            composable(Routes.REVIEW) { ReviewQueueScreen(vm, me, onBack = { nav.popBackStack() }, onOpenScan = { nav.navigate(Routes.scan(it)) }) }
            composable(Routes.TEAM) { TeamScreen(vm, me, onBack = { nav.popBackStack() }) }
            composable(Routes.ANNOUNCEMENTS) { AnnouncementsScreen(vm, me, onBack = { nav.popBackStack() }) }
            composable(Routes.DATASET) { DatasetScreen(vm, me, onBack = { nav.popBackStack() }) }
            composable(Routes.BATCHES) {
                BatchesScreen(vm, onBack = { nav.popBackStack() }, onScan = { nav.navigate(Routes.capture("fruit")) }, onOpenScan = { nav.navigate(Routes.scan(it)) })
            }
        }
    }
}

fun NavHostController.goTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

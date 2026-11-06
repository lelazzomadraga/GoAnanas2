package ph.edu.asiatech.goananas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import ph.edu.asiatech.goananas.data.AccountEntity
import ph.edu.asiatech.goananas.data.AccountStatus
import ph.edu.asiatech.goananas.data.ReviewStatus
import ph.edu.asiatech.goananas.data.Role
import ph.edu.asiatech.goananas.data.ScanStatus
import ph.edu.asiatech.goananas.ui.ActionTile
import ph.edu.asiatech.goananas.ui.AppViewModel
import ph.edu.asiatech.goananas.ui.Avatar
import ph.edu.asiatech.goananas.ui.GradientHeader
import ph.edu.asiatech.goananas.ui.InfoBanner
import ph.edu.asiatech.goananas.ui.Routes
import ph.edu.asiatech.goananas.ui.ScanRow
import ph.edu.asiatech.goananas.ui.SectionCard
import ph.edu.asiatech.goananas.ui.StatTile
import ph.edu.asiatech.goananas.ui.goTab
import ph.edu.asiatech.goananas.ui.lang
import ph.edu.asiatech.goananas.ui.theme.AlertRed
import ph.edu.asiatech.goananas.ui.theme.DarkGold
import ph.edu.asiatech.goananas.ui.theme.FreshGreen
import ph.edu.asiatech.goananas.ui.theme.LeafGreen
import ph.edu.asiatech.goananas.ui.theme.MintGreen
import ph.edu.asiatech.goananas.ui.theme.Plum
import ph.edu.asiatech.goananas.ui.theme.PineGold
import ph.edu.asiatech.goananas.ui.theme.SkyBlue
import ph.edu.asiatech.goananas.ui.theme.SoftBlue
import ph.edu.asiatech.goananas.ui.theme.SoftGold
import ph.edu.asiatech.goananas.ui.theme.SoftPlum
import ph.edu.asiatech.goananas.ui.theme.SoftRed
import ph.edu.asiatech.goananas.ui.tr

private class TileSpec(val icon: ImageVector, val title: String, val sub: String, val tint: Color, val bg: Color, val badge: Int = 0, val onClick: () -> Unit)

/** home screen with the functions of the signed-in role */
@Composable
fun HomeScreen(vm: AppViewModel, me: AccountEntity, nav: NavHostController) {
    val scans by vm.scans.collectAsStateWithLifecycle()
    val members by vm.members.collectAsStateWithLifecycle()
    val announcements by vm.announcements.collectAsStateWithLifecycle()
    val l = lang()
    val role = me.roleEnum
    val now = System.currentTimeMillis()
    val diseased = scans.count { !it.pineappleClass.isHealthy }
    val dueRechecks = scans.count { it.status == ScanStatus.OPEN && (it.recheckAt ?: Long.MAX_VALUE) <= now && it.userId == me.uid }
    val forReview = scans.count { it.reviewStatus == ReviewStatus.PENDING }
    val pending = members.count { it.status == AccountStatus.PENDING }
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greet = when {
        hour < 12 -> tr("Good morning", "Magandang umaga")
        hour < 18 -> tr("Good afternoon", "Magandang hapon")
        else -> tr("Good evening", "Magandang gabi")
    }

    val scanTile = TileSpec(Icons.Filled.CameraAlt, tr("Scan a plant", "Mag-scan ng halaman"), tr("Leaf, crown, heart, base, or fruit", "Dahon, korona, puso, puno, o bunga"), LeafGreen, MintGreen) { nav.navigate(Routes.capture("field")) }
    val recordsTile = TileSpec(Icons.Filled.PhotoLibrary, tr("My records", "Aking talaan"), tr("Photos and results", "Mga larawan at resulta"), SkyBlue, SoftBlue) { nav.goTab(Routes.records()) }
    val recheckTile = TileSpec(Icons.Filled.Autorenew, tr("Re-check plants", "Suriin muli"), tr("Plants that need action", "Halamang kailangang aksyunan"), AlertRed, SoftRed, dueRechecks) { nav.navigate(Routes.records("open")) }
    val guideTile = TileSpec(Icons.AutoMirrored.Filled.MenuBook, tr("Disease guide", "Gabay sa sakit"), tr("Symptoms and what to do", "Sintomas at gagawin"), DarkGold, SoftGold) { nav.navigate(Routes.GUIDE) }
    val newsTile = TileSpec(Icons.Filled.Campaign, tr("Announcements", "Mga anunsiyo"), tr("Messages from the farm", "Mensahe mula sa taniman"), Plum, SoftPlum, 0) { nav.navigate(Routes.ANNOUNCEMENTS) }
    val dashTile = TileSpec(Icons.Filled.Dashboard, tr("Farm dashboard", "Dashboard ng taniman"), tr("Charts and affected blocks", "Tsart at apektadong block"), LeafGreen, MintGreen) { nav.navigate(Routes.DASHBOARD) }
    val reviewTile = TileSpec(Icons.Filled.RateReview, tr("Review scans", "Suriin ang mga scan"), tr("Confirm or correct results", "Kumpirmahin o itama"), SkyBlue, SoftBlue, forReview) { nav.navigate(Routes.REVIEW) }
    val datasetTile = TileSpec(Icons.Filled.Collections, tr("Image dataset", "Dataset ng larawan"), tr("Confirmed images for training", "Kumpirmadong larawan"), Plum, SoftPlum) { nav.navigate(Routes.DATASET) }
    val teamTile = TileSpec(Icons.Filled.Groups, tr("Team", "Mga kasapi"), tr("Approve and manage workers", "Aprubahan ang mga manggagawa"), DarkGold, SoftGold, pending) { nav.navigate(Routes.TEAM) }
    val usersTile = TileSpec(Icons.Filled.ManageAccounts, tr("User accounts", "Mga account"), tr("Roles and access of all users", "Role at access ng lahat"), DarkGold, SoftGold, pending) { nav.navigate(Routes.TEAM) }
    val fruitTile = TileSpec(Icons.Filled.CameraAlt, tr("Check a fruit", "Suriin ang bunga"), tr("Before buying or selling", "Bago bumili o magbenta"), LeafGreen, MintGreen) { nav.navigate(Routes.capture("fruit")) }
    val batchTile = TileSpec(Icons.Filled.Inventory, tr("Fruit batches", "Mga batch ng bunga"), tr("Accepted and rejected fruits", "Tinanggap at tinanggihan"), SkyBlue, SoftBlue) { nav.navigate(Routes.BATCHES) }

    val tiles = when (role) {
        Role.FARMER -> listOf(scanTile, recheckTile, recordsTile, guideTile, newsTile, dashTile.let { TileSpec(it.icon, tr("My summary", "Buod ko"), tr("My scans in charts", "Mga scan ko sa tsart"), it.tint, it.bg, 0, it.onClick) })
        Role.VENDOR -> listOf(fruitTile, batchTile, recordsTile, guideTile, newsTile)
        Role.TECHNICIAN -> listOf(reviewTile, dashTile, scanTile, newsTile.let { TileSpec(it.icon, tr("Send advice", "Magpadala ng payo"), tr("Announcement to the farm", "Anunsiyo sa taniman"), it.tint, it.bg, 0, it.onClick) }, datasetTile, guideTile)
        Role.OWNER -> listOf(dashTile, teamTile, reviewTile, newsTile, scanTile, guideTile)
        Role.ADMIN -> listOf(usersTile, dashTile.let { TileSpec(it.icon, tr("All farms", "Lahat ng taniman"), tr("Scans of every farm", "Scan ng lahat ng taniman"), it.tint, it.bg, 0, it.onClick) }, datasetTile, reviewTile, guideTile, newsTile)
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        GradientHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(me.photoUrl, 52.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("$greet,", color = Color.White.copy(alpha = 0.85f))
                    Text(me.name.ifBlank { me.email }, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("${role.label(l)} • ${me.farmName.ifBlank { me.farmCode }}", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                }
            }
            if (role == Role.FARMER || role == Role.VENDOR) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { nav.navigate(Routes.capture(if (role == Role.VENDOR) "fruit" else "field")) },
                    modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PineGold, contentColor = Color(0xFF3A2A00)),
                ) {
                    Icon(Icons.Filled.CameraAlt, null, Modifier.size(22.dp)); Spacer(Modifier.width(8.dp))
                    Text(if (role == Role.VENDOR) tr("Check a fruit", "Suriin ang bunga") else tr("Scan a pineapple", "Mag-scan ng pinya"), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            }
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (me.status == AccountStatus.PENDING) {
                InfoBanner(tr("Your account is waiting for the farm owner's approval. You can already scan; your records will be shared once you are approved.",
                    "Hinihintay pa ang pag-apruba ng may-ari sa iyong account. Maaari ka nang mag-scan; maibabahagi ang talaan kapag naaprubahan ka na."),
                    DarkGold, SoftGold, Icons.Filled.HourglassTop)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("${scans.size}", if (role.seesFarm) tr("Farm scans", "Scan sa taniman") else tr("My scans", "Aking scan"), LeafGreen, Modifier.weight(1f))
                StatTile("$diseased", tr("With disease", "May sakit"), AlertRed, Modifier.weight(1f))
                if (role.seesFarm) StatTile("$forReview", tr("For review", "Susuriin"), SkyBlue, Modifier.weight(1f))
                else StatTile("${scans.size - diseased}", tr("Healthy", "Malusog"), FreshGreen, Modifier.weight(1f))
            }

            tiles.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { t -> ActionTile(t.icon, t.title, t.sub, t.tint, t.bg, Modifier.weight(1f), t.badge, t.onClick) }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            announcements.firstOrNull()?.let { a ->
                SectionCard(tr("Latest announcement", "Pinakabagong anunsiyo")) {
                    Text(a.title, fontWeight = FontWeight.Bold)
                    if (a.body.isNotBlank()) Text(a.body, style = MaterialTheme.typography.bodyMedium)
                    Text("— ${a.fromName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            SectionCard(if (role.seesFarm) tr("Latest farm scans", "Pinakabagong scan sa taniman") else tr("My latest scans", "Pinakabago kong scan")) {
                if (scans.isEmpty()) Text(tr("No scans yet. Tap \"Scan\" to check your first plant.", "Wala pang scan. Pindutin ang \"Scan\" para suriin ang unang halaman."))
                scans.take(4).forEach { ScanRow(it, showUser = role.seesFarm) { nav.navigate(Routes.scan(it.id)) } }
            }

            SectionCard(tr("Tips for a good photo", "Mga tip para sa magandang larawan")) {
                listOf(
                    tr("Take the photo 30 to 60 cm from the plant, in daylight.", "Kunan mula 30 hanggang 60 cm, sa liwanag ng araw."),
                    tr("Fill the frame with the affected part.", "Punuin ang frame ng apektadong bahagi."),
                    tr("Avoid shadows and direct sunlight on the lens.", "Iwasan ang anino at direktang sikat ng araw sa lente."),
                    tr("Take another photo if the confidence is below 60%.", "Kumuha ng panibago kung mas mababa sa 60% ang kumpiyansa."),
                ).forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}

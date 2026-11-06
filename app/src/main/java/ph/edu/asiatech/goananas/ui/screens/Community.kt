package ph.edu.asiatech.goananas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import ph.edu.asiatech.goananas.BuildConfig
import ph.edu.asiatech.goananas.data.AccountEntity
import ph.edu.asiatech.goananas.data.AccountStatus
import ph.edu.asiatech.goananas.data.FARMING_YEARS
import ph.edu.asiatech.goananas.data.GENDERS
import ph.edu.asiatech.goananas.data.NotificationType
import ph.edu.asiatech.goananas.data.PineappleClass
import ph.edu.asiatech.goananas.data.Role
import ph.edu.asiatech.goananas.data.ScanStatus
import ph.edu.asiatech.goananas.ui.AppViewModel
import ph.edu.asiatech.goananas.ui.Avatar
import ph.edu.asiatech.goananas.ui.EmptyState
import ph.edu.asiatech.goananas.ui.GradientHeader
import ph.edu.asiatech.goananas.ui.InfoBanner
import ph.edu.asiatech.goananas.ui.Pill
import ph.edu.asiatech.goananas.ui.ScanRow
import ph.edu.asiatech.goananas.ui.SectionCard
import ph.edu.asiatech.goananas.ui.StatTile
import ph.edu.asiatech.goananas.ui.classColor
import ph.edu.asiatech.goananas.ui.dateOnly
import ph.edu.asiatech.goananas.ui.lang
import ph.edu.asiatech.goananas.ui.timeAgo
import ph.edu.asiatech.goananas.ui.theme.AlertRed
import ph.edu.asiatech.goananas.ui.theme.DarkGold
import ph.edu.asiatech.goananas.ui.theme.FreshGreen
import ph.edu.asiatech.goananas.ui.theme.LeafGreen
import ph.edu.asiatech.goananas.ui.theme.MintGreen
import ph.edu.asiatech.goananas.ui.theme.Muted
import ph.edu.asiatech.goananas.ui.theme.Plum
import ph.edu.asiatech.goananas.ui.theme.SkyBlue
import ph.edu.asiatech.goananas.ui.theme.SoftBlue
import ph.edu.asiatech.goananas.ui.theme.SoftGold
import ph.edu.asiatech.goananas.ui.theme.SoftGreen
import ph.edu.asiatech.goananas.ui.theme.SoftPlum
import ph.edu.asiatech.goananas.ui.theme.SoftRed
import ph.edu.asiatech.goananas.ui.tr

private fun typeIcon(type: String): Triple<ImageVector, Color, Color> = when (type) {
    NotificationType.RECHECK -> Triple(Icons.Filled.Autorenew, DarkGold, SoftGold)
    NotificationType.MONTHLY -> Triple(Icons.Filled.EventAvailable, LeafGreen, SoftGreen)
    NotificationType.OUTBREAK -> Triple(Icons.Filled.Warning, AlertRed, SoftRed)
    NotificationType.NEW_SCAN -> Triple(Icons.Filled.RateReview, SkyBlue, SoftBlue)
    NotificationType.REVIEW -> Triple(Icons.Filled.CheckCircle, FreshGreen, SoftGreen)
    NotificationType.ANNOUNCEMENT -> Triple(Icons.Filled.Campaign, Plum, SoftPlum)
    else -> Triple(Icons.Filled.PersonAdd, DarkGold, SoftGold)
}

/** notification center */
@Composable
fun NotificationsScreen(vm: AppViewModel, onOpenScan: (Long) -> Unit) {
    val list by vm.notifications.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val l = lang()
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(tr("Alerts", "Mga abiso"), actions = {
            if (list.any { !it.read }) TextButton(onClick = { vm.markAllRead() }) { Icon(Icons.Filled.DoneAll, null); Spacer(Modifier.width(4.dp)); Text(tr("Mark all read", "Basa na lahat")) }
        })
        if (list.isEmpty()) EmptyState(Icons.Filled.Notifications, tr("No alerts yet. Reminders, reviews, and farm messages will appear here.",
            "Wala pang abiso. Dito lalabas ang mga paalala, pagsusuri, at mensahe ng taniman."))
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.id }) { n ->
                val (icon, fg, bg) = typeIcon(n.type)
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (n.read) Color.White else MintGreen)
                        .clickable {
                            vm.markRead(n)
                            if (n.scanUuid.isNotBlank()) scope.launch { vm.scanIdByUuid(n.scanUuid)?.let(onOpenScan) }
                        }.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) { Icon(icon, null, tint = fg) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(n.title, fontWeight = if (n.read) FontWeight.SemiBold else FontWeight.Bold)
                        Text(n.body, style = MaterialTheme.typography.bodyMedium)
                        Text(timeAgo(n.createdAt, l), style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    if (!n.read) Box(Modifier.padding(top = 6.dp).size(10.dp).clip(CircleShape).background(AlertRed))
                }
            }
        }
    }
}

/** messages from the farm owner or technician to the whole farm */
@Composable
fun AnnouncementsScreen(vm: AppViewModel, me: AccountEntity, onBack: () -> Unit) {
    val list by vm.announcements.collectAsStateWithLifecycle()
    val l = lang()
    val canPost = me.roleEnum == Role.OWNER || me.roleEnum == Role.TECHNICIAN || me.roleEnum == Role.ADMIN
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(tr("Announcements", "Mga anunsiyo"), onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (canPost) item {
                SectionCard(tr("Send to everyone in the farm", "Ipadala sa lahat sa taniman")) {
                    OutlinedTextField(title, { title = it }, label = { Text(tr("Title (e.g. Spraying in Block B tomorrow)", "Pamagat (hal. Mag-i-spray sa Block B bukas)")) },
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(body, { body = it }, label = { Text(tr("Message", "Mensahe")) }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { vm.postAnnouncement(title, body) { title = ""; body = "" } }, enabled = title.isNotBlank() && !vm.busy, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Send, null); Spacer(Modifier.width(6.dp)); Text(tr("Send announcement", "Ipadala ang anunsiyo"))
                    }
                    Text(tr("Every member receives it as a notification.", "Matatanggap ito ng bawat kasapi bilang abiso."), style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
            if (list.isEmpty()) item { EmptyState(Icons.Filled.Campaign, tr("No announcements yet.", "Wala pang anunsiyo.")) }
            items(list, key = { it.id }) { a ->
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(SoftPlum), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Campaign, null, tint = Plum) }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(a.title, fontWeight = FontWeight.Bold)
                            Text("${a.fromName} • ${Role.of(a.fromRole).label(l)} • ${timeAgo(a.createdAt, l)}", style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                    }
                    if (a.body.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(a.body) }
                }
            }
        }
    }
}

/** owner: approve workers and set their roles; administrator: manage every account */
@Composable
fun TeamScreen(vm: AppViewModel, me: AccountEntity, onBack: () -> Unit) {
    val members by vm.members.collectAsStateWithLifecycle()
    val l = lang()
    val admin = me.roleEnum == Role.ADMIN
    val pending = members.filter { it.status == AccountStatus.PENDING }
    val others = members.filter { it.status != AccountStatus.PENDING }
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(if (admin) tr("User accounts", "Mga account") else tr("Team", "Mga kasapi"), onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("${members.size}", tr("Accounts", "Account"), LeafGreen, Modifier.weight(1f))
                    StatTile("${pending.size}", tr("Waiting approval", "Naghihintay"), DarkGold, Modifier.weight(1f))
                    StatTile("${members.count { it.status == AccountStatus.DISABLED }}", tr("Disabled", "Hindi aktibo"), AlertRed, Modifier.weight(1f))
                }
            }
            if (!admin) item {
                InfoBanner(tr("Share your farm code ", "Ibahagi ang farm code ") + me.farmCode +
                    tr(" with your workers. They will appear here so you can approve them.", " sa mga manggagawa. Lalabas sila rito para maaprubahan."), SkyBlue, SoftBlue)
            }
            if (pending.isNotEmpty()) item { Text(tr("Waiting for approval", "Naghihintay ng pag-apruba"), fontWeight = FontWeight.Bold) }
            items(pending, key = { "p" + it.uid }) { m -> MemberCard(vm, me, m, l, admin) }
            item { Text(tr("Members", "Mga kasapi"), fontWeight = FontWeight.Bold) }
            items(others, key = { it.uid }) { m -> MemberCard(vm, me, m, l, admin) }
        }
    }
}

@Composable
private fun MemberCard(vm: AppViewModel, me: AccountEntity, m: AccountEntity, l: String, admin: Boolean) {
    var menu by remember { mutableStateOf(false) }
    val roles = if (admin) Role.entries.toList() else listOf(Role.FARMER, Role.VENDOR, Role.TECHNICIAN)
    val self = m.uid == me.uid
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(m.photoUrl, 46.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(m.name.ifBlank { m.email } + if (self) tr(" (you)", " (ikaw)") else "", fontWeight = FontWeight.Bold)
                Text(m.email, style = MaterialTheme.typography.bodySmall, color = Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                    Pill(m.roleEnum.label(l), LeafGreen, SoftGreen)
                    when (m.status) {
                        AccountStatus.PENDING -> Pill(tr("Pending", "Naghihintay"), DarkGold, SoftGold)
                        AccountStatus.DISABLED -> Pill(tr("Disabled", "Hindi aktibo"), AlertRed, SoftRed)
                        else -> {}
                    }
                    if (admin && m.farmCode.isNotBlank()) Pill(m.farmCode, SkyBlue, SoftBlue)
                }
            }
            val canManage = !self && (admin || (m.roleEnum != Role.OWNER && m.roleEnum != Role.ADMIN))
            if (canManage) Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, null) }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    roles.filter { it != m.roleEnum }.forEach { r ->
                        DropdownMenuItem(text = { Text(tr("Make ", "Gawing ") + r.label(l)) }, onClick = { menu = false; vm.changeRole(m, r) })
                    }
                    if (m.status != AccountStatus.DISABLED) DropdownMenuItem(text = { Text(tr("Disable account", "I-disable ang account"), color = AlertRed) },
                        onClick = { menu = false; vm.setAccountStatus(m, AccountStatus.DISABLED) })
                    else DropdownMenuItem(text = { Text(tr("Enable account", "I-enable ang account")) }, onClick = { menu = false; vm.setAccountStatus(m, AccountStatus.ACTIVE) })
                }
            }
        }
        val details = listOfNotNull(
            GENDERS.firstOrNull { it.first == m.gender }?.let { if (l == "tl") it.second else it.first },
            m.ageRange.ifBlank { null }?.let { tr("Age ", "Edad ") + it },
            FARMING_YEARS.firstOrNull { it.first == m.yearsFarming }?.let { if (l == "tl") it.second else it.first },
            m.location.ifBlank { null },
        )
        if (details.isNotEmpty()) Text(details.joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = Muted, modifier = Modifier.padding(top = 6.dp))
        if (m.status == AccountStatus.PENDING && !self) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.approve(m) }, modifier = Modifier.weight(1f)) { Text(tr("Approve", "Aprubahan")) }
                OutlinedButton(onClick = { vm.setAccountStatus(m, AccountStatus.DISABLED) }, modifier = Modifier.weight(1f)) { Text(tr("Decline", "Tanggihan")) }
            }
        }
    }
}

/** images confirmed by technicians, grouped by condition (used to retrain the model) */
@Composable
fun DatasetScreen(vm: AppViewModel, me: AccountEntity, onBack: () -> Unit) {
    val datasetItems by vm.dataset.collectAsStateWithLifecycle()
    val l = lang()
    val info = vm.modelInfo
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(tr("Image dataset", "Dataset ng larawan"), onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                SectionCard(tr("Confirmed images per condition", "Kumpirmadong larawan bawat kondisyon")) {
                    PineappleClass.modelOrder.forEach { c ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                            Text(c.title(l), Modifier.weight(1f), color = classColor(c))
                            Text("${datasetItems.count { it.label == c.label }}", fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                    Row { Text(tr("Total", "Kabuuan"), Modifier.weight(1f), fontWeight = FontWeight.Bold); Text("${datasetItems.size}", fontWeight = FontWeight.Bold) }
                }
            }
            if (me.roleEnum == Role.ADMIN) item {
                SectionCard(tr("Model", "Model")) {
                    Text("MobileNetV2 • TensorFlow Lite • 224 × 224 × 3")
                    Text(tr("Version: ", "Bersiyon: ") + info.version)
                    Text(tr("Test accuracy: ", "Katumpakan sa test: ") + info.testAccuracy)
                    Text(tr("Classes: ", "Mga klase: ") + info.classes.joinToString(), style = MaterialTheme.typography.bodySmall, color = Muted)
                    Spacer(Modifier.height(6.dp))
                    Text(tr("To retrain, export the dataset from the web dashboard and run the training notebook (model_training/Go_Ananas_Training.ipynb).",
                        "Para muling sanayin, i-export ang dataset sa web dashboard at patakbuhin ang training notebook (model_training/Go_Ananas_Training.ipynb)."),
                        style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
            if (datasetItems.isEmpty()) item { EmptyState(Icons.Filled.Collections, tr("No images yet. Technicians add reviewed scans from the scan details screen.",
                "Wala pang larawan. Idinadagdag ng technician ang mga nasuring scan mula sa detalye ng scan.")) }
            items(datasetItems.chunked(3)) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { d ->
                        Column(Modifier.weight(1f)) {
                            val model: Any? = if (d.imagePath.isNotBlank() && java.io.File(d.imagePath).exists()) java.io.File(d.imagePath) else d.imageUrl.ifBlank { null }
                            AsyncImage(model, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(SoftGreen))
                            Text(PineappleClass.fromLabel(d.label).title(l), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** vendor: groups of fruits checked together, with accepted and rejected counts */
@Composable
fun BatchesScreen(vm: AppViewModel, onBack: () -> Unit, onScan: () -> Unit, onOpenScan: (Long) -> Unit) {
    val batches by vm.batches.collectAsStateWithLifecycle()
    val scans by vm.myScans.collectAsStateWithLifecycle()
    val l = lang()
    var name by remember { mutableStateOf("") }
    var open by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(tr("Fruit batches", "Mga batch ng bunga"), onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                if (vm.activeBatch.isBlank()) {
                    SectionCard(tr("Start a new batch", "Magsimula ng bagong batch")) {
                        Text(tr("Use a batch for one delivery or one supplier. Every fruit you check is counted as accepted (healthy) or rejected.",
                            "Gumamit ng batch para sa isang deliver o supplier. Bibilangin ang bawat bunga bilang tinanggap (malusog) o tinanggihan."), color = Muted)
                        OutlinedTextField(name, { name = it }, label = { Text(tr("Batch name (e.g. Supplier A, Nov 6)", "Pangalan ng batch (hal. Supplier A, Nob 6)")) },
                            singleLine = true, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.startBatch(name); name = "" }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Filled.Inventory, null); Spacer(Modifier.width(6.dp)); Text(tr("Start batch", "Simulan ang batch")) }
                    }
                } else {
                    val b = batches.firstOrNull { it.uuid == vm.activeBatch }
                    val inBatch = scans.filter { it.batchId == vm.activeBatch }
                    SectionCard(tr("Open batch: ", "Bukas na batch: ") + (b?.name ?: "")) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatTile("${inBatch.count { it.pineappleClass.isHealthy }}", tr("Accepted", "Tinanggap"), FreshGreen, Modifier.weight(1f))
                            StatTile("${inBatch.count { !it.pineappleClass.isHealthy }}", tr("Rejected", "Tinanggihan"), AlertRed, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onScan, modifier = Modifier.fillMaxWidth()) { Text(tr("Check the next fruit", "Suriin ang susunod na bunga")) }
                        OutlinedButton(onClick = { vm.endBatch() }, modifier = Modifier.fillMaxWidth()) { Text(tr("Close batch", "Isara ang batch")) }
                    }
                }
            }
            if (batches.isEmpty()) item { EmptyState(Icons.Filled.Inventory, tr("No batches yet.", "Wala pang batch.")) }
            items(batches, key = { it.uuid }) { b ->
                val inBatch = scans.filter { it.batchId == b.uuid }
                val ok = inBatch.count { it.pineappleClass.isHealthy }
                SectionCard {
                    Row(Modifier.fillMaxWidth().clickable { open = if (open == b.uuid) "" else b.uuid }, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(b.name, fontWeight = FontWeight.Bold)
                            Text(dateOnly(b.createdAt) + " • ${inBatch.size} " + tr("fruits", "bunga"), style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                        Pill("$ok ✓", LeafGreen, SoftGreen); Spacer(Modifier.width(6.dp)); Pill("${inBatch.size - ok} ✗", AlertRed, SoftRed)
                    }
                    if (open == b.uuid) {
                        inBatch.forEach { ScanRow(it) { onOpenScan(it.id) } }
                        TextButton(onClick = { vm.deleteBatch(b.uuid) }) { Text(tr("Delete batch", "Burahin ang batch"), color = AlertRed) }
                    }
                }
            }
        }
    }
}

/** profile, language, notifications, cloud status, and sign out */
@Composable
fun ProfileScreen(vm: AppViewModel, me: AccountEntity, onEdit: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val myScans by vm.myScans.collectAsStateWithLifecycle()
    val l = lang()
    var confirmLogout by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        GradientHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(me.photoUrl, 72.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(me.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(me.roleEnum.label(l), color = Color.White.copy(alpha = 0.9f))
                    Text(me.email, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, tr("Edit", "I-edit"), tint = Color.White) }
            }
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("${myScans.size}", tr("My scans", "Aking scan"), LeafGreen, Modifier.weight(1f))
                StatTile("${myScans.count { it.status == ScanStatus.OPEN }}", tr("Need action", "Aaksyunan"), AlertRed, Modifier.weight(1f))
                StatTile(dateOnly(me.createdAt).substringBefore(","), tr("Member since", "Kasapi mula"), SkyBlue, Modifier.weight(1f))
            }
            SectionCard(tr("About me", "Tungkol sa akin")) {
                InfoRow(tr("Farm", "Taniman"), "${me.farmName.ifBlank { me.farmCode }} (${me.farmCode})")
                InfoRow(tr("Sex", "Kasarian"), GENDERS.firstOrNull { it.first == me.gender }?.let { if (l == "tl") it.second else it.first } ?: "—")
                InfoRow(tr("Age", "Edad"), me.ageRange.ifBlank { "—" })
                InfoRow(tr("Experience", "Karanasan"), FARMING_YEARS.firstOrNull { it.first == me.yearsFarming }?.let { if (l == "tl") it.second else it.first } ?: "—")
                InfoRow(tr("Location", "Lugar"), me.location.ifBlank { "—" })
                if (me.phone.isNotBlank()) InfoRow(tr("Mobile", "Cellphone"), me.phone)
                InfoRow(tr("Account", "Account"), when (me.status) {
                    AccountStatus.PENDING -> tr("Waiting for approval", "Naghihintay ng pag-apruba")
                    AccountStatus.DISABLED -> tr("Disabled", "Hindi aktibo")
                    else -> tr("Active", "Aktibo")
                })
            }
            SectionCard(tr("Language", "Wika")) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("en" to "English", "tl" to "Tagalog").forEach { (code, label) ->
                        if (l == code) Button(onClick = {}, modifier = Modifier.weight(1f)) { Text(label) }
                        else OutlinedButton(onClick = { vm.setLanguage(code) }, modifier = Modifier.weight(1f)) { Text(label) }
                    }
                }
            }
            SectionCard(tr("Notifications", "Mga abiso")) {
                ToggleRow(tr("Re-check reminders for diseased plants", "Paalala sa muling pagsusuri ng may sakit"), settings?.recheckReminders ?: true) { vm.setRecheck(it) }
                ToggleRow(tr("Monthly inspection reminder", "Buwanang paalala sa pagsusuri"), settings?.monthlyReminder ?: true) { vm.setMonthly(it) }
                ToggleRow(tr("Outbreak alert (3 or more cases in one block within 14 days)", "Babala sa pagkalat (3 o higit sa isang block sa loob ng 14 araw)"), settings?.outbreakAlerts ?: true) { vm.setOutbreak(it) }
            }
            SectionCard(tr("Cloud and web dashboard", "Cloud at web dashboard")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (vm.cloudEnabled) Icons.Filled.Cloud else Icons.Filled.CloudOff, null, tint = if (vm.cloudEnabled) LeafGreen else Muted)
                    Spacer(Modifier.width(10.dp))
                    Text(if (vm.cloudEnabled) tr("Connected. Your records are shared with your farm and the web dashboard in real time.",
                        "Naka-konekta. Ibinabahagi nang real-time ang talaan sa taniman at sa web dashboard.")
                    else tr("Offline mode. Records stay on this phone until the app is connected to Firebase.",
                        "Offline mode. Mananatili sa phone ang talaan hanggang maikonekta sa Firebase."), Modifier.weight(1f))
                }
                if (vm.cloudEnabled) OutlinedButton(onClick = { vm.syncNow() }, modifier = Modifier.padding(top = 8.dp)) { Text(tr("Sync now", "I-sync ngayon")) }
            }
            SectionCard(tr("Help", "Tulong")) {
                TextButton(onClick = { vm.showTutorialAgain() }) { Icon(Icons.Filled.School, null); Spacer(Modifier.width(6.dp)); Text(tr("Show the tutorial again", "Ipakita muli ang tutorial")) }
                Text("Go Ananas! ${BuildConfig.VERSION_NAME} • BS Computer Engineering, Asia Technological School of Science and Arts",
                    style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            Button(onClick = { confirmLogout = true }, modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed)) {
                Icon(Icons.AutoMirrored.Filled.Logout, null); Spacer(Modifier.width(8.dp)); Text(tr("Log out", "Mag-log out"))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text(tr("Log out?", "Mag-log out?")) },
            text = { Text(tr("Your saved records stay on this phone and in the cloud.", "Mananatili ang iyong talaan sa phone at sa cloud.")) },
            confirmButton = { TextButton(onClick = { confirmLogout = false; vm.logout() }) { Text(tr("Log out", "Mag-log out"), color = AlertRed) } },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text(tr("Cancel", "Kanselahin")) } },
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, Modifier.width(110.dp), color = Muted)
        Text(value, Modifier.weight(1f), fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}


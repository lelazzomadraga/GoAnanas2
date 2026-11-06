package ph.edu.asiatech.goananas.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ph.edu.asiatech.goananas.data.AccountEntity
import ph.edu.asiatech.goananas.data.PineappleClass
import ph.edu.asiatech.goananas.data.ReviewStatus
import ph.edu.asiatech.goananas.data.Role
import ph.edu.asiatech.goananas.data.ScanEntity
import ph.edu.asiatech.goananas.data.ScanStatus
import ph.edu.asiatech.goananas.ui.AppViewModel
import ph.edu.asiatech.goananas.ui.ChoiceChips
import ph.edu.asiatech.goananas.ui.EmptyState
import ph.edu.asiatech.goananas.ui.InfoBanner
import ph.edu.asiatech.goananas.ui.ProbabilityBars
import ph.edu.asiatech.goananas.ui.ReviewBadge
import ph.edu.asiatech.goananas.ui.ScanRow
import ph.edu.asiatech.goananas.ui.SectionCard
import ph.edu.asiatech.goananas.ui.StatTile
import ph.edu.asiatech.goananas.ui.StatusBadge
import ph.edu.asiatech.goananas.ui.classColor
import ph.edu.asiatech.goananas.ui.dateOnly
import ph.edu.asiatech.goananas.ui.dateTime
import ph.edu.asiatech.goananas.ui.lang
import ph.edu.asiatech.goananas.ui.pct
import ph.edu.asiatech.goananas.ui.theme.AlertRed
import ph.edu.asiatech.goananas.ui.theme.DarkGold
import ph.edu.asiatech.goananas.ui.theme.SoftGold
import ph.edu.asiatech.goananas.ui.theme.FreshGreen
import ph.edu.asiatech.goananas.ui.theme.LeafGreen
import ph.edu.asiatech.goananas.ui.theme.Muted
import ph.edu.asiatech.goananas.ui.theme.PineGold
import ph.edu.asiatech.goananas.ui.theme.SkyBlue
import ph.edu.asiatech.goananas.ui.theme.SoftBlue
import ph.edu.asiatech.goananas.ui.theme.SoftGreen
import ph.edu.asiatech.goananas.ui.tr
import java.util.concurrent.TimeUnit

/** list of scan records with search and filters */
@Composable
fun HistoryScreen(vm: AppViewModel, me: AccountEntity, initialFilter: String, onOpenScan: (Long) -> Unit) {
    val scans by vm.scans.collectAsStateWithLifecycle()
    val l = lang()
    var query by remember { mutableStateOf("") }
    var filter by remember(initialFilter) { mutableStateOf(initialFilter) }
    val shown = scans.filter { s ->
        when (filter) {
            "open" -> s.status == ScanStatus.OPEN
            "review" -> s.reviewStatus == ReviewStatus.PENDING
            "all" -> true
            else -> s.finalLabel == filter
        } && (query.isBlank() || listOf(s.block, s.notes, s.userName, s.pineappleClass.title(l), s.plantPart).any { it.contains(query, ignoreCase = true) })
    }
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(if (me.roleEnum.seesFarm) tr("Farm records", "Talaan ng taniman") else tr("My records", "Aking talaan"))
        OutlinedTextField(query, { query = it }, leadingIcon = { Icon(Icons.Filled.Search, null) }, singleLine = true,
            placeholder = { Text(tr("Search block, notes, name, or disease", "Hanapin ang block, tala, pangalan, o sakit")) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(14.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(filter == "all", { filter = "all" }, { Text(tr("All", "Lahat")) })
            FilterChip(filter == "open", { filter = "open" }, { Text(tr("Needs action", "Kailangang aksyunan")) })
            FilterChip(filter == "review", { filter = "review" }, { Text(tr("For review", "Susuriin")) })
            PineappleClass.modelOrder.forEach { c -> FilterChip(filter == c.label, { filter = if (filter == c.label) "all" else c.label }, { Text(c.title(l)) }) }
        }
        Text("${shown.size} / ${scans.size} " + tr("records", "talaan"), Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall, color = Muted)
        if (shown.isEmpty()) EmptyState(Icons.Filled.PhotoLibrary, tr("No records to show.", "Walang talaang maipapakita."))
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            items(shown, key = { it.id }) { s ->
                ScanRow(s, showUser = me.roleEnum.seesFarm) { onOpenScan(s.id) }
                HorizontalDivider()
            }
        }
    }
}

/** details of one scan; technicians and owners can review it here */
@Composable
fun ScanDetailScreen(vm: AppViewModel, me: AccountEntity, id: Long, onBack: () -> Unit, onGuide: (String) -> Unit) {
    val flow = remember(id) { vm.scan(id) }
    val scan by flow.collectAsStateWithLifecycle(initialValue = null)
    val l = lang()
    var notes by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    var reviewLabel by remember { mutableStateOf("") }
    var advice by remember { mutableStateOf("") }
    LaunchedEffect(scan?.id) { scan?.let { notes = it.notes; reviewLabel = it.finalLabel; advice = it.advice } }
    val s = scan
    val mine = s != null && s.userId == me.uid
    val canReview = me.roleEnum.canReview

    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(tr("Scan details", "Detalye ng scan"), onBack = onBack, actions = {
            if (mine || me.roleEnum == Role.ADMIN) IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, "Delete") }
        })
        if (s == null) {
            Text(tr("This scan was deleted.", "Nabura na ang scan na ito."), Modifier.padding(16.dp))
        } else {
            val c = s.pineappleClass
            Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AsyncImage(s.imageModel, contentDescription = c.title(l), contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.2f).clip(RoundedCornerShape(20.dp)).background(SoftGreen))
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(c.title(l), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = classColor(c))
                        if (s.confirmedLabel.isNotBlank() && s.confirmedLabel != s.predictedLabel) {
                            Text(tr("App result: ", "Resulta ng app: ") + PineappleClass.fromLabel(s.predictedLabel).title(l), style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                        Text("${pct(s.confidence)} ${tr("confidence", "kumpiyansa")} • ${s.plantPart}${if (s.block.isNotBlank()) " • ${s.block}" else ""}")
                        Text(dateTime(s.createdAt) + (if (s.userName.isNotBlank()) " • ${s.userName}" else ""), style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) { StatusBadge(s.status); ReviewBadge(s.reviewStatus) }
                }
                if (s.recheckAt != null && s.status == ScanStatus.OPEN) {
                    InfoBanner(tr("Re-check on ", "Suriin muli sa ") + dateOnly(s.recheckAt), DarkGold, SoftGold)
                }
                if (s.reviewStatus == ReviewStatus.CONFIRMED || s.reviewStatus == ReviewStatus.CORRECTED) {
                    SectionCard(tr("Technician's review", "Pagsusuri ng technician")) {
                        Text((if (s.reviewStatus == ReviewStatus.CONFIRMED) tr("Confirmed by ", "Kinumpirma ni ") else tr("Corrected by ", "Itinama ni ")) + s.reviewerName,
                            fontWeight = FontWeight.SemiBold)
                        if (s.advice.isNotBlank()) Text(s.advice)
                        s.reviewedAt?.let { Text(dateTime(it), style = MaterialTheme.typography.bodySmall, color = Muted) }
                    }
                }
                SectionCard(tr("What to do", "Ano ang gagawin")) {
                    Text(c.action(l))
                    TextButton(onClick = { onGuide(c.label) }) { Text(tr("Disease guide", "Gabay sa sakit")) }
                }
                SectionCard(tr("Confidence of each condition", "Kumpiyansa sa bawat kondisyon")) {
                    ProbabilityBars(PineappleClass.modelOrder.zip(s.probabilityList).sortedByDescending { it.second })
                }
                if (canReview) {
                    SectionCard(tr("Review this scan", "Suriin ang scan na ito"), trailing = { Icon(Icons.Filled.RateReview, null, tint = SkyBlue) }) {
                        Text(tr("Is the result correct? Choose the right condition.", "Tama ba ang resulta? Piliin ang tamang kondisyon."), color = Muted)
                        Spacer(Modifier.height(6.dp))
                        ChoiceChips(PineappleClass.modelOrder.map { it.label to it.title(l) }, reviewLabel) { reviewLabel = it }
                        OutlinedTextField(advice, { advice = it }, label = { Text(tr("Advice to the farmer", "Payo sa magsasaka")) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        Spacer(Modifier.height(6.dp))
                        Button(onClick = { vm.review(s.id, reviewLabel, advice) {} }, enabled = reviewLabel.isNotBlank() && !vm.busy, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.DoneAll, null); Spacer(Modifier.width(6.dp))
                            Text(if (reviewLabel == s.predictedLabel) tr("Confirm result", "Kumpirmahin") else tr("Save correction", "I-save ang pagtatama"))
                        }
                        if (me.roleEnum == Role.TECHNICIAN || me.roleEnum == Role.ADMIN) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                                Column(Modifier.weight(1f)) {
                                    Text(tr("Add to training dataset", "Idagdag sa dataset"), fontWeight = FontWeight.SemiBold)
                                    Text(tr("Use this reviewed image to retrain the model", "Gamitin ang larawang ito sa muling pagsasanay ng model"), style = MaterialTheme.typography.bodySmall, color = Muted)
                                }
                                Switch(checked = s.inDataset, onCheckedChange = { vm.setInDataset(s.id, it) },
                                    enabled = s.reviewStatus == ReviewStatus.CONFIRMED || s.reviewStatus == ReviewStatus.CORRECTED)
                            }
                        }
                    }
                }
                if (!c.isHealthy && (mine || canReview)) {
                    SectionCard(tr("Update status", "I-update ang kalagayan")) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { vm.setStatus(s.id, ScanStatus.RECHECKED) }, Modifier.weight(1f)) { Text(tr("Rechecked", "Nasuri muli")) }
                            Button(onClick = { vm.setStatus(s.id, ScanStatus.RESOLVED) }, Modifier.weight(1f)) { Text(tr("Resolved", "Naayos na")) }
                        }
                        if (s.status != ScanStatus.OPEN) TextButton(onClick = { vm.setStatus(s.id, ScanStatus.OPEN) }) { Text(tr("Reopen", "Buksan muli")) }
                    }
                }
                if (mine) {
                    SectionCard(tr("Notes", "Tala")) {
                        OutlinedTextField(notes, { notes = it }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(onClick = { vm.updateNotes(s.id, notes) }, enabled = notes != s.notes) { Text(tr("Save notes", "I-save ang tala")) }
                    }
                } else if (s.notes.isNotBlank()) {
                    SectionCard(tr("Notes", "Tala")) { Text(s.notes) }
                }
                Text("${tr("Processing time", "Tagal ng pagsusuri")}: ${s.inferenceMs} ms • " +
                    (if (s.synced) tr("Saved in the cloud", "Naka-save sa cloud") else tr("Saved on this phone", "Naka-save sa phone")),
                    style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(tr("Delete this scan?", "Burahin ang scan na ito?")) },
            text = { Text(tr("The photo and result will be removed.", "Mabubura ang larawan at resulta.")) },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.delete(id); onBack() }) { Text(tr("Delete", "Burahin"), color = AlertRed) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(tr("Cancel", "Kanselahin")) } },
        )
    }
}

/** scans waiting for the technician's review (diseased or low-confidence results) */
@Composable
fun ReviewQueueScreen(vm: AppViewModel, me: AccountEntity, onBack: () -> Unit, onOpenScan: (Long) -> Unit) {
    val scans by vm.scans.collectAsStateWithLifecycle()
    val queue = scans.filter { it.reviewStatus == ReviewStatus.PENDING }.sortedBy { it.createdAt }
    val done = scans.filter { it.reviewStatus == ReviewStatus.CONFIRMED || it.reviewStatus == ReviewStatus.CORRECTED }
    val agree = if (done.isEmpty()) 0f else done.count { it.reviewStatus == ReviewStatus.CONFIRMED }.toFloat() / done.size
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(tr("Review scans", "Suriin ang mga scan"), onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("${queue.size}", tr("Waiting", "Naghihintay"), SkyBlue, Modifier.weight(1f))
                    StatTile("${done.size}", tr("Reviewed", "Nasuri na"), LeafGreen, Modifier.weight(1f))
                    StatTile(if (done.isEmpty()) "—" else pct(agree), tr("App agreed", "Tama ang app"), FreshGreen, Modifier.weight(1f))
                }
            }
            item {
                InfoBanner(tr("Open a scan, check the photo, then confirm or correct the result and add advice. The farmer is notified right away.",
                    "Buksan ang scan, tingnan ang larawan, saka kumpirmahin o itama ang resulta at magdagdag ng payo. Aabisuhan agad ang magsasaka."), SkyBlue, SoftBlue, Icons.Filled.RateReview)
            }
            if (queue.isEmpty()) item { EmptyState(Icons.Filled.DoneAll, tr("All scans are reviewed. Great work!", "Nasuri na lahat ng scan. Magaling!")) }
            items(queue, key = { it.id }) { s -> ScanRow(s, showUser = true) { onOpenScan(s.id) } }
        }
    }
}

/** farm dashboard: totals, conditions, weekly scans, affected blocks, workers */
@Composable
fun DashboardScreen(vm: AppViewModel, me: AccountEntity, onBack: () -> Unit, onOpenScan: (Long) -> Unit) {
    val all by vm.scans.collectAsStateWithLifecycle()
    val l = lang()
    val context = LocalContext.current
    val scans = all.filter { it.batchId.isBlank() }
    val total = scans.size
    val healthy = scans.count { it.pineappleClass.isHealthy }
    val perClass = PineappleClass.modelOrder.map { c -> c to scans.count { it.finalLabel == c.label } }
    val blocks = scans.filter { !it.pineappleClass.isHealthy && it.block.isNotBlank() }.groupingBy { it.block }.eachCount().entries.sortedByDescending { it.value }
    val people = scans.filter { it.userName.isNotBlank() }.groupingBy { it.userName }.eachCount().entries.sortedByDescending { it.value }
    val now = System.currentTimeMillis()
    val week = TimeUnit.DAYS.toMillis(7)
    val weekly = (7 downTo 0).map { w -> scans.count { it.createdAt > now - (w + 1) * week && it.createdAt <= now - w * week } }
    val due = scans.filter { it.status == ScanStatus.OPEN && it.recheckAt != null }.sortedBy { it.recheckAt }

    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(when (me.roleEnum) {
            Role.ADMIN -> tr("All farms", "Lahat ng taniman")
            Role.FARMER -> tr("My summary", "Buod ko")
            else -> tr("Farm dashboard", "Dashboard ng taniman")
        }, onBack = onBack, actions = {
            IconButton(onClick = { shareCsv(context, scans, l) }) { Icon(Icons.Filled.Share, tr("Export", "I-export")) }
        })
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("$total", tr("Plants scanned", "Na-scan"), LeafGreen, Modifier.weight(1f))
                    StatTile(if (total == 0) "—" else "${healthy * 100 / total}%", tr("Healthy", "Malusog"), FreshGreen, Modifier.weight(1f))
                    StatTile("${total - healthy}", tr("With disease", "May sakit"), AlertRed, Modifier.weight(1f))
                }
            }
            item {
                SectionCard(tr("Results per condition", "Resulta bawat kondisyon")) {
                    val max = (perClass.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
                    perClass.forEach { (c, n) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(c.title(l), Modifier.width(130.dp), style = MaterialTheme.typography.bodySmall, maxLines = 2)
                            Canvas(Modifier.weight(1f).height(16.dp)) {
                                drawRoundRect(Color(0xFFECEFF1), cornerRadius = CornerRadius(8f, 8f))
                                drawRoundRect(classColor(c), size = Size(size.width * n / max, size.height), cornerRadius = CornerRadius(8f, 8f))
                            }
                            Text("  $n", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            item {
                SectionCard(tr("Scans per week (last 8 weeks)", "Scan bawat linggo (huling 8 linggo)")) {
                    val max = (weekly.maxOrNull() ?: 0).coerceAtLeast(1)
                    Canvas(Modifier.fillMaxWidth().height(110.dp)) {
                        val gap = 10f
                        val w = (size.width - gap * (weekly.size - 1)) / weekly.size
                        weekly.forEachIndexed { i, n ->
                            val h = size.height * n / max
                            drawRoundRect(if (i == weekly.lastIndex) PineGold else LeafGreen, topLeft = Offset(i * (w + gap), size.height - h),
                                size = Size(w, h), cornerRadius = CornerRadius(6f, 6f))
                        }
                    }
                    Text(tr("The gold bar is this week.", "Ang gintong bar ay ang linggong ito."), style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
            item {
                SectionCard(tr("Most affected blocks", "Pinakaapektadong block")) {
                    if (blocks.isEmpty()) Text(tr("No diseased plants recorded with a block yet.", "Wala pang may sakit na naitala na may block."))
                    blocks.take(5).forEachIndexed { i, b ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text("${i + 1}. ${b.key}", Modifier.weight(1f))
                            Text("${b.value} " + tr("diseased", "may sakit"), color = AlertRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            if (me.roleEnum.seesFarm) item {
                SectionCard(tr("Scans per worker", "Scan bawat manggagawa")) {
                    if (people.isEmpty()) Text("—")
                    people.take(6).forEach { p ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) { Text(p.key, Modifier.weight(1f)); Text("${p.value}", fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
            item {
                SectionCard(tr("Plants waiting for re-check", "Halamang susuriin muli")) {
                    if (due.isEmpty()) Text(tr("No plants are waiting for a re-check.", "Walang halamang naghihintay ng muling pagsusuri."))
                    due.take(8).forEach { ScanRow(it, showUser = me.roleEnum.seesFarm) { onOpenScan(it.id) } }
                }
            }
        }
    }
}

/** shares the records as a CSV file (opens in Excel or Google Sheets) */
private fun shareCsv(context: android.content.Context, scans: List<ScanEntity>, l: String) {
    val head = "date,condition,confidence,block,plant_part,scanned_by,status,review,reviewer,notes"
    fun q(s: String) = "\"" + s.replace("\"", "\"\"") + "\""
    val rows = scans.joinToString("\n") { s ->
        listOf(dateTime(s.createdAt), s.pineappleClass.displayName, pct(s.confidence), s.block, s.plantPart, s.userName, s.status, s.reviewStatus, s.reviewerName, s.notes).joinToString(",") { q(it) }
    }
    val dir = java.io.File(context.cacheDir, "exports").apply { mkdirs() }
    val file = java.io.File(dir, "go_ananas_records.csv")
    file.writeText(head + "\n" + rows)
    val uri = androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(android.content.Intent.createChooser(send, if (l == "tl") "I-export ang talaan" else "Export records").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
}

/** list of the six conditions */
@Composable
fun GuideScreen(onBack: () -> Unit, onOpen: (String) -> Unit) {
    val l = lang()
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(tr("Disease guide", "Gabay sa sakit"), onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            item {
                Text(tr("The six conditions recognized by Go Ananas!. Tap one to see its symptoms, cause, and what to do.",
                    "Ang anim na kondisyong nakikilala ng Go Ananas!. Pindutin ang isa para makita ang sintomas, sanhi, at gagawin."), color = Muted)
                Spacer(Modifier.height(8.dp))
            }
            PineappleClass.entries.forEach { c ->
                item {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onOpen(c.label) }.padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(c.title(l), fontWeight = FontWeight.Bold, color = classColor(c))
                            Text("${c.localName} • " + tr("photograph the ", "kunan ang ") + c.part(l).lowercase(), style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                    }
                    HorizontalDivider()
                }
            }
            item {
                Spacer(Modifier.height(12.dp))
                Text(tr("Limitation: the app reads only what is visible on the surface. Internal conditions such as \"kalawang\" cannot be detected; the result is a screening aid, not a laboratory diagnosis.",
                    "Limitasyon: ang nakikita lamang sa labas ang nababasa ng app. Hindi nito matutukoy ang mga sakit sa loob tulad ng \"kalawang\"; gabay lamang ito at hindi pagsusuri sa laboratoryo."),
                    style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
    }
}

@Composable
fun GuideDetailScreen(label: String, onBack: () -> Unit) {
    val c = PineappleClass.fromLabel(label)
    val l = lang()
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(c.title(l), onBack = onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(c.localName, style = MaterialTheme.typography.titleMedium, color = classColor(c))
            SectionCard(tr("What it looks like", "Itsura")) { Text(c.symptoms(l)) }
            SectionCard(tr("Cause", "Sanhi")) { Text(c.cause(l)) }
            SectionCard(tr("What to do", "Ano ang gagawin")) { c.management(l).forEach { Text("• $it") } }
            SectionCard(tr("How to photograph it", "Paano kunan")) {
                Text(tr("Take a clear photo of the ", "Kumuha ng malinaw na larawan ng ") + c.part(l).lowercase() + tr(" from 30 to 60 cm away, in daylight.", " mula 30 hanggang 60 cm, sa liwanag ng araw."))
            }
            Spacer(Modifier.size(8.dp))
        }
    }
}

package ph.edu.asiatech.goananas.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ph.edu.asiatech.goananas.data.AccountEntity
import ph.edu.asiatech.goananas.data.PLANT_PARTS
import ph.edu.asiatech.goananas.ml.ImagePreprocessor
import ph.edu.asiatech.goananas.ml.Rejection
import ph.edu.asiatech.goananas.ui.AnalysisState
import ph.edu.asiatech.goananas.ui.AppViewModel
import ph.edu.asiatech.goananas.ui.InfoBanner
import ph.edu.asiatech.goananas.ui.ProbabilityBars
import ph.edu.asiatech.goananas.ui.SectionCard
import ph.edu.asiatech.goananas.ui.classColor
import ph.edu.asiatech.goananas.ui.lang
import ph.edu.asiatech.goananas.ui.pct
import ph.edu.asiatech.goananas.ui.theme.AlertRed
import ph.edu.asiatech.goananas.ui.theme.DarkGold
import ph.edu.asiatech.goananas.ui.theme.LeafGreen
import ph.edu.asiatech.goananas.ui.theme.Muted
import ph.edu.asiatech.goananas.ui.theme.PineGold
import ph.edu.asiatech.goananas.ui.theme.SkyBlue
import ph.edu.asiatech.goananas.ui.theme.SoftBlue
import ph.edu.asiatech.goananas.ui.theme.SoftGold
import ph.edu.asiatech.goananas.ui.theme.SoftGreen
import ph.edu.asiatech.goananas.ui.theme.SoftRed
import ph.edu.asiatech.goananas.ui.tr
import java.io.File

/** camera screen; mode "fruit" is used by vendors */
@Composable
fun CaptureScreen(vm: AppViewModel, mode: String, onBack: () -> Unit, onAnalyzed: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by vm.analysis.collectAsStateWithLifecycle()
    val l = lang()
    var part by remember { mutableStateOf(if (mode == "fruit") "Fruit" else vm.selectedPart) }
    var flash by remember { mutableStateOf(false) }
    var captureError by remember { mutableStateOf<String?>(null) }
    var hasCamera by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCamera = it }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) { vm.selectedPart = part; vm.analyze(uri) }
    }
    val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build() }
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }

    LaunchedEffect(Unit) { vm.resetAnalysis(); if (!hasCamera) cameraPermission.launch(Manifest.permission.CAMERA) }
    LaunchedEffect(state) { if (state is AnalysisState.Done) onAnalyzed() }
    LaunchedEffect(flash) { imageCapture.flashMode = if (flash) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF }

    DisposableEffect(hasCamera) {
        var provider: ProcessCameraProvider? = null
        if (hasCamera) {
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({
                provider = future.get().also { p ->
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    p.unbindAll()
                    p.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                }
            }, ContextCompat.getMainExecutor(context))
        }
        onDispose { provider?.unbindAll() }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF0E1A12))) {
        SimpleTopBar(if (mode == "fruit") tr("Check a fruit", "Suriin ang bunga") else tr("Scan a pineapple", "Mag-scan ng pinya"), onBack = onBack, actions = {
            IconButton(onClick = { flash = !flash }) { Icon(if (flash) Icons.Filled.FlashOn else Icons.Filled.FlashOff, "Flash") }
        })
        if (mode == "fruit" && vm.activeBatch.isNotBlank()) {
            Row(Modifier.padding(horizontal = 16.dp)) {
                InfoBanner(tr("Scans are added to the open batch.", "Idinadagdag ang scan sa bukas na batch."), SkyBlue, SoftBlue, Icons.Filled.Inventory)
            }
        }
        Text(tr("What part is in the photo?", "Anong bahagi ang nasa larawan?"), Modifier.padding(start = 16.dp, top = 8.dp), color = Color.White, fontWeight = FontWeight.SemiBold)
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PLANT_PARTS.forEach { (en, tl) ->
                FilterChip(selected = part == en, onClick = { part = en; vm.selectedPart = en }, label = { Text(if (l == "tl") tl else en) },
                    colors = FilterChipDefaults.filterChipColors(containerColor = Color(0x33FFFFFF), labelColor = Color.White,
                        selectedContainerColor = PineGold, selectedLabelColor = Color(0xFF3A2A00)))
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth().padding(12.dp).clip(RoundedCornerShape(24.dp)).background(Color.Black), contentAlignment = Alignment.Center) {
            if (hasCamera) {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
                Box(Modifier.size(250.dp).border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(20.dp)))
                Text(tr("Keep the affected part inside the frame", "Ilagay sa loob ng frame ang apektadong bahagi"),
                    color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.45f)).padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.bodySmall)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text(tr("Allow the camera to take photos, or choose a photo from the gallery.", "Payagan ang camera para kumuha ng larawan, o pumili mula sa gallery."),
                        color = Color.White, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { cameraPermission.launch(Manifest.permission.CAMERA) }) { Text(tr("Allow camera", "Payagan ang camera")) }
                }
            }
            if (state is AnalysisState.Working) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color.Black.copy(alpha = 0.7f)).padding(24.dp)) {
                    CircularProgressIndicator(color = PineGold)
                    Spacer(Modifier.height(10.dp))
                    Text(tr("Checking the photo…", "Sinusuri ang larawan…"), color = Color.White)
                }
            }
        }
        val failed = state as? AnalysisState.Failed
        val err = if (failed != null) tr("No image found. The file could not be opened; choose another photo.", "Walang nakitang larawan. Hindi mabuksan ang file; pumili ng ibang larawan.") else captureError
        if (err != null) Row(Modifier.padding(horizontal = 16.dp)) { InfoBanner(err, AlertRed, SoftRed, Icons.Filled.ErrorOutline) }
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                Icon(Icons.Filled.PhotoLibrary, null); Spacer(Modifier.size(6.dp)); Text(tr("Gallery", "Gallery"))
            }
            Box(Modifier.size(80.dp).clip(CircleShape).background(PineGold), contentAlignment = Alignment.Center) {
                IconButton(
                    enabled = hasCamera && state !is AnalysisState.Working,
                    onClick = {
                        captureError = null
                        val dir = File(context.cacheDir, "captures").apply { mkdirs() }
                        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
                        imageCapture.takePicture(
                            ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) { vm.selectedPart = part; vm.analyze(Uri.fromFile(file)) }
                                override fun onError(exception: ImageCaptureException) {
                                    captureError = if (l == "tl") "Hindi nakuha ang larawan. Subukan muli." else "The photo could not be taken. Please try again."
                                }
                            },
                        )
                    },
                    modifier = Modifier.size(80.dp),
                ) { Box(Modifier.size(62.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) { Icon(Icons.Filled.CameraAlt, null, tint = LeafGreen) } }
            }
            Spacer(Modifier.size(width = 100.dp, height = 1.dp))
        }
    }
}

/** result screen: the diagnosis, or an error when the photo does not show a pineapple */
@Composable
fun ResultScreen(vm: AppViewModel, me: AccountEntity, onBack: () -> Unit, onSaved: (Long) -> Unit, onGuide: (String) -> Unit) {
    val state by vm.analysis.collectAsStateWithLifecycle()
    val scans by vm.myScans.collectAsStateWithLifecycle()
    val blocks = remember(scans) { scans.map { it.block }.filter { it.isNotBlank() }.distinct().take(8) }
    val done = state as? AnalysisState.Done
    val l = lang()
    var showEnhanced by remember { mutableStateOf(false) }
    var block by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    BackHandler(onBack = onBack)

    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(tr("Result", "Resulta"), onBack = onBack)
        if (done == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(tr("No photo to show.", "Walang larawang maipapakita.")) }
        } else {
        val d = done.diagnosis
        val c = d.top
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(
                bitmap = (if (showEnhanced) d.enhancedPreview else done.photo).asImageBitmap(), contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().aspectRatio(1.2f).clip(RoundedCornerShape(20.dp)),
            )

            if (d.rejection != null) {
                // error scenario: the photo has no pineapple, or the result is too uncertain
                val notPine = d.rejection == Rejection.NOT_PINEAPPLE
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(SoftRed).padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(if (notPine) Icons.Filled.ErrorOutline else Icons.Filled.HelpOutline, null, tint = AlertRed, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(6.dp))
                    Text(if (notPine) tr("No pineapple detected", "Walang nakitang pinya") else tr("The result is not clear", "Hindi malinaw ang resulta"),
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AlertRed, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text(if (notPine) tr("This photo does not seem to show a pineapple plant or fruit, so it cannot be diagnosed. Take a photo of a pineapple leaf, crown, heart, base, or fruit.",
                        "Mukhang walang halaman o bunga ng pinya sa larawang ito, kaya hindi ito masusuri. Kunan ang dahon, korona, puso, puno, o bunga ng pinya.")
                    else tr("The app is not sure what this is. Move closer to the affected part, use daylight, and try again.",
                        "Hindi sigurado ang app kung ano ito. Lumapit sa apektadong bahagi, gumamit ng liwanag ng araw, at subukan muli."),
                        textAlign = TextAlign.Center)
                }
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Filled.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text(tr("Take another photo", "Kumuha ng panibagong larawan"))
                }
            } else {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Show the enhanced image (CLAHE) used by the model", "Ipakita ang pinalinaw na larawan (CLAHE)"), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                Switch(checked = showEnhanced, onCheckedChange = { showEnhanced = it })
            }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(if (c.isHealthy) SoftGreen else SoftRed).padding(18.dp)) {
                Text(if (c.isHealthy) tr("Status: Optimal", "Kalagayan: Maayos") else tr("Disease or disorder detected", "May nakitang sakit o diperensiya"),
                    color = classColor(c), fontWeight = FontWeight.SemiBold)
                Text(c.title(l), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = classColor(c))
                Text("${pct(d.confidence)} ${tr("confidence", "kumpiyansa")} • ${c.localName}", style = MaterialTheme.typography.bodyMedium)
                Text(tr("Checked on the phone in ", "Nasuri sa phone sa loob ng ") + "${d.inferenceMs} ms", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            if (d.lowConfidence || d.qualityWarnings.isNotEmpty()) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SoftGold).padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.WarningAmber, null, tint = DarkGold); Spacer(Modifier.width(8.dp))
                        Text(tr("Check the photo", "Suriin ang larawan"), fontWeight = FontWeight.Bold, color = DarkGold)
                    }
                    if (d.lowConfidence) Text("• " + tr("Low confidence (below 60%). Take another photo of the affected part before acting on this result.",
                        "Mababa ang kumpiyansa (mas mababa sa 60%). Kumuha ng panibagong larawan bago kumilos."))
                    d.qualityWarnings.forEach { w ->
                        Text("• " + when (w) {
                            ImagePreprocessor.WARN_BLUR -> tr("The photo looks blurred. Hold the phone steady.", "Malabo ang larawan. Hawakan nang matatag ang phone.")
                            ImagePreprocessor.WARN_DARK -> tr("The photo is too dark. Use daylight or the flash.", "Masyadong madilim. Gumamit ng liwanag ng araw o flash.")
                            else -> tr("The photo is too bright. Avoid direct sunlight on the lens.", "Masyadong maliwanag. Iwasan ang direktang sikat ng araw.")
                        })
                    }
                }
            }
            SectionCard(tr("What to do", "Ano ang gagawin")) {
                Text(c.action(l))
                TextButton(onClick = { onGuide(c.label) }) { Text(tr("Read more about ", "Alamin pa ang tungkol sa ") + c.title(l)) }
            }
            SectionCard(tr("Confidence of each condition", "Kumpiyansa sa bawat kondisyon")) { ProbabilityBars(d.ranked) }
            SectionCard(tr("Save this scan", "I-save ang scan")) {
                OutlinedTextField(block, { block = it }, label = { Text(tr("Farm block or row (e.g. Block A)", "Block o hanay (hal. Block A)")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (blocks.isNotEmpty()) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        blocks.forEach { b -> AssistChip(onClick = { block = b }, label = { Text(b) }) }
                    }
                }
                OutlinedTextField(notes, { notes = it }, label = { Text(tr("Notes (optional)", "Tala (opsiyonal)")) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                if (!c.isHealthy && c.recheckDays > 0 && vm.activeBatch.isBlank()) {
                    Text(tr("You will be reminded to re-check this plant after ${c.recheckDays} days.", "Ipapaalala sa iyo na suriin muli ang halaman pagkalipas ng ${c.recheckDays} araw."),
                        style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                if (!c.isHealthy || d.lowConfidence) {
                    Text(tr("The technician of your farm will be notified to review it.", "Aabisuhan ang technician ng taniman para suriin ito."), style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { saving = true; vm.save(block, notes, onSaved) }, enabled = !saving, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) {
                    Text(if (saving) tr("Saving…", "Sine-save…") else tr("Save to records", "I-save sa talaan"), fontSize = 16.sp)
                }
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(tr("Discard and scan again", "Huwag i-save at mag-scan muli")) }
            }
            }
        }
        }
    }
}

package ph.edu.asiatech.goananas.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ph.edu.asiatech.goananas.R
import ph.edu.asiatech.goananas.data.AGE_RANGES
import ph.edu.asiatech.goananas.data.AccountEntity
import ph.edu.asiatech.goananas.data.AccountRepository
import ph.edu.asiatech.goananas.data.FARMING_YEARS
import ph.edu.asiatech.goananas.data.GENDERS
import ph.edu.asiatech.goananas.data.Role
import ph.edu.asiatech.goananas.ui.AppViewModel
import ph.edu.asiatech.goananas.ui.Avatar
import ph.edu.asiatech.goananas.ui.ChoiceCard
import ph.edu.asiatech.goananas.ui.ChoiceChips
import ph.edu.asiatech.goananas.ui.InfoBanner
import ph.edu.asiatech.goananas.ui.SectionCard
import ph.edu.asiatech.goananas.ui.lang
import ph.edu.asiatech.goananas.ui.theme.AlertRed
import ph.edu.asiatech.goananas.ui.theme.DeepGreen
import ph.edu.asiatech.goananas.ui.theme.FreshGreen
import ph.edu.asiatech.goananas.ui.theme.LeafGreen
import ph.edu.asiatech.goananas.ui.theme.MintGreen
import ph.edu.asiatech.goananas.ui.theme.Muted
import ph.edu.asiatech.goananas.ui.theme.PineGold
import ph.edu.asiatech.goananas.ui.theme.SkyBlue
import ph.edu.asiatech.goananas.ui.theme.SoftBlue
import ph.edu.asiatech.goananas.ui.theme.SoftGold
import ph.edu.asiatech.goananas.ui.theme.SoftRed
import ph.edu.asiatech.goananas.ui.tr

fun roleIcon(r: Role): ImageVector = when (r) {
    Role.FARMER -> Icons.Filled.Agriculture
    Role.VENDOR -> Icons.Filled.ShoppingBasket
    Role.TECHNICIAN -> Icons.Filled.Science
    Role.OWNER -> Icons.Filled.HomeWork
    Role.ADMIN -> Icons.Filled.AdminPanelSettings
}

/** first screen: choose English or Tagalog */
@Composable
fun LanguageScreen(onPick: (String) -> Unit) {
    var pick by remember { mutableStateOf("en") }
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MintGreen, Color.White))).padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painterResource(R.drawable.ic_logo), null, Modifier.size(120.dp))
        Text("Go Ananas!", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = LeafGreen)
        Text("Pineapple disease detection • Pagtukoy ng sakit ng pinya", color = Muted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Text("Choose your language / Piliin ang wika", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        ChoiceCard(pick == "en", "English", "Use the app in English", Icons.Filled.Language) { pick = "en" }
        Spacer(Modifier.height(10.dp))
        ChoiceCard(pick == "tl", "Tagalog", "Gamitin ang app sa Tagalog", Icons.Filled.Translate) { pick = "tl" }
        Spacer(Modifier.height(28.dp))
        Button(onClick = { onPick(pick) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
            Text(if (pick == "tl") "Magpatuloy" else "Continue", fontSize = 17.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text("You can change it later in Profile. / Mapapalitan ito sa Profile.", style = MaterialTheme.typography.bodySmall, color = Muted, textAlign = TextAlign.Center)
    }
}

private class Page(val icon: ImageVector, val tint: Color, val bg: Color, val en: String, val tl: String, val bodyEn: String, val bodyTl: String)

/** short tutorial shown before the first sign-in (it can be shown again from Profile) */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialScreen(onDone: () -> Unit) {
    val pages = listOf(
        Page(Icons.Filled.CameraAlt, LeafGreen, MintGreen, "Take a clear photo", "Kumuha ng malinaw na larawan",
            "Choose the plant part, then photograph the leaf, crown, heart, base, or fruit from 30 to 60 cm away in daylight.",
            "Piliin ang bahagi ng halaman, saka kunan ang dahon, korona, puso, puno, o bunga mula 30 hanggang 60 cm, sa liwanag ng araw."),
        Page(Icons.Filled.Verified, Color(0xFF8A6100), SoftGold, "Get the result and advice", "Alamin ang resulta at payo",
            "The app tells you if the plant is healthy or has fruit rot, heart rot, mealybug wilt, multiple crowns, or root rot, and what to do. It also warns you if the photo has no pineapple.",
            "Sasabihin ng app kung malusog ang halaman o may bulok na bunga, bulok sa puso, kuto-kuto, maraming korona, o bulok na ugat, at kung ano ang gagawin. Babalaan ka rin kung walang pinya sa larawan."),
        Page(Icons.Filled.NotificationsActive, AlertRed, SoftRed, "Records and reminders", "Talaan at paalala",
            "Every scan is saved with its photo. The app reminds you to re-check sick plants and warns you when a disease spreads in one block.",
            "Nai-save ang bawat scan kasama ang larawan. Ipapaalala ng app ang muling pagsusuri at babalaan ka kapag kumakalat ang sakit sa isang block."),
        Page(Icons.Filled.Groups, SkyBlue, SoftBlue, "Work as one farm", "Magtulungan bilang isang taniman",
            "Your scans reach the technician and farm owner, who can confirm the result and send advice. They can also see the farm on the web dashboard.",
            "Makikita ng technician at may-ari ang iyong mga scan; maaari nilang kumpirmahin ang resulta at magpadala ng payo. Makikita rin nila ang taniman sa web dashboard."),
    )
    val state = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(Color.White).padding(24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDone) { Text(tr("Skip", "Laktawan")) }
        }
        HorizontalPager(state = state, modifier = Modifier.weight(1f)) { i ->
            val p = pages[i]
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(Modifier.size(170.dp).clip(CircleShape).background(p.bg), contentAlignment = Alignment.Center) {
                    Icon(p.icon, null, tint = p.tint, modifier = Modifier.size(84.dp))
                }
                Spacer(Modifier.height(28.dp))
                Text(tr(p.en, p.tl), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, color = DeepGreen)
                Spacer(Modifier.height(12.dp))
                Text(tr(p.bodyEn, p.bodyTl), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = Muted)
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center) {
            repeat(pages.size) { i ->
                Box(Modifier.padding(4.dp).size(width = if (state.currentPage == i) 24.dp else 8.dp, height = 8.dp)
                    .clip(RoundedCornerShape(4.dp)).background(if (state.currentPage == i) LeafGreen else Color(0xFFCFD8CC)))
            }
        }
        val last = state.currentPage == pages.lastIndex
        Button(
            onClick = { if (last) onDone() else scope.launch { state.animateScrollToPage(state.currentPage + 1) } },
            modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp),
        ) { Text(if (last) tr("Get started", "Magsimula") else tr("Next", "Susunod"), fontSize = 17.sp) }
    }
}

/** sign in or create an account */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(vm: AppViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var role by remember { mutableStateOf(Role.FARMER) }
    var farmCode by remember { mutableStateOf("") }
    var farmName by remember { mutableStateOf("") }
    val l = lang()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding()) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Brush.linearGradient(listOf(DeepGreen, LeafGreen, FreshGreen))).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(painterResource(R.drawable.ic_logo), null, Modifier.size(84.dp))
            Text("Go Ananas!", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(tr("Healthy pineapples start with early detection", "Maagang pagtukoy para sa malusog na pinya"), color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center)
        }
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryTabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
                Tab(selected = tab == 0, onClick = { tab = 0; vm.message = null }, text = { Text(tr("Sign in", "Mag-sign in")) })
                Tab(selected = tab == 1, onClick = { tab = 1; vm.message = null }, text = { Text(tr("Create account", "Gumawa ng account")) })
            }
            OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            OutlinedTextField(password, { password = it }, label = { Text(tr("Password", "Password")) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = { IconButton(onClick = { show = !show }) { Icon(if (show) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null) } })

            AnimatedVisibility(tab == 1) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(confirm, { confirm = it }, label = { Text(tr("Confirm password", "Ulitin ang password")) }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(),
                        isError = confirm.isNotEmpty() && confirm != password)
                    Text(tr("I am a…", "Ako ay…"), fontWeight = FontWeight.Bold)
                    Role.selfRegister.forEach { r -> ChoiceCard(role == r, r.label(l), r.desc(l), roleIcon(r)) { role = r } }
                    OutlinedTextField(farmCode, { farmCode = it.uppercase() }, label = { Text(tr("Farm code", "Farm code")) }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text(if (role == Role.OWNER) tr("Create a short code for your farm, e.g. DAKO-TAGAYTAY. Share it with your workers.", "Gumawa ng maikling code ng taniman, hal. DAKO-TAGAYTAY. Ibahagi ito sa mga manggagawa.")
                            else tr("Ask your farm owner for the farm code. The owner will approve your account.", "Itanong sa may-ari ang farm code. Aaprubahan niya ang iyong account."))
                        })
                    if (role == Role.OWNER) {
                        OutlinedTextField(farmName, { farmName = it }, label = { Text(tr("Farm name", "Pangalan ng taniman")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            vm.message?.let { InfoBanner(it, AlertRed, SoftRed, Icons.Filled.Info) }

            val valid = email.isNotBlank() && password.length >= 6 && (tab == 0 || (confirm == password && farmCode.isNotBlank()))
            Button(
                onClick = {
                    vm.message = null
                    if (tab == 0) vm.login(email, password) else vm.register(email, password, role, farmCode, farmName)
                },
                enabled = valid && !vm.busy, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp),
            ) {
                if (vm.busy) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(if (tab == 0) tr("Sign in", "Mag-sign in") else tr("Create account", "Gumawa ng account"), fontSize = 17.sp)
            }
            if (tab == 0) {
                TextButton(onClick = { vm.resetPassword(email) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(tr("Forgot password?", "Nakalimutan ang password?"))
                }
            }

            if (!vm.cloudEnabled) {
                SectionCard(tr("Offline mode: sample accounts", "Offline mode: mga sample na account")) {
                    Text(tr("This phone is not connected to the cloud yet, so accounts are saved on the phone. Tap an account to try each role (password: ",
                        "Hindi pa naka-konekta sa cloud ang phone na ito, kaya sa phone naka-save ang mga account. Pindutin ang isang account para subukan ang bawat role (password: ") +
                        AccountRepository.DEMO_PASSWORD + ").", style = MaterialTheme.typography.bodySmall, color = Muted)
                    Spacer(Modifier.height(6.dp))
                    listOf("farmer@demo.ph" to Role.FARMER, "vendor@demo.ph" to Role.VENDOR, "tech@demo.ph" to Role.TECHNICIAN,
                        "owner@demo.ph" to Role.OWNER, "admin@demo.ph" to Role.ADMIN).forEach { (mail, r) ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable {
                            tab = 0; email = mail; password = AccountRepository.DEMO_PASSWORD
                        }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(roleIcon(r), null, tint = LeafGreen)
                            Spacer(Modifier.width(10.dp))
                            Column { Text(r.label(l), fontWeight = FontWeight.SemiBold); Text(mail, style = MaterialTheme.typography.bodySmall, color = Muted) }
                        }
                    }
                }
            }
        }
    }
}

/** demographic profile asked before the first use, also used to edit the profile later */
@Composable
fun ProfileSetupScreen(vm: AppViewModel, me: AccountEntity, firstTime: Boolean, onDone: () -> Unit) {
    var name by remember(me.uid) { mutableStateOf(me.name) }
    var gender by remember(me.uid) { mutableStateOf(me.gender) }
    var age by remember(me.uid) { mutableStateOf(me.ageRange) }
    var years by remember(me.uid) { mutableStateOf(me.yearsFarming) }
    var location by remember(me.uid) { mutableStateOf(me.location) }
    var phone by remember(me.uid) { mutableStateOf(me.phone) }
    val l = lang()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? -> if (uri != null) vm.setPhoto(uri) }

    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(if (firstTime) tr("Tell us about yourself", "Ipakilala ang sarili") else tr("Edit profile", "I-edit ang profile"),
            onBack = if (firstTime) null else onDone)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp).imePadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (firstTime) Text(tr("This helps the farm owner and technician know who made each scan. It only takes a minute.",
                "Makatutulong ito para malaman ng may-ari at technician kung sino ang gumawa ng bawat scan. Isang minuto lang ito."), color = Muted)
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box {
                    Avatar(me.photoUrl, 112.dp, Modifier.clickable { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })
                    Box(Modifier.align(Alignment.BottomEnd).size(36.dp).clip(CircleShape).background(PineGold), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PhotoCamera, null, tint = Color(0xFF3A2A00), modifier = Modifier.size(20.dp))
                    }
                }
            }
            Text(tr("Tap the picture to choose a profile photo (optional).", "Pindutin ang larawan para pumili ng profile photo (opsiyonal)."),
                style = MaterialTheme.typography.bodySmall, color = Muted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Card(colors = CardDefaults.cardColors(containerColor = MintGreen), shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(roleIcon(me.roleEnum), null, tint = LeafGreen)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(me.roleEnum.label(l), fontWeight = FontWeight.Bold)
                        Text("${me.email} • ${me.farmName.ifBlank { me.farmCode }}", style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                }
            }
            OutlinedTextField(name, { name = it }, label = { Text(tr("Full name", "Buong pangalan")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text(tr("Sex", "Kasarian"), fontWeight = FontWeight.Bold)
            ChoiceChips(GENDERS.map { it.first to (if (l == "tl") it.second else it.first) }, gender) { gender = it }
            Text(tr("Age", "Edad"), fontWeight = FontWeight.Bold)
            ChoiceChips(AGE_RANGES.map { it to (if (l == "tl" && it.startsWith("56")) "56 pataas" else it) }, age) { age = it }
            Text(tr("Experience with pineapple", "Karanasan sa pinya"), fontWeight = FontWeight.Bold)
            ChoiceChips(FARMING_YEARS.map { it.first to (if (l == "tl") it.second else it.first) }, years) { years = it }
            OutlinedTextField(location, { location = it }, label = { Text(tr("Barangay / town", "Barangay / bayan")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(phone, { phone = it }, label = { Text(tr("Mobile number (optional)", "Numero ng cellphone (opsiyonal)")) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            vm.message?.let { InfoBanner(it, AlertRed, SoftRed, Icons.Filled.Info) }
            Button(
                onClick = { vm.saveProfile(me.copy(name = name.trim(), gender = gender, ageRange = age, yearsFarming = years, location = location.trim(), phone = phone.trim(), language = l), onDone) },
                enabled = name.isNotBlank() && gender.isNotBlank() && age.isNotBlank() && !vm.busy,
                modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen),
            ) { Text(if (firstTime) tr("Save and continue", "I-save at magpatuloy") else tr("Save changes", "I-save"), fontSize = 17.sp) }
            Spacer(Modifier.height(16.dp))
        }
    }
}

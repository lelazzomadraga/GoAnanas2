package ph.edu.asiatech.goananas.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ph.edu.asiatech.goananas.R
import ph.edu.asiatech.goananas.data.PineappleClass
import ph.edu.asiatech.goananas.data.ReviewStatus
import ph.edu.asiatech.goananas.data.ScanEntity
import ph.edu.asiatech.goananas.data.ScanStatus
import ph.edu.asiatech.goananas.data.Thumbs
import androidx.compose.ui.graphics.asImageBitmap
import ph.edu.asiatech.goananas.ui.theme.AlertRed
import ph.edu.asiatech.goananas.ui.theme.DarkGold
import ph.edu.asiatech.goananas.ui.theme.DeepGreen
import ph.edu.asiatech.goananas.ui.theme.FreshGreen
import ph.edu.asiatech.goananas.ui.theme.LeafGreen
import ph.edu.asiatech.goananas.ui.theme.Muted
import ph.edu.asiatech.goananas.ui.theme.SkyBlue
import ph.edu.asiatech.goananas.ui.theme.SoftBlue
import ph.edu.asiatech.goananas.ui.theme.SoftGold
import ph.edu.asiatech.goananas.ui.theme.SoftGreen
import ph.edu.asiatech.goananas.ui.theme.SoftRed
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun classColor(c: PineappleClass): Color = if (c.isHealthy) FreshGreen else AlertRed
fun pct(v: Float) = "%.1f%%".format(Locale.US, v * 100)
fun dateTime(ms: Long): String = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US).format(Date(ms))
fun dateOnly(ms: Long): String = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(ms))

fun timeAgo(ms: Long, lang: String): String {
    val m = (System.currentTimeMillis() - ms) / 60_000
    return when {
        m < 1 -> if (lang == "tl") "ngayon lang" else "just now"
        m < 60 -> if (lang == "tl") "$m min ang nakalipas" else "$m min ago"
        m < 1440 -> if (lang == "tl") "${m / 60} oras ang nakalipas" else "${m / 60} h ago"
        m < 2880 -> if (lang == "tl") "kahapon" else "yesterday"
        else -> dateOnly(ms)
    }
}

fun statusText(status: String, lang: String): String = when (status) {
    ScanStatus.OPEN -> if (lang == "tl") "Kailangang aksyunan" else "Needs action"
    ScanStatus.RECHECKED -> if (lang == "tl") "Nasuri muli" else "Rechecked"
    ScanStatus.RESOLVED -> if (lang == "tl") "Naayos na" else "Resolved"
    ScanStatus.HEALTHY -> if (lang == "tl") "Malusog" else "Healthy"
    else -> status
}

/** image of a profile picture, or the default avatar when there is none */
@Composable
fun Avatar(url: String, size: Dp = 48.dp, modifier: Modifier = Modifier) {
    val m = modifier.size(size).clip(CircleShape).background(SoftGreen)
    val dataBitmap = if (url.startsWith("data:")) Thumbs.decode(url) else null
    if (dataBitmap != null) {
        Image(dataBitmap.asImageBitmap(), contentDescription = null, modifier = m, contentScale = ContentScale.Crop)
    } else if (url.isBlank() || url.startsWith("data:")) {
        Image(painterResource(R.drawable.ic_avatar_default), contentDescription = null, modifier = m, contentScale = ContentScale.Crop)
    } else {
        val model: Any = if (url.startsWith("/")) File(url) else url
        AsyncImage(model = model, contentDescription = null, modifier = m, contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_avatar_default), placeholder = painterResource(R.drawable.ic_avatar_default))
    }
}

/** green header with rounded bottom corners used at the top of the main screens */
@Composable
fun GradientHeader(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.linearGradient(listOf(DeepGreen, LeafGreen, FreshGreen)))
            .padding(horizontal = 20.dp, vertical = 20.dp),
        content = content,
    )
}

@Composable
fun SectionCard(title: String? = null, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            if (title != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    if (trailing != null) trailing()
                }
                Spacer(Modifier.height(8.dp))
            }
            content()
        }
    }
}

/** large square button with an icon, used for the main functions of each role */
@Composable
fun ActionTile(icon: ImageVector, title: String, subtitle: String, tint: Color, bg: Color, modifier: Modifier = Modifier, badge: Int = 0, onClick: () -> Unit) {
    Card(
        onClick = onClick, modifier = modifier.height(128.dp), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.Top) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(bg), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.weight(1f))
                if (badge > 0) {
                    Box(Modifier.clip(CircleShape).background(AlertRed).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("$badge", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun StatTile(value: String, label: String, accent: Color = LeafGreen, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(12.dp)) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = accent)
            Text(label, style = MaterialTheme.typography.bodySmall, color = Muted, maxLines = 2)
        }
    }
}

@Composable
fun Pill(text: String, fg: Color, bg: Color) {
    Box(Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 3.dp)) {
        Text(text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
fun StatusBadge(status: String) {
    val l = lang()
    when (status) {
        ScanStatus.OPEN -> Pill(statusText(status, l), AlertRed, SoftRed)
        ScanStatus.RECHECKED -> Pill(statusText(status, l), DarkGold, SoftGold)
        else -> Pill(statusText(status, l), LeafGreen, SoftGreen)
    }
}

@Composable
fun ReviewBadge(reviewStatus: String) {
    when (reviewStatus) {
        ReviewStatus.PENDING -> Pill(tr("For review", "Susuriin pa"), SkyBlue, SoftBlue)
        ReviewStatus.CONFIRMED -> Pill(tr("Confirmed", "Kinumpirma"), LeafGreen, SoftGreen)
        ReviewStatus.CORRECTED -> Pill(tr("Corrected", "Itinama"), DarkGold, SoftGold)
        else -> {}
    }
}

@Composable
fun ScanRow(scan: ScanEntity, showUser: Boolean = false, onClick: () -> Unit) {
    val c = scan.pineappleClass
    val l = lang()
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(model = scan.imageModel, contentDescription = c.title(l), contentScale = ContentScale.Crop,
            modifier = Modifier.size(58.dp).clip(RoundedCornerShape(12.dp)).background(SoftGreen))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(c.title(l), fontWeight = FontWeight.Bold, color = classColor(c), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                listOfNotNull(if (showUser) scan.userName.ifBlank { null } else null, scan.block.ifBlank { null }, timeAgo(scan.createdAt, l)).joinToString(" • "),
                style = MaterialTheme.typography.bodySmall, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 3.dp)) {
                StatusBadge(scan.status)
                ReviewBadge(scan.reviewStatus)
            }
        }
        Text(pct(scan.confidence), style = MaterialTheme.typography.bodySmall, color = Muted)
    }
}

/** confidence of each of the six conditions, highest first */
@Composable
fun ProbabilityBars(ranked: List<Pair<PineappleClass, Float>>) {
    val l = lang()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ranked.forEachIndexed { i, (c, p) ->
            Column {
                Row(Modifier.fillMaxWidth()) {
                    Text(c.title(l), Modifier.weight(1f), fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal)
                    Text(pct(p), fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal)
                }
                LinearProgressIndicator(
                    progress = { p.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = if (i == 0) classColor(c) else Color(0xFF9E9E9E),
                    trackColor = Color(0xFFECEFF1),
                )
            }
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, text: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(64.dp).clip(CircleShape).background(SoftGreen), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = LeafGreen, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(text, color = Muted, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
    }
}

/** single choice shown as chips that wrap to the next line */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChoiceChips(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = selected == value, onClick = { onSelect(value) }, label = { Text(label) },
                leadingIcon = if (selected == value) ({ Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }) else null,
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SoftGreen, selectedLabelColor = DeepGreen),
            )
        }
    }
}

/** card that can be selected, used for language and role choices */
@Composable
fun ChoiceCard(selected: Boolean, title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) SoftGreen else Color.White),
        border = if (selected) BorderStroke(2.dp, LeafGreen) else BorderStroke(1.dp, Color(0xFFDDE3D8)),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(if (selected) LeafGreen else SoftGreen), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if (selected) Color.White else LeafGreen)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            if (selected) Icon(Icons.Filled.Check, null, tint = LeafGreen)
        }
    }
}

@Composable
fun InfoBanner(text: String, fg: Color, bg: Color, icon: ImageVector? = null) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(bg).border(1.dp, fg.copy(alpha = 0.25f), RoundedCornerShape(14.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) { Icon(icon, null, tint = fg); Spacer(Modifier.width(10.dp)) }
        Text(text, color = fg, style = MaterialTheme.typography.bodyMedium)
    }
}

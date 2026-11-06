package ph.edu.asiatech.goananas.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val LeafGreen = Color(0xFF1E5631)
val DeepGreen = Color(0xFF123B21)
val FreshGreen = Color(0xFF4C9A2A)
val MintGreen = Color(0xFFDDF0D3)
val PineGold = Color(0xFFF4B400)
val SoftGold = Color(0xFFFFF4D6)
val DarkGold = Color(0xFF8A6100)
val AlertRed = Color(0xFFC62828)
val SoftRed = Color(0xFFFDECEA)
val SoftGreen = Color(0xFFE6F2E0)
val SkyBlue = Color(0xFF1565C0)
val SoftBlue = Color(0xFFE3F0FD)
val Plum = Color(0xFF6A1B9A)
val SoftPlum = Color(0xFFF3E5F5)
val Paper = Color(0xFFF6F8F2)
val Ink = Color(0xFF1D2A22)
val Muted = Color(0xFF5F6B63)

private val Colors = lightColorScheme(
    primary = LeafGreen, onPrimary = Color.White,
    primaryContainer = MintGreen, onPrimaryContainer = DeepGreen,
    secondary = PineGold, onSecondary = Color(0xFF3A2A00),
    secondaryContainer = SoftGold, onSecondaryContainer = DarkGold,
    tertiary = SkyBlue, tertiaryContainer = SoftBlue,
    error = AlertRed, errorContainer = SoftRed,
    background = Paper, onBackground = Ink,
    surface = Color.White, onSurface = Ink,
    surfaceVariant = Color(0xFFEFF3EA), onSurfaceVariant = Muted,
    outline = Color(0xFFC9D2C4),
)

private val Type = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp),
    titleLarge = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun GoAnanasTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = Type, shapes = AppShapes, content = content)
}

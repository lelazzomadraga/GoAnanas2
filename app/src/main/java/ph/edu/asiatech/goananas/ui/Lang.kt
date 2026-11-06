package ph.edu.asiatech.goananas.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

/** language chosen by the user: "en" (English) or "tl" (Tagalog) */
val LocalLang = compositionLocalOf { "en" }

/** returns the English or Tagalog text depending on the chosen language */
@Composable
@ReadOnlyComposable
fun tr(en: String, tl: String): String = if (LocalLang.current == "tl") tl else en

@Composable
@ReadOnlyComposable
fun lang(): String = LocalLang.current

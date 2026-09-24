package com.okdaithi.daycounter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.okdaithi.daycounter.R

/** Nocturne design tokens (handoff README, "Design tokens"). */
object Nocturne {
    val Bg = Color(0xFF161826)
    val Surface = Color(0xFF232532)
    val Text = Color(0xFFE9E9ED)
    val Accent = Color(0xFF9184D9)
    val Divider = Text.copy(alpha = 0.16f)
    val Scrim = Color(0xFF05060C).copy(alpha = 0.55f)
    val SectionGlow = Color(0xFF353B80)
    val PreviewTop = Color(0xFF1D2036)

    val Neutral100 = Color(0xFFF3F5FE)
    val Neutral300 = Color(0xFFCFD3E5)
    val Neutral400 = Color(0xFFB2B6CA)
    val Neutral500 = Color(0xFF9397AB)
    val Neutral700 = Color(0xFF595D6C)
    val Neutral800 = Color(0xFF3F424D)

    val Accent300 = Color(0xFFD2CEFD)
    val Accent800 = Color(0xFF423A6A)
}

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
)

private val colors = darkColorScheme(
    primary = Nocturne.Accent,
    onPrimary = Nocturne.Bg,
    primaryContainer = Nocturne.Accent800,
    onPrimaryContainer = Nocturne.Text,
    secondary = Nocturne.Accent,
    onSecondary = Nocturne.Bg,
    background = Nocturne.Bg,
    onBackground = Nocturne.Text,
    surface = Nocturne.Surface,
    onSurface = Nocturne.Text,
    surfaceVariant = Nocturne.Surface,
    onSurfaceVariant = Nocturne.Neutral400,
    surfaceContainerHigh = Nocturne.Surface,
    surfaceContainerHighest = Nocturne.Surface,
    outline = Nocturne.Divider,
    outlineVariant = Nocturne.Divider,
    error = Nocturne.Accent300,
)

private val typography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = Inter),
        displayMedium = base.displayMedium.copy(fontFamily = Inter),
        displaySmall = base.displaySmall.copy(fontFamily = Inter),
        headlineLarge = base.headlineLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
        headlineMedium = base.headlineMedium.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
        headlineSmall = base.headlineSmall.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
        titleLarge = base.titleLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
        titleMedium = base.titleMedium.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
        titleSmall = base.titleSmall.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
        bodyLarge = base.bodyLarge.copy(fontFamily = Inter),
        bodyMedium = base.bodyMedium.copy(fontFamily = Inter),
        bodySmall = base.bodySmall.copy(fontFamily = Inter),
        labelLarge = base.labelLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
        labelMedium = base.labelMedium.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
        labelSmall = base.labelSmall.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
    )
}

/** Body text default: Inter 400, 14sp, text color. */
val BodyStyle = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 14.sp, color = Nocturne.Text)

@Composable
fun NocturneTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}

package com.wird.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.wird.core.ui.R

// Default Material typography for the Latin UI.
val Typography = Typography()

/**
 * Amiri Quran — an OFL-licensed typeface designed specifically for Quranic
 * Uthmani text with full vocalization. Bundled in res/font; license in
 * core/ui/AMIRI_QURAN_OFL.txt.
 */
val ArabicFontFamily = FontFamily(
    Font(R.font.amiri_quran_regular),
)

/** Style for rendering Quranic ayah text. */
val ArabicAyahTextStyle: TextStyle = TextStyle(
    fontFamily = ArabicFontFamily,
    fontSize = 28.sp,
    lineHeight = 50.sp,
)

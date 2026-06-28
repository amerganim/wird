package com.wird.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

// Default Material typography. A verified Uthmani/Hafs Arabic FontFamily will be
// added here in Phase 0 (ship the font in res/font and expose `ArabicFontFamily`
// for ayah rendering with proper RTL handling).
val Typography = Typography()

/**
 * Placeholder for the dedicated Quranic Arabic text style. Swap [FontFamily.Serif]
 * for the bundled Uthmani font once the licensed font asset is added.
 */
val ArabicAyahTextStyle: TextStyle = TextStyle(
    fontFamily = FontFamily.Serif,
    fontSize = 28.sp,
    lineHeight = 48.sp,
)

package com.wird.feature.quran.ui

private val arabicIndicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

/** Renders a non-negative integer using Arabic-Indic digits (e.g. 255 -> ٢٥٥). */
fun Int.toArabicIndic(): String =
    toString().map { c -> if (c in '0'..'9') arabicIndicDigits[c - '0'] else c }
        .joinToString("")

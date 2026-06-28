package com.wird.feature.quran.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ArabicNumeralsTest {

    @Test
    fun `single digits map to Arabic-Indic`() {
        assertEquals("٠", 0.toArabicIndic())
        assertEquals("٧", 7.toArabicIndic())
    }

    @Test
    fun `multi digit numbers map each digit`() {
        assertEquals("١٢", 12.toArabicIndic())
        assertEquals("٢٥٥", 255.toArabicIndic())
    }
}

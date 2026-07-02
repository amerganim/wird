package com.wird.feature.recitation.text

import org.junit.Assert.assertEquals
import org.junit.Test

class RecitationTextTest {

    @Test
    fun stripsTashkeelAndAnnotationSigns() {
        // Uthmani "ٱلرَّحْمَٰنِ" -> bare "الرحمن" (alef wasla folded, dagger alef + harakat dropped)
        assertEquals("الرحمن", RecitationText.normalizeWord("ٱلرَّحْمَٰنِ"))
    }

    @Test
    fun foldsAlefVariants() {
        assertEquals("احمد", RecitationText.normalizeWord("أحمد"))
        assertEquals("ان", RecitationText.normalizeWord("إن"))
        assertEquals("امن", RecitationText.normalizeWord("آمن"))
        assertEquals("الله", RecitationText.normalizeWord("ٱلله"))
    }

    @Test
    fun foldsYaAndHamzaCarriersAndTaaMarbuta() {
        assertEquals("علي", RecitationText.normalizeWord("على"))  // alef maksura -> ya
        assertEquals("صلاه", RecitationText.normalizeWord("صلاة")) // taa marbuta -> haa
        assertEquals("شي", RecitationText.normalizeWord("شيء"))    // standalone hamza dropped
    }

    @Test
    fun dropsDigitsAndPunctuationAndEndOfAyahSign() {
        assertEquals("", RecitationText.normalizeWord("١٢٣"))
        assertEquals(listOf("بسم", "الله"), RecitationText.tokenize("بِسْمِ، ٱللَّهِ ۝"))
    }

    @Test
    fun tokenizesBismillah() {
        assertEquals(
            listOf("بسم", "الله", "الرحمن", "الرحيم"),
            RecitationText.tokenize("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"),
        )
    }

    @Test
    fun collapsesWhitespace() {
        assertEquals(listOf("رب", "العلمين"), RecitationText.tokenize("  رب   العٰلمين  "))
    }
}

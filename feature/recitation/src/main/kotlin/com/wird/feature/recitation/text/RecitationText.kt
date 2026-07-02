package com.wird.feature.recitation.text

/**
 * Normalizes Arabic for recitation matching, then tokenizes into words.
 *
 * The expected text is Uthmani (heavy with tashkeel and Quranic annotation signs);
 * an ASR hypothesis is usually bare letters. To compare them fairly we strip all
 * diacritics/marks and fold the letter variants that ASR commonly conflates (the
 * different alifs, ya/alif-maksura, the hamza carriers, taa marbuta → haa). This is
 * matching-only normalization — it never touches the text shown to the user.
 */
object RecitationText {

    // Tashkeel, superscript alef, Quranic annotation signs, tatweel, hamza above/below.
    private val marks = (
        ('ؐ'..'ؚ') +   // Arabic signs
            ('ً'..'ٟ') + // tanwin, harakat, sukun, misc
            'ـ' +             // tatweel
            'ٰ' +             // superscript alef
            ('ۖ'..'ۭ')   // Quranic annotation signs (sajda, waqf, etc.)
        ).toSet()

    private val letterFolds = mapOf(
        'آ' to 'ا', // آ alef madda -> ا
        'أ' to 'ا', // أ alef hamza above -> ا
        'إ' to 'ا', // إ alef hamza below -> ا
        'ٱ' to 'ا', // ٱ alef wasla -> ا
        'ى' to 'ي', // ى alef maksura -> ي
        'ئ' to 'ي', // ئ ya with hamza -> ي
        'ؤ' to 'و', // ؤ waw with hamza -> و
        'ة' to 'ه', // ة taa marbuta -> ه
        'ء' to ' ',      // ء standalone hamza -> drop
    )

    /** Normalize a single word to its comparison form. */
    fun normalizeWord(word: String): String = buildString {
        for (ch in word) {
            when {
                ch in marks -> {} // drop
                ch in letterFolds -> letterFolds[ch]!!.let { if (it != ' ') append(it) }
                ch.isArabicLetter() -> append(ch)
                // anything else (digits, punctuation, latin) is dropped
            }
        }
    }

    /** Split text into normalized word tokens, dropping any that normalize to empty. */
    fun tokenize(text: String): List<String> =
        text.split(WHITESPACE)
            .map { normalizeWord(it) }
            .filter { it.isNotEmpty() }

    private val WHITESPACE = Regex("\\s+")

    // Base Arabic letter block (U+0621..U+064A). Variant letters are folded above,
    // so by the time we reach here only plain letters should remain.
    private fun Char.isArabicLetter(): Boolean = this in 'ء'..'ي'
}

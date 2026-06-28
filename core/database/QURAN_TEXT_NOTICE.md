# Quran text attribution

`surahs.tsv` and `ayahs.tsv` contain the **Tanzil Uthmani** Quran text and surah
metadata, retrieved via the alquran.cloud API edition `quran-uthmani`.

The Arabic Quran text originates from the **Tanzil Project** (https://tanzil.net)
and must be reproduced **unmodified** with attribution, per the Tanzil terms of use:

> Tanzil Quran Text (Uthmani), Version 1.1
> Copyright (C) 2007-2025 Tanzil Project
> License: Creative Commons Attribution 3.0
>
> This copy of the Quran text is carefully produced, highly verified and made
> available for free download. You may not make any changes to the Quran text.

The only processing applied during ingestion was: stripping a leading BOM
(U+FEFF) and trailing whitespace, and removing the leading word "سُورَةُ" from
surah display names. The ayah text itself is unmodified.

Translations are **not** bundled yet. When added, each translation edition's
license and redistribution terms must be confirmed and recorded here first.

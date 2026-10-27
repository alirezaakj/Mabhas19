package ir.mabhas19.feature.home

private const val PERSIAN_ZERO = '\u06F0'
private const val ASCII_ZERO = '0'

private fun Char.toPersianDigit(): Char = when (this) {
    in '0'..'9' -> (PERSIAN_ZERO.code + (this - ASCII_ZERO)).toChar()
    else -> this
}

/**
 * Formats a non-negative count for Persian display only.
 *
 * Never feed the result back into arithmetic or JSON; canonical values stay numeric.
 */
fun Int.toPersianDigits(): String = toString().map { it.toPersianDigit() }.joinToString("")

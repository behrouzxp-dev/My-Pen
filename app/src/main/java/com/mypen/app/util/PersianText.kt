package com.mypen.app.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Persian (Farsi) digit rendering.
 *
 * All user-visible numbers in the interface are shown with Persian digits, which
 * is what Iranian users expect. Internally the application keeps using Latin
 * digits for storage, parsing and tests.
 */
object PersianText {

    private const val PERSIAN_ZERO = '\u06F0' // ۰
    private const val ARABIC_ZERO = '\u0660' // ٠ (Arabic-Indic, used by some keyboards)

    /** Converts every ASCII digit in [input] to its Persian counterpart. */
    fun digits(input: String): String {
        val builder = StringBuilder(input.length)
        for (character in input) {
            builder.append(
                when (character) {
                    in '0'..'9' -> (PERSIAN_ZERO + (character - '0'))
                    else -> character
                }
            )
        }
        return builder.toString()
    }

    /** Converts Persian and Arabic-Indic digits in [input] back to ASCII. */
    fun latinDigits(input: String): String {
        val builder = StringBuilder(input.length)
        for (character in input) {
            builder.append(
                when (character) {
                    in PERSIAN_ZERO..(PERSIAN_ZERO + 9) -> '0' + (character - PERSIAN_ZERO)
                    in ARABIC_ZERO..(ARABIC_ZERO + 9) -> '0' + (character - ARABIC_ZERO)
                    else -> character
                }
            )
        }
        return builder.toString()
    }

    /** Formats [value] with Persian digits and optional grouping separators. */
    fun number(value: Long, grouping: Boolean = false): String {
        val formatted = if (grouping) {
            DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(value)
        } else {
            value.toString()
        }
        return digits(formatted)
    }

    fun number(value: Int, grouping: Boolean = false): String = number(value.toLong(), grouping)
}

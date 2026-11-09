package com.mypen.app.util

/**
 * Jalali (Solar Hijri) calendar conversion.
 *
 * The application stores every date as an ISO-8601 Gregorian string
 * (`yyyy-MM-dd`) so that existing databases, backups and unit tests remain
 * valid, and converts to the Jalali calendar only for display and picking.
 *
 * The conversion is the classic, dependency-free integer algorithm; it is
 * verified by [com.mypen.app.JalaliDateTest] against known Nowruz dates and by
 * an exhaustive round-trip over the 1900..2100 range.
 */
object JalaliDate {

    /** Persian month names, index 0 is Farvardin. */
    val monthNames: List<String> = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    /** Weekday names, index 0 is Saturday (first day of the Persian week). */
    val weekDayNames: List<String> = listOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
    )

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val daysInGregorianMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var year = gy
        var jy: Int
        if (year > 1600) {
            jy = 979
            year -= 1600
        } else {
            jy = 0
            year -= 621
        }
        val leapAdjusted = if (gm > 2) year + 1 else year
        var days = (365 * year) + ((leapAdjusted + 3) / 4) - ((leapAdjusted + 99) / 100) +
            ((leapAdjusted + 399) / 400) - 80 + gd + daysInGregorianMonth[gm - 1]

        jy += 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + (days / 31) else 7 + ((days - 186) / 30)
        val jd = 1 + if (days < 186) days % 31 else (days - 186) % 30
        return Triple(jy, jm, jd)
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        var gy: Int
        var year = jy
        if (year > 979) {
            gy = 1600
            year -= 979
        } else {
            gy = 621
        }
        var days = (365 * year) + ((year / 33) * 8) + (((year % 33) + 3) / 4) + 78 + jd +
            if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30) + 186

        gy += 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            days -= 1
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) days += 1
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd = days + 1
        val leap = if (DateUtils.isLeapYear(gy)) 29 else 28
        val daysInMonth = intArrayOf(0, 31, leap, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        for (index in 1..12) {
            if (gd <= daysInMonth[index]) {
                gm = index
                break
            }
            gd -= daysInMonth[index]
        }
        if (gm == 0) gm = 12
        return Triple(gy, gm, gd)
    }

    /** Converts an ISO Gregorian date (`yyyy-MM-dd`) to a Jalali triple, or null. */
    fun fromIsoDate(isoDate: String?): Triple<Int, Int, Int>? {
        if (isoDate == null) return null
        val trimmed = isoDate.trim()
        if (trimmed.length < 10) return null
        val parts = trimmed.substring(0, 10).split("-")
        if (parts.size != 3) return null
        val gy = parts[0].toIntOrNull() ?: return null
        val gm = parts[1].toIntOrNull() ?: return null
        val gd = parts[2].toIntOrNull() ?: return null
        if (!DateUtils.isValidDate(trimmed)) return null
        return gregorianToJalali(gy, gm, gd)
    }

    /** Converts a Jalali date to an ISO Gregorian date (`yyyy-MM-dd`). */
    fun toIsoDate(jy: Int, jm: Int, jd: Int): String {
        val (gy, gm, gd) = jalaliToGregorian(jy, jm, jd)
        return String.format(java.util.Locale.US, "%04d-%02d-%02d", gy, gm, gd)
    }

    /** Number of days in a Jalali month. */
    fun daysInMonth(jy: Int, jm: Int): Int {
        return when {
            jm <= 6 -> 31
            jm <= 11 -> 30
            else -> if (isLeapYear(jy)) 30 else 29
        }
    }

    /**
     * A Jalali year is leap when twelve consecutive months can fit 366 days;
     * a 33-year cycle approximation is used, which matches the official
     * calendar for the supported range (1264..1500 Jalali).
     */
    fun isLeapYear(jy: Int): Boolean {
        val remainder = jy % 33
        return remainder == 1 || remainder == 5 || remainder == 9 || remainder == 13 ||
            remainder == 17 || remainder == 22 || remainder == 26 || remainder == 30
    }

    /**
     * Zero-based index of the weekday of a Jalali date, where 0 is Saturday.
     */
    fun weekDayIndex(jy: Int, jm: Int, jd: Int): Int {
        val iso = toIsoDate(jy, jm, jd)
        val days = DateUtils.toEpochDays(iso)
        // Saturday is index 0 in the Persian week; the +5 offset aligns the
        // epoch day count used by DateUtils with that convention.
        return (((days + 5) % 7 + 7) % 7).toInt()
    }
}

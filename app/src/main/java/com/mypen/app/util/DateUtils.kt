package com.mypen.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

object DateUtils {

    private val DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$")

    // Use the device's local timezone so that "today" matches the user's calendar day.
    private val LOCAL: TimeZone
        get() = TimeZone.getDefault()

    fun todayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = LOCAL
        return sdf.format(Date())
    }

    fun currentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        sdf.timeZone = LOCAL
        return sdf.format(Date())
    }

    fun isValidDate(dateStr: String?): Boolean {
        if (dateStr == null || !DATE_PATTERN.matcher(dateStr.trim()).matches()) {
            return false
        }
        return try {
            val parts = dateStr.trim().split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()

            if (year !in 1900..2100) return false
            if (month !in 1..12) return false
            if (day !in 1..31) return false

            val maxDays = getDaysInMonth(year, month)
            day <= maxDays
        } catch (e: Exception) {
            false
        }
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 31
        }
    }

    fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    fun toEpochDays(dateStr: String?): Long {
        if (dateStr == null) return 0L
        val parts = dateStr.trim().split("-")
        var y = parts[0].toInt()
        var m = parts[1].toInt()
        val d = parts[2].toInt()

        if (m <= 2) {
            y--
            m += 12
        }
        return 365L * y + y / 4 - y / 100 + y / 400 + (153 * (m + 1)) / 5 + d - 306
    }

    fun calculateDaysRemaining(expiryDate: String, todayDate: String): Long {
        if (!isValidDate(expiryDate) || !isValidDate(todayDate)) {
            return 0L
        }
        return toEpochDays(expiryDate) - toEpochDays(todayDate)
    }

    fun addDays(dateStr: String, days: Int): String {
        if (!isValidDate(dateStr)) return dateStr
        return try {
            val parts = dateStr.trim().split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()

            val cal = Calendar.getInstance(LOCAL, Locale.US).apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_MONTH, days)
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.timeZone = LOCAL
            sdf.format(cal.time)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun isExpiringSoon(expiryDate: String, todayDate: String): Boolean {
        val days = calculateDaysRemaining(expiryDate, todayDate)
        return days in 0..30
    }

    fun isExpired(expiryDate: String, todayDate: String): Boolean {
        val days = calculateDaysRemaining(expiryDate, todayDate)
        return days <= 0
    }

    fun formatEnglish(number: Long): String {
        return String.format(Locale.US, "%d", number)
    }

    fun formatEnglish(number: Int): String {
        return String.format(Locale.US, "%d", number)
    }

    // ---------------------------------------------------------------------
    // Presentation helpers (Jalali calendar + Persian digits)
    // ---------------------------------------------------------------------

    private const val PLACEHOLDER = "—"

    /** Formats an ISO Gregorian date as a short Jalali date: ۱۴۰۳/۰۸/۱۹ */
    fun displayDate(isoDate: String?): String {
        val jalali = JalaliDate.fromIsoDate(isoDate) ?: return PLACEHOLDER
        val (jy, jm, jd) = jalali
        return PersianText.digits(
            String.format(Locale.US, "%04d/%02d/%02d", jy, jm, jd)
        )
    }

    /** Formats an ISO Gregorian date as a long Jalali date: ۱۹ آبان ۱۴۰۳ */
    fun displayDateLong(isoDate: String?): String {
        val jalali = JalaliDate.fromIsoDate(isoDate) ?: return PLACEHOLDER
        val (jy, jm, jd) = jalali
        val monthName = JalaliDate.monthNames.getOrElse(jm - 1) { "" }
        return "${PersianText.number(jd)} $monthName ${PersianText.number(jy)}"
    }

    /** Formats an ISO date-time stamp as ۱۹ آبان ۱۴۰۳ • ۱۴:۳۲ */
    fun displayDateTime(dateTime: String?): String {
        if (dateTime == null || dateTime.length < 10) return PLACEHOLDER
        val datePart = displayDateLong(dateTime.substring(0, 10))
        if (datePart == PLACEHOLDER) return PLACEHOLDER
        val timePart = if (dateTime.length >= 16) {
            val hour = dateTime.substring(11, 13)
            val minute = dateTime.substring(14, 16)
            PersianText.digits("$hour:$minute")
        } else {
            null
        }
        return if (timePart == null) datePart else "$datePart • $timePart"
    }

    /** Short month/day label used by the history timeline: ۱۹ آبان */
    fun displayMonthDay(isoDate: String?): String {
        val jalali = JalaliDate.fromIsoDate(isoDate) ?: return PLACEHOLDER
        val (_, jm, jd) = jalali
        val monthName = JalaliDate.monthNames.getOrElse(jm - 1) { "" }
        return "${PersianText.number(jd)} $monthName"
    }

    /**
     * Human readable shelf-life label for a number of remaining days.
     * Examples: امروز, فردا, ۱۲ روز مانده, ۲ روز گذشته.
     */
    fun remainingLabel(days: Long): String {
        return when {
            days > 1L -> "${PersianText.number(days)} روز مانده"
            days == 1L -> "فردا منقضی می‌شود"
            days == 0L -> "امروز منقضی می‌شود"
            days == -1L -> "۱ روز گذشته"
            else -> "${PersianText.number(-days)} روز گذشته"
        }
    }

    /** Classifies a date as today / yesterday / older for history grouping. */
    fun relativeDayLabel(isoDate: String, today: String): String {
        val days = calculateDaysRemaining(today, isoDate)
        return when (days) {
            0L -> "امروز"
            1L -> "دیروز"
            in 2L..7L -> "${PersianText.number(days)} روز پیش"
            else -> displayDateLong(isoDate)
        }
    }

    /**
     * The fraction (0f..1f) of the shelf life that has already elapsed between
     * [entryDate] and [expiryDate]. Used by the shelf-life progress bar.
     */
    fun shelfLifeProgress(entryDate: String, expiryDate: String, today: String): Float {
        val total = calculateDaysRemaining(expiryDate, entryDate)
        if (total <= 0L) return 0f
        val elapsed = calculateDaysRemaining(today, entryDate)
        return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }
}

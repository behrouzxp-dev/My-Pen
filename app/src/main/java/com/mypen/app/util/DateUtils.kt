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
}

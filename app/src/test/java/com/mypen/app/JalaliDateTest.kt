package com.mypen.app

import com.mypen.app.util.DateUtils
import com.mypen.app.util.JalaliDate
import com.mypen.app.util.PersianText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale

/**
 * The Jalali conversion is the foundation of every displayed date, so it is
 * verified against known Nowruz dates, against weekday arithmetic and with an
 * exhaustive round-trip over a 200 year span.
 */
class JalaliDateTest {

    @Test
    fun nowruzDatesAreConvertedCorrectly() {
        assertEquals(Triple(1403, 1, 1), JalaliDate.gregorianToJalali(2024, 3, 20))
        assertEquals(Triple(1404, 1, 1), JalaliDate.gregorianToJalali(2025, 3, 21))
        assertEquals(Triple(1405, 1, 1), JalaliDate.gregorianToJalali(2026, 3, 21))
        assertEquals(Triple(1400, 1, 1), JalaliDate.gregorianToJalali(2021, 3, 21))
    }

    @Test
    fun knownHistoricalDatesAreConvertedCorrectly() {
        assertEquals(Triple(1357, 11, 22), JalaliDate.gregorianToJalali(1979, 2, 11))
        assertEquals(Triple(1378, 10, 11), JalaliDate.gregorianToJalali(2000, 1, 1))
        assertEquals(Triple(1403, 8, 19), JalaliDate.gregorianToJalali(2024, 11, 9))
    }

    @Test
    fun setIsConvertedInBothDirections() {
        val cases = listOf(
            Triple(1403, 8, 19) to Triple(2024, 11, 9),
            Triple(1404, 1, 1) to Triple(2025, 3, 21),
            Triple(1378, 10, 11) to Triple(2000, 1, 1),
            Triple(1400, 12, 29) to Triple(2022, 3, 20)
        )
        cases.forEach { (jalali, gregorian) ->
            assertEquals(gregorian, JalaliDate.jalaliToGregorian(jalali.first, jalali.second, jalali.third))
        }
    }

    @Test
    fun conversionRoundTripsForEveryDayInTwoCenturies() {
        val calendar = Calendar.getInstance(Locale.US)
        calendar.clear()
        calendar.set(1900, Calendar.JANUARY, 1)
        val end = Calendar.getInstance(Locale.US)
        end.clear()
        end.set(2100, Calendar.DECEMBER, 31)

        while (!calendar.after(end)) {
            val gy = calendar.get(Calendar.YEAR)
            val gm = calendar.get(Calendar.MONTH) + 1
            val gd = calendar.get(Calendar.DAY_OF_MONTH)
            val jalali = JalaliDate.gregorianToJalali(gy, gm, gd)
            assertTrue("month out of range for $gy-$gm-$gd", jalali.second in 1..12)
            assertTrue("day out of range for $gy-$gm-$gd", jalali.third in 1..31)

            val back = JalaliDate.jalaliToGregorian(jalali.first, jalali.second, jalali.third)
            assertEquals("round trip failed for $gy-$gm-$gd", Triple(gy, gm, gd), back)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    @Test
    fun leapYearsFollowTheOfficialCalendar() {
        assertTrue(JalaliDate.isLeapYear(1403))
        assertFalse(JalaliDate.isLeapYear(1404))
        assertFalse(JalaliDate.isLeapYear(1402))
        assertEquals(30, JalaliDate.daysInMonth(1403, 12))
        assertEquals(29, JalaliDate.daysInMonth(1404, 12))
        assertEquals(31, JalaliDate.daysInMonth(1404, 1))
        assertEquals(30, JalaliDate.daysInMonth(1404, 7))
    }

    @Test
    fun weekDaysStartOnSaturday() {
        // 2024-11-09 was a Saturday and 2024-03-20 a Wednesday.
        assertEquals(0, JalaliDate.weekDayIndex(1403, 8, 19))
        assertEquals(4, JalaliDate.weekDayIndex(1403, 1, 1))
    }

    @Test
    fun isoDatesAreConvertedSafely() {
        assertEquals(Triple(1403, 8, 19), JalaliDate.fromIsoDate("2024-11-09"))
        assertEquals(null, JalaliDate.fromIsoDate(null))
        assertEquals(null, JalaliDate.fromIsoDate("not-a-date"))
        assertEquals(null, JalaliDate.fromIsoDate("2024-13-45"))
        assertEquals("2024-11-09", JalaliDate.toIsoDate(1403, 8, 19))
    }

    @Test
    fun persianDisplayHelpersProducePersianDigits() {
        assertEquals("۱۴۰۳/۰۸/۱۹", DateUtils.displayDate("2024-11-09"))
        assertEquals("۱۹ آبان ۱۴۰۳", DateUtils.displayDateLong("2024-11-09"))
        assertEquals("۱۲ روز مانده", DateUtils.remainingLabel(12))
        assertEquals("امروز منقضی می‌شود", DateUtils.remainingLabel(0))
        assertEquals("۳ روز گذشته", DateUtils.remainingLabel(-3))
        assertEquals("۱۴۰۳", PersianText.digits("1403"))
    }

    @Test
    fun latinDigitsCanBeParsedBack() {
        assertEquals("1403", PersianText.latinDigits("۱۴۰۳"))
        assertEquals("25", PersianText.latinDigits(PersianText.digits("25")))
    }
}

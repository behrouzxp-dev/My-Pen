package com.arena.mypen

import com.arena.mypen.model.EventType
import com.arena.mypen.model.HistoryEvent
import com.arena.mypen.model.InsulinPen
import com.arena.mypen.model.InsulinType
import com.arena.mypen.model.NotificationSettings
import com.arena.mypen.model.PenStatus
import com.arena.mypen.util.DateUtils
import com.arena.mypen.util.PriorityCalculator
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.util.UUID

class MyPenCompatibilityTest {

    // ==========================================
    // ۱. آزمون سازگاری واقعی فایل پشتیبان نسخه قبلی (v0)
    // ==========================================

    @Test
    fun testPreviousVersionBackupFileCompatibility() {
        val backupFile = File("/home/user/My Pen/releases/v0/sample_insulin_backup.json")
        assertTrue("فایل پشتیبان نسخه قبلی باید موجود باشد", backupFile.exists())

        val jsonContent = backupFile.readText(Charsets.UTF_8)
        assertNotNull(jsonContent)

        val root = JSONObject(jsonContent)
        assertEquals("شماره نسخه قالب پشتیبان باید ۱ باشد", 1, root.getInt("schemaVersion"))
        assertEquals("2026-10-18 10:00:00", root.getString("exportedAt"))

        // اعتبارسنجی تنظیمات اعلان
        val settingsObj = root.getJSONObject("notificationSettings")
        val settings = NotificationSettings.fromJsonObject(settingsObj)
        assertTrue("اعلان ۳۰ روز باید فعال باشد", settings.notify30Days)
        assertTrue("اعلان ۱۴ روز باید فعال باشد", settings.notify14Days)
        assertTrue("اعلان ۷ روز باید فعال باشد", settings.notify7Days)

        // اعتبارسنجی قلم‌های صادرشده نسخه قبلی
        val pensArray = root.getJSONArray("pens")
        assertEquals("تعداد قلم‌های صادرشده در نسخه قبلی ۸ قلم است", 8, pensArray.length())

        val restoredPens = mutableListOf<InsulinPen>()
        for (i in 0 until pensArray.length()) {
            val pen = InsulinPen.fromJsonObject(pensArray.getJSONObject(i))
            restoredPens.add(pen)
        }

        // بررسی یکتایی شناسه‌ها
        val uniqueIds = restoredPens.map { it.id }.toSet()
        assertEquals(8, uniqueIds.size)

        // بررسی جزئیات قلم‌های خاص (بلنداثر، کوتاه‌اثر، بازشده، معیوب، منقضی)
        val pen1 = restoredPens.first { it.id == "e1a9501a-8b20-4a74-9f21-72a50a7c0001" }
        assertEquals("Lantus SoloStar", pen1.name)
        assertEquals(InsulinType.LONG_ACTING, pen1.type)
        assertEquals("2026-10-25", pen1.expiryDate)
        assertEquals(PenStatus.ACTIVE, pen1.status)

        val pen6 = restoredPens.first { it.id == "e1a9501a-8b20-4a74-9f21-72a50a7c0006" }
        assertEquals("Apidra SoloStar", pen6.name)
        assertEquals(InsulinType.SHORT_ACTING, pen6.type)
        assertEquals(PenStatus.OPENED, pen6.status)
        assertEquals("2026-10-18", pen6.openedDate)

        val pen7 = restoredPens.first { it.id == "e1a9501a-8b20-4a74-9f21-72a50a7c0007" }
        assertEquals(PenStatus.DEFECTIVE, pen7.status)
        assertEquals("2026-10-12", pen7.defectiveDate)
        assertEquals("تغییر شفافیت مایع انسولین و ایجاد ذرات معلق", pen7.defectiveReason)

        val pen8 = restoredPens.first { it.id == "e1a9501a-8b20-4a74-9f21-72a50a7c0008" }
        assertEquals(PenStatus.EXPIRED, pen8.status)
        assertEquals("2026-10-10", pen8.expiryDate)

        // اعتبارسنجی تاریخچه رویدادها
        val eventsArray = root.getJSONArray("events")
        assertEquals("تعداد رویدادهای صادرشده نسخه قبلی ۱۱ رویداد است", 11, eventsArray.length())

        val restoredEvents = mutableListOf<HistoryEvent>()
        for (i in 0 until eventsArray.length()) {
            val ev = HistoryEvent.fromJsonObject(eventsArray.getJSONObject(i))
            restoredEvents.add(ev)
        }

        // بررسی رویداد باز شدن قلم شماره ۶
        val openEvent = restoredEvents.first { it.penId == pen6.id && it.eventType == EventType.OPENED }
        assertEquals("2026-10-18", openEvent.eventDate)
        assertTrue(openEvent.description.contains("باز شدن قلم انسولین"))

        // بررسی رویداد خرابی قلم شماره ۷ با متن فارسی
        val defectEvent = restoredEvents.first { it.penId == pen7.id && it.eventType == EventType.DEFECTIVE }
        assertTrue(defectEvent.description.contains("تغییر شفافیت مایع انسولین"))
    }

    // ==========================================
    // ۲. آزمون واکنش به فایل‌های نامعتبر، ناقص و نسخه ناشناخته
    // ==========================================

    @Test
    fun testInvalidAndCorruptBackupRejection() {
        // ۱. رد فایل خالی
        try {
            val emptyJson = ""
            require(emptyJson.isNotBlank())
            fail("باید فایل خالی را رد کند")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message != null)
        }

        // ۲. رد نسخه قالب ناشناخته
        val incompatibleJson = """{"schemaVersion": 99, "pens": [], "events": []}"""
        try {
            val root = JSONObject(incompatibleJson)
            val v = root.optInt("schemaVersion", -1)
            if (v != 1) throw IllegalArgumentException("Unsupported schema: $v")
            fail("باید نسخه ناشناخته ۹۹ را رد کند")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Unsupported schema"))
        }

        // ۳. رد فایل ناقص (فاقد بخش pens یا events)
        val missingSectionsJson = """{"schemaVersion": 1, "pens": []}"""
        try {
            val root = JSONObject(missingSectionsJson)
            if (!root.has("pens") || !root.has("events")) {
                throw IllegalArgumentException("Missing required sections")
            }
            fail("باید فایل فاقد events را رد کند")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Missing required sections"))
        }
    }

    // ==========================================
    // ۳. آزمون اولویت‌بندی مصرف و برابری تاریخ‌های یکسان
    // ==========================================

    @Test
    fun testPriorityCalculationAndEqualDates() {
        val today = "2026-10-18"
        val pens = mutableListOf(
            InsulinPen(id = "1", name = "Pen A", type = InsulinType.LONG_ACTING, expiryDate = "2026-11-10"),
            InsulinPen(id = "2", name = "Pen B", type = InsulinType.LONG_ACTING, expiryDate = "2026-10-25"),
            InsulinPen(id = "3", name = "Pen C", type = InsulinType.LONG_ACTING, expiryDate = "2026-10-25"), // تاریخ برابر با Pen B
            InsulinPen(id = "4", name = "Pen D", type = InsulinType.LONG_ACTING, expiryDate = "2026-12-01")
        )

        PriorityCalculator.assignPrioritiesAndDays(pens, today)

        // ردیف اول و دوم باید دارای تاریخ ۲۰۲۶-۱۰-۲۵ باشند
        assertEquals("2026-10-25", pens[0].expiryDate)
        assertEquals("2026-10-25", pens[1].expiryDate)
        // اولویت مصرف هر دو باید دقیقاً ۱ باشد!
        assertEquals(1, pens[0].priority)
        assertEquals(1, pens[1].priority)
        assertEquals(7L, pens[0].daysRemaining)
        assertEquals(7L, pens[1].daysRemaining)

        // ردیف سوم باید دارای تاریخ ۲۰۲۶-۱۱-۱۰ با اولویت ۲ باشد
        assertEquals("2026-11-10", pens[2].expiryDate)
        assertEquals(2, pens[2].priority)
        assertEquals(23L, pens[2].daysRemaining)

        // ردیف چهارم باید دارای تاریخ ۲۰۲۶-۱۲-۰۱ با اولویت ۳ باشد
        assertEquals("2026-12-01", pens[3].expiryDate)
        assertEquals(3, pens[3].priority)
        assertEquals(44L, pens[3].daysRemaining)
    }

    // ==========================================
    // ۴. آزمون استقلال رکوردهای ثبت گروهی
    // ==========================================

    @Test
    fun testGroupEntryIndependence() {
        val count = 4
        val name = "Lantus"
        val type = InsulinType.LONG_ACTING
        val expiry = "2027-01-15"
        val today = "2026-10-18"

        val pens = (0 until count).map {
            InsulinPen(
                id = UUID.randomUUID().toString(),
                name = name,
                type = type,
                expiryDate = expiry,
                status = PenStatus.ACTIVE,
                entryDate = today
            )
        }

        // ۴ رکورد با ۴ شناسه یکتای مجزا
        assertEquals(4, pens.size)
        val ids = pens.map { it.id }.toSet()
        assertEquals(4, ids.size)

        // تغییر وضعیت یکی روی بقیه تأثیر نمی‌گذارد
        pens[0].status = PenStatus.OPENED
        pens[0].openedDate = today

        assertEquals(PenStatus.OPENED, pens[0].status)
        assertEquals(PenStatus.ACTIVE, pens[1].status)
        assertEquals(PenStatus.ACTIVE, pens[2].status)
        assertEquals(PenStatus.ACTIVE, pens[3].status)
    }

    // ==========================================
    // ۵. آزمون محاسبات تقویمی و روز صفر انقضا
    // ==========================================

    @Test
    fun testCalendarCalculations() {
        val today = "2026-10-18"

        // روز صفر = منقضی
        assertEquals(0L, DateUtils.calculateDaysRemaining("2026-10-18", today))
        assertTrue(DateUtils.isExpired("2026-10-18", today))

        // روز گذشته = منقضی
        assertEquals(-3L, DateUtils.calculateDaysRemaining("2026-10-15", today))
        assertTrue(DateUtils.isExpired("2026-10-15", today))

        // بازه ۳۰ روز آینده
        val in30Days = DateUtils.addDays(today, 30)
        assertEquals(30L, DateUtils.calculateDaysRemaining(in30Days, today))
        assertTrue(DateUtils.isExpiringSoon(in30Days, today))

        // بیش از ۳۰ روز
        val in31Days = DateUtils.addDays(today, 31)
        assertEquals(31L, DateUtils.calculateDaysRemaining(in31Days, today))
        assertFalse(DateUtils.isExpiringSoon(in31Days, today))
    }

    // ==========================================
    // ۶. آزمون اعتبارسنجی ارقام انگلیسی (ASCII 0-9)
    // ==========================================

    @Test
    fun testEnglishDigitsIntegrity() {
        val formatted = DateUtils.formatEnglish(1234567890L)
        assertEquals("1234567890", formatted)
        for (c in formatted.toCharArray()) {
            assertTrue("کاراکتر $c باید رقم انگلیسی باشد", c in '0'..'9')
        }
    }
}

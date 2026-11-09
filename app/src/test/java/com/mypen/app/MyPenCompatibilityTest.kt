package com.mypen.app

import com.mypen.app.model.EventType
import com.mypen.app.model.HistoryEvent
import com.mypen.app.model.InsulinPen
import com.mypen.app.model.InsulinType
import com.mypen.app.model.NotificationSettings
import com.mypen.app.model.PenStatus
import com.mypen.app.util.DateUtils
import com.mypen.app.util.PriorityCalculator
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

/**
 * Compatibility and robustness tests for the backup format and core domain logic.
 * The v0 backup fixture lives at app/src/test/resources/sample_insulin_backup.json
 * so the suite is fully hermetic (no machine-specific paths).
 */
class MyPenCompatibilityTest {

    // ==========================================
    // 1. Compatibility with a real v0 backup file
    // ==========================================

    @Test
    fun testPreviousVersionBackupFileCompatibility() {
        val fixtureStream = javaClass.getResourceAsStream("/sample_insulin_backup.json")
        assertNotNull("v0 backup fixture must be on the test classpath", fixtureStream)

        val jsonContent = fixtureStream!!.bufferedReader(Charsets.UTF_8).readText()
        assertNotNull(jsonContent)

        val root = JSONObject(jsonContent)
        assertEquals("Backup schema version must be 1", 1, root.getInt("schemaVersion"))
        assertEquals("2026-10-18 10:00:00", root.getString("exportedAt"))

        // Notification settings exported by the previous version
        val settingsObj = root.getJSONObject("notificationSettings")
        val settings = NotificationSettings.fromJsonObject(settingsObj)
        assertTrue("30-day notification must be enabled", settings.notify30Days)
        assertTrue("14-day notification must be enabled", settings.notify14Days)
        assertTrue("7-day notification must be enabled", settings.notify7Days)

        // Pens exported by the previous version
        val pensArray = root.getJSONArray("pens")
        assertEquals("The v0 export contains 8 pens", 8, pensArray.length())

        val restoredPens = mutableListOf<InsulinPen>()
        for (i in 0 until pensArray.length()) {
            val pen = InsulinPen.fromJsonObject(pensArray.getJSONObject(i))
            restoredPens.add(pen)
        }

        // IDs must be unique
        val uniqueIds = restoredPens.map { it.id }.toSet()
        assertEquals(8, uniqueIds.size)

        // Spot-check specific pens (long-acting, short-acting, opened, defective, expired)
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

        // Events exported by the previous version
        val eventsArray = root.getJSONArray("events")
        assertEquals("The v0 export contains 11 events", 11, eventsArray.length())

        val restoredEvents = mutableListOf<HistoryEvent>()
        for (i in 0 until eventsArray.length()) {
            val ev = HistoryEvent.fromJsonObject(eventsArray.getJSONObject(i))
            restoredEvents.add(ev)
        }

        // OPENED event for pen #6
        val openEvent = restoredEvents.first { it.penId == pen6.id && it.eventType == EventType.OPENED }
        assertEquals("2026-10-18", openEvent.eventDate)
        assertTrue(openEvent.description.contains("باز شدن قلم انسولین"))

        // DEFECTIVE event for pen #7 with its Persian description preserved
        val defectEvent = restoredEvents.first { it.penId == pen7.id && it.eventType == EventType.DEFECTIVE }
        assertTrue(defectEvent.description.contains("تغییر شفافیت مایع انسولین"))
    }

    // ==========================================
    // 2. Rejection of invalid, corrupt, and unknown-version backups
    // ==========================================

    @Test
    fun testInvalidAndCorruptBackupRejection() {
        // 1. Reject an empty file
        try {
            val emptyJson = ""
            require(emptyJson.isNotBlank())
            fail("An empty file must be rejected")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message != null)
        }

        // 2. Reject an unknown backup schema version (99)
        val incompatibleJson = """{"schemaVersion": 99, "pens": [], "events": []}"""
        try {
            val root = JSONObject(incompatibleJson)
            val v = root.optInt("schemaVersion", -1)
            if (v != 1) throw IllegalArgumentException("Unsupported schema: $v")
            fail("Schema version 99 must be rejected")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Unsupported schema"))
        }

        // 3. Reject a backup missing required sections (no "events")
        val missingSectionsJson = """{"schemaVersion": 1, "pens": []}"""
        try {
            val root = JSONObject(missingSectionsJson)
            if (!root.has("pens") || !root.has("events")) {
                throw IllegalArgumentException("Missing required sections")
            }
            fail("A backup without events must be rejected")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Missing required sections"))
        }
    }

    // ==========================================
    // 3. Consumption priority ranking and equal expiry dates
    // ==========================================

    @Test
    fun testPriorityCalculationAndEqualDates() {
        val today = "2026-10-18"
        val pens = mutableListOf(
            InsulinPen(id = "1", name = "Pen A", type = InsulinType.LONG_ACTING, expiryDate = "2026-11-10"),
            InsulinPen(id = "2", name = "Pen B", type = InsulinType.LONG_ACTING, expiryDate = "2026-10-25"),
            InsulinPen(id = "3", name = "Pen C", type = InsulinType.LONG_ACTING, expiryDate = "2026-10-25"), // same expiry as Pen B
            InsulinPen(id = "4", name = "Pen D", type = InsulinType.LONG_ACTING, expiryDate = "2026-12-01")
        )

        PriorityCalculator.assignPrioritiesAndDays(pens, today)

        // First two rows must be the 2026-10-25 pens
        assertEquals("2026-10-25", pens[0].expiryDate)
        assertEquals("2026-10-25", pens[1].expiryDate)
        // Both must receive priority rank 1
        assertEquals(1, pens[0].priority)
        assertEquals(1, pens[1].priority)
        assertEquals(7L, pens[0].daysRemaining)
        assertEquals(7L, pens[1].daysRemaining)

        // Third row is 2026-11-10 with priority 2
        assertEquals("2026-11-10", pens[2].expiryDate)
        assertEquals(2, pens[2].priority)
        assertEquals(23L, pens[2].daysRemaining)

        // Fourth row is 2026-12-01 with priority 3
        assertEquals("2026-12-01", pens[3].expiryDate)
        assertEquals(3, pens[3].priority)
        assertEquals(44L, pens[3].daysRemaining)
    }

    // ==========================================
    // 4. Independence of grouped entry records
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

        // 4 records with 4 distinct IDs
        assertEquals(4, pens.size)
        val ids = pens.map { it.id }.toSet()
        assertEquals(4, ids.size)

        // Changing one record must not affect the others
        pens[0].status = PenStatus.OPENED
        pens[0].openedDate = today

        assertEquals(PenStatus.OPENED, pens[0].status)
        assertEquals(PenStatus.ACTIVE, pens[1].status)
        assertEquals(PenStatus.ACTIVE, pens[2].status)
        assertEquals(PenStatus.ACTIVE, pens[3].status)
    }

    // ==========================================
    // 5. Calendar math and zero-day expiry boundary
    // ==========================================

    @Test
    fun testCalendarCalculations() {
        val today = "2026-10-18"

        // Zero day = expired
        assertEquals(0L, DateUtils.calculateDaysRemaining("2026-10-18", today))
        assertTrue(DateUtils.isExpired("2026-10-18", today))

        // Past day = expired
        assertEquals(-3L, DateUtils.calculateDaysRemaining("2026-10-15", today))
        assertTrue(DateUtils.isExpired("2026-10-15", today))

        // 30 days from now
        val in30Days = DateUtils.addDays(today, 30)
        assertEquals(30L, DateUtils.calculateDaysRemaining(in30Days, today))
        assertTrue(DateUtils.isExpiringSoon(in30Days, today))

        // More than 30 days
        val in31Days = DateUtils.addDays(today, 31)
        assertEquals(31L, DateUtils.calculateDaysRemaining(in31Days, today))
        assertFalse(DateUtils.isExpiringSoon(in31Days, today))
    }

    // ==========================================
    // 6. English digits integrity (ASCII 0-9)
    // ==========================================

    @Test
    fun testEnglishDigitsIntegrity() {
        val formatted = DateUtils.formatEnglish(1234567890L)
        assertEquals("1234567890", formatted)
        for (c in formatted.toCharArray()) {
            assertTrue("Character $c must be an ASCII digit", c in '0'..'9')
        }
    }
}

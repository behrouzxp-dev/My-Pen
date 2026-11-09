package com.mypen.app

import com.mypen.app.model.EventType
import com.mypen.app.model.HistoryEvent
import com.mypen.app.model.InsulinPen
import com.mypen.app.model.InsulinType
import com.mypen.app.model.NotificationSettings
import com.mypen.app.model.PenStatus
import com.mypen.app.util.DateUtils
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Hermetic unit tests for JSON (de)serialization of the backup models and
 * for defensive enum parsing. No Android runtime required.
 */
class DataModelsTest {

    @Test
    fun insulinPenJsonRoundTripPreservesAllFields() {
        val pen = InsulinPen(
            id = "pen-1",
            name = "Lantus SoloStar",
            type = InsulinType.SHORT_ACTING,
            expiryDate = "2027-01-31",
            status = PenStatus.OPENED,
            entryDate = "2026-10-01",
            openedDate = "2026-10-05",
            defectiveDate = null,
            defectiveReason = null
        )

        val json = pen.toJsonObject()
        // JSONObject.NULL must be used for nullable fields instead of omitting them
        assertTrue(json.isNull("defectiveDate"))
        assertTrue(json.isNull("defectiveReason"))

        val restored = InsulinPen.fromJsonObject(json)
        assertEquals(pen.id, restored.id)
        assertEquals(pen.name, restored.name)
        assertEquals(pen.type, restored.type)
        assertEquals(pen.expiryDate, restored.expiryDate)
        assertEquals(pen.status, restored.status)
        assertEquals(pen.entryDate, restored.entryDate)
        assertEquals(pen.openedDate, restored.openedDate)
        assertNull(restored.defectiveDate)
        assertNull(restored.defectiveReason)
    }

    @Test
    fun insulinPenJsonRoundTripWithDefectiveFields() {
        val pen = InsulinPen(
            id = "pen-2",
            name = "NovoRapid FlexPen",
            type = InsulinType.SHORT_ACTING,
            expiryDate = "2027-04-10",
            status = PenStatus.DEFECTIVE,
            entryDate = "2026-09-01",
            openedDate = null,
            defectiveDate = "2026-10-12",
            defectiveReason = "Particles in solution"
        )

        val restored = InsulinPen.fromJsonObject(pen.toJsonObject())
        assertEquals(PenStatus.DEFECTIVE, restored.status)
        assertEquals("2026-10-12", restored.defectiveDate)
        assertEquals("Particles in solution", restored.defectiveReason)
        assertNull(restored.openedDate)
    }

    @Test
    fun historyEventJsonRoundTripPreservesValues() {
        val event = HistoryEvent(
            id = 42L,
            penId = "pen-1",
            eventType = EventType.EXPIRED,
            eventDate = "2026-10-18",
            eventTime = "2026-10-18 08:30:00",
            description = "Reached expiry"
        )

        val restored = HistoryEvent.fromJsonObject(event.toJsonObject())
        assertEquals(42L, restored.id)
        assertEquals("pen-1", restored.penId)
        assertEquals(EventType.EXPIRED, restored.eventType)
        assertEquals("2026-10-18", restored.eventDate)
        assertEquals("2026-10-18 08:30:00", restored.eventTime)
        assertEquals("Reached expiry", restored.description)
    }

    @Test
    fun historyEventFromJsonUsesEventDateForMissingEventTime() {
        val json = JSONObject("""{"penId":"p","eventType":"ENTRY","eventDate":"2026-10-18","description":"x"}""")
        val event = HistoryEvent.fromJsonObject(json)
        assertEquals("2026-10-18 00:00:00", event.eventTime)
    }

    @Test
    fun notificationSettingsRoundTripAndDefaults() {
        val defaults = NotificationSettings.fromJsonObject(null)
        assertTrue(defaults.notify30Days)
        assertTrue(defaults.notify14Days)
        assertTrue(defaults.notify7Days)

        val settings = NotificationSettings(notify30Days = false, notify14Days = true, notify7Days = false)
        val restored = NotificationSettings.fromJsonObject(settings.toJsonObject())
        assertFalse(restored.notify30Days)
        assertTrue(restored.notify14Days)
        assertFalse(restored.notify7Days)
    }

    @Test
    fun enumParsersFallbackToSafeDefaults() {
        assertEquals(PenStatus.ACTIVE, PenStatus.fromString(null))
        assertEquals(PenStatus.ACTIVE, PenStatus.fromString("UNKNOWN_VALUE"))
        assertEquals(PenStatus.OPENED, PenStatus.fromString("opened"))

        assertEquals(InsulinType.LONG_ACTING, InsulinType.fromString(null))
        assertEquals(InsulinType.LONG_ACTING, InsulinType.fromString("something-else"))
        assertEquals(InsulinType.SHORT_ACTING, InsulinType.fromString("short_acting"))

        assertEquals(EventType.ENTRY, EventType.fromString(null))
        assertEquals(EventType.ENTRY, EventType.fromString("garbage"))
        assertEquals(EventType.DEFECTIVE, EventType.fromString("defective"))
    }

    @Test
    fun dateValidationRejectsImpossibleDates() {
        assertTrue(DateUtils.isValidDate("2024-02-29")) // leap year
        assertFalse(DateUtils.isValidDate("2025-02-29")) // not a leap year
        assertFalse(DateUtils.isValidDate("2026-13-01"))
        assertFalse(DateUtils.isValidDate("2026-00-10"))
        assertFalse(DateUtils.isValidDate("2026-04-31"))
        assertFalse(DateUtils.isValidDate("1899-01-01"))
        assertFalse(DateUtils.isValidDate("2101-01-01"))
        assertFalse(DateUtils.isValidDate("18-10-2026"))
        assertFalse(DateUtils.isValidDate("2026/10/18"))
        assertFalse(DateUtils.isValidDate(""))
        assertFalse(DateUtils.isValidDate(null))
    }

    @Test
    fun addDaysRollsAcrossYearBoundaries() {
        assertEquals("2026-01-01", DateUtils.addDays("2025-12-31", 1))
        assertEquals("2027-01-01", DateUtils.addDays("2026-12-31", 1))
        assertEquals("2024-02-29", DateUtils.addDays("2024-02-28", 1))
        assertEquals("2025-03-01", DateUtils.addDays("2025-02-28", 1))
        assertEquals("2026-10-18", DateUtils.addDays("2026-10-18", 0))
    }

    @Test
    fun epochDayDifferencesAreExactAcrossLeapYears() {
        // Difference must equal 366 across a leap-year boundary
        val d1 = DateUtils.toEpochDays("2024-02-28")
        val d2 = DateUtils.toEpochDays("2025-02-28")
        assertEquals(366L, d2 - d1)

        val d3 = DateUtils.toEpochDays("2023-03-01")
        val d4 = DateUtils.toEpochDays("2024-03-01")
        assertEquals(366L, d4 - d3)

        assertEquals(1L, DateUtils.toEpochDays("2025-03-02") - DateUtils.toEpochDays("2025-03-01"))
    }
}

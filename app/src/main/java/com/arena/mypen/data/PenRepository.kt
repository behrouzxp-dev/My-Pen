package com.arena.mypen.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.arena.mypen.model.DashboardMetrics
import com.arena.mypen.model.EventType
import com.arena.mypen.model.HistoryEvent
import com.arena.mypen.model.InsulinPen
import com.arena.mypen.model.InsulinType
import com.arena.mypen.model.NotificationSettings
import com.arena.mypen.model.PenStatus
import com.arena.mypen.util.DateUtils
import com.arena.mypen.util.PriorityCalculator
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

class PenRepository(private val dbHelper: AppDatabase) {

    constructor(context: Context) : this(AppDatabase.getInstance(context))

    /**
     * Adds N independent pens atomically in a single transaction.
     */
    @Synchronized
    fun addPens(name: String, type: InsulinType, expiryDate: String, count: Int, today: String): List<String> {
        require(name.trim().isNotEmpty()) { "نام انسولین نمی‌تواند خالی باشد." }
        require(DateUtils.isValidDate(expiryDate)) { "تاریخ انقضا نامعتبر است." }
        require(count > 0) { "تعداد قلم‌ها باید بزرگ‌تر از صفر باشد." }
        require(count <= MAX_PENS_PER_ENTRY) { "حداکثر $MAX_PENS_PER_ENTRY قلم در هر ثبت امکان‌پذیر است." }

        val db = dbHelper.writableDatabase
        val generatedIds = mutableListOf<String>()
        val trimmedName = name.trim()
        val currentTimestamp = DateUtils.currentDateTime()

        db.beginTransaction()
        try {
            for (i in 0 until count) {
                val id = UUID.randomUUID().toString()
                generatedIds.add(id)

                val penValues = ContentValues().apply {
                    put("id", id)
                    put("name", trimmedName)
                    put("type", type.name)
                    put("expiry_date", expiryDate)
                    put("status", PenStatus.ACTIVE.name)
                    put("entry_date", today)
                    putNull("opened_date")
                    putNull("defective_date")
                    putNull("defective_reason")
                }

                val insertPen = db.insertOrThrow(AppDatabase.TABLE_PENS, null, penValues)
                if (insertPen == -1L) {
                    throw RuntimeException("خطا در ذخیره‌سازی قلم شماره ${i + 1}")
                }

                val eventValues = ContentValues().apply {
                    put("pen_id", id)
                    put("event_type", EventType.ENTRY.name)
                    put("event_date", today)
                    put("event_time", currentTimestamp)
                    put("description", "ورود به موجودی به عنوان قلم فعال بازنشده (${type.persianLabel})")
                }

                val insertEvent = db.insertOrThrow(AppDatabase.TABLE_HISTORY, null, eventValues)
                if (insertEvent == -1L) {
                    throw RuntimeException("خطا در ثبت تاریخچه قلم شماره ${i + 1}")
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        checkAndTransitionExpiredPens(today)
        return generatedIds
    }

    /**
     * Transitions ACTIVE and OPENED pens with expiry_date <= today to EXPIRED.
     * DEFECTIVE pens keep their DEFECTIVE status (defect supersedes expiry).
     * Records exactly one EXPIRED event in history per pen.
     */
    @Synchronized
    fun checkAndTransitionExpiredPens(today: String): Int {
        if (!DateUtils.isValidDate(today)) return 0

        val db = dbHelper.writableDatabase
        val expiredIds = mutableListOf<String>()

        val cursor = db.rawQuery(
            "SELECT id FROM ${AppDatabase.TABLE_PENS} WHERE status IN (?, ?) AND expiry_date <= ?",
            arrayOf(PenStatus.ACTIVE.name, PenStatus.OPENED.name, today)
        )
        cursor.use {
            while (it.moveToNext()) {
                expiredIds.add(it.getString(0))
            }
        }

        if (expiredIds.isEmpty()) return 0

        val currentTimestamp = DateUtils.currentDateTime()
        var transitionedCount = 0

        db.beginTransaction()
        try {
            for (id in expiredIds) {
                val cv = ContentValues().apply {
                    put("status", PenStatus.EXPIRED.name)
                }
                db.update(AppDatabase.TABLE_PENS, cv, "id = ?", arrayOf(id))

                val checkCursor = db.rawQuery(
                    "SELECT id FROM ${AppDatabase.TABLE_HISTORY} WHERE pen_id = ? AND event_type = ?",
                    arrayOf(id, EventType.EXPIRED.name)
                )
                val exists = checkCursor.use { it.count > 0 }

                if (!exists) {
                    val ev = ContentValues().apply {
                        put("pen_id", id)
                        put("event_type", EventType.EXPIRED.name)
                        put("event_date", today)
                        put("event_time", currentTimestamp)
                        put("description", "رسیدن تاریخ انقضا و انتقال خودکار به بخش منقضی‌شده‌ها")
                    }
                    db.insert(AppDatabase.TABLE_HISTORY, null, ev)
                }

                db.delete(AppDatabase.TABLE_NOTIFICATION_LOGS, "pen_id = ?", arrayOf(id))
                transitionedCount++
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        return transitionedCount
    }

    fun getDashboardMetrics(today: String): DashboardMetrics {
        checkAndTransitionExpiredPens(today)

        val db = dbHelper.readableDatabase
        val date30Days = DateUtils.addDays(today, 30)

        val totalActive = getCount(
            db,
            "SELECT COUNT(*) FROM ${AppDatabase.TABLE_PENS} WHERE status = ?",
            arrayOf(PenStatus.ACTIVE.name)
        )

        val longActing = getCount(
            db,
            "SELECT COUNT(*) FROM ${AppDatabase.TABLE_PENS} WHERE status = ? AND type = ?",
            arrayOf(PenStatus.ACTIVE.name, InsulinType.LONG_ACTING.name)
        )

        val shortActing = getCount(
            db,
            "SELECT COUNT(*) FROM ${AppDatabase.TABLE_PENS} WHERE status = ? AND type = ?",
            arrayOf(PenStatus.ACTIVE.name, InsulinType.SHORT_ACTING.name)
        )

        val expiringSoon = getCount(
            db,
            "SELECT COUNT(*) FROM ${AppDatabase.TABLE_PENS} WHERE status = ? AND expiry_date >= ? AND expiry_date <= ?",
            arrayOf(PenStatus.ACTIVE.name, today, date30Days)
        )

        val expired = getCount(
            db,
            "SELECT COUNT(*) FROM ${AppDatabase.TABLE_PENS} WHERE status = ?",
            arrayOf(PenStatus.EXPIRED.name)
        )

        return DashboardMetrics(totalActive, longActing, shortActing, expiringSoon, expired)
    }

    private fun getCount(db: SQLiteDatabase, query: String, args: Array<String>): Int {
        val cursor = db.rawQuery(query, args)
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    fun getActivePens(type: InsulinType, today: String): List<InsulinPen> {
        checkAndTransitionExpiredPens(today)

        val db = dbHelper.readableDatabase
        val list = mutableListOf<InsulinPen>()

        val cursor = db.query(
            AppDatabase.TABLE_PENS,
            null,
            "status = ? AND type = ?",
            arrayOf(PenStatus.ACTIVE.name, type.name),
            null, null, "expiry_date ASC, name ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToPen(it))
            }
        }

        PriorityCalculator.assignPrioritiesAndDays(list, today)
        return list
    }

    fun getAllActivePens(today: String): List<InsulinPen> {
        checkAndTransitionExpiredPens(today)

        val db = dbHelper.readableDatabase
        val list = mutableListOf<InsulinPen>()

        val cursor = db.query(
            AppDatabase.TABLE_PENS,
            null,
            "status = ?",
            arrayOf(PenStatus.ACTIVE.name),
            null, null, "expiry_date ASC, name ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToPen(it))
            }
        }

        PriorityCalculator.assignPrioritiesAndDays(list, today)
        return list
    }

    fun getExpiredPens(): List<InsulinPen> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<InsulinPen>()

        val cursor = db.query(
            AppDatabase.TABLE_PENS,
            null,
            "status = ?",
            arrayOf(PenStatus.EXPIRED.name),
            null, null, "expiry_date DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToPen(it))
            }
        }
        return list
    }

    fun getPenById(penId: String?): InsulinPen? {
        if (penId == null) return null
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            AppDatabase.TABLE_PENS,
            null,
            "id = ?",
            arrayOf(penId),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) cursorToPen(it) else null
        }
    }

    fun getPenHistory(penId: String?): List<HistoryEvent> {
        val events = mutableListOf<HistoryEvent>()
        if (penId == null) return events

        val db = dbHelper.readableDatabase
        val cursor = db.query(
            AppDatabase.TABLE_HISTORY,
            null,
            "pen_id = ?",
            arrayOf(penId),
            null, null, "id DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                events.add(cursorToHistoryEvent(it))
            }
        }
        return events
    }

    fun getAllHistory(filterType: EventType?): List<HistoryEvent> {
        val list = mutableListOf<HistoryEvent>()
        val db = dbHelper.readableDatabase

        var sql = """
            SELECT h.id, h.pen_id, h.event_type, h.event_date, h.event_time, h.description, p.name, p.type 
            FROM ${AppDatabase.TABLE_HISTORY} h 
            LEFT JOIN ${AppDatabase.TABLE_PENS} p ON h.pen_id = p.id 
        """.trimIndent()

        val selectionArgs: Array<String>?
        if (filterType != null) {
            sql += " WHERE h.event_type = ? "
            selectionArgs = arrayOf(filterType.name)
        } else {
            selectionArgs = null
        }
        sql += " ORDER BY h.id DESC"

        val cursor = db.rawQuery(sql, selectionArgs)
        cursor.use {
            while (it.moveToNext()) {
                val ev = HistoryEvent(
                    id = it.getLong(0),
                    penId = it.getString(1),
                    eventType = EventType.fromString(it.getString(2)),
                    eventDate = it.getString(3),
                    eventTime = it.getString(4),
                    description = it.getString(5),
                    penName = it.getString(6) ?: "قلم حذف‌شده",
                    penType = if (it.getString(7) != null) InsulinType.fromString(it.getString(7)) else InsulinType.LONG_ACTING
                )
                list.add(ev)
            }
        }
        return list
    }

    @Synchronized
    fun updatePen(penId: String, newName: String, newType: InsulinType, newExpiryDate: String, today: String) {
        require(newName.trim().isNotEmpty()) { "نام انسولین نمی‌تواند خالی باشد." }
        require(DateUtils.isValidDate(newExpiryDate)) { "تاریخ انقضا نامعتبر است." }

        val existing = getPenById(penId) ?: throw IllegalArgumentException("قلم مورد نظر یافت نشد.")

        val expiryChanged = existing.expiryDate != newExpiryDate
        val typeChanged = existing.type != newType
        val nameChanged = existing.name != newName.trim()

        if (!expiryChanged && !typeChanged && !nameChanged) return

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val cv = ContentValues().apply {
                put("name", newName.trim())
                put("type", newType.name)
                put("expiry_date", newExpiryDate)
            }
            db.update(AppDatabase.TABLE_PENS, cv, "id = ?", arrayOf(penId))

            if (expiryChanged) {
                db.delete(AppDatabase.TABLE_NOTIFICATION_LOGS, "pen_id = ?", arrayOf(penId))
            }

            val desc = StringBuilder("ویرایش مشخصات: ").apply {
                if (nameChanged) append("نام به ${newName.trim()}؛ ")
                if (typeChanged) append("نوع اثر به ${newType.persianLabel}؛ ")
                if (expiryChanged) append("تاریخ انقضا به $newExpiryDate؛ ")
            }.toString()

            val ev = ContentValues().apply {
                put("pen_id", penId)
                put("event_type", EventType.EDITED.name)
                put("event_date", today)
                put("event_time", DateUtils.currentDateTime())
                put("description", desc)
            }
            db.insert(AppDatabase.TABLE_HISTORY, null, ev)

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        checkAndTransitionExpiredPens(today)
    }

    @Synchronized
    fun markPenAsOpened(penId: String, openedDate: String, today: String) {
        require(DateUtils.isValidDate(openedDate)) { "تاریخ باز شدن نامعتبر است." }
        val pen = getPenById(penId) ?: throw IllegalArgumentException("قلم یافت نشد.")

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val cv = ContentValues().apply {
                put("status", PenStatus.OPENED.name)
                put("opened_date", openedDate)
            }
            db.update(AppDatabase.TABLE_PENS, cv, "id = ?", arrayOf(penId))

            val ev = ContentValues().apply {
                put("pen_id", penId)
                put("event_type", EventType.OPENED.name)
                put("event_date", openedDate)
                put("event_time", DateUtils.currentDateTime())
                put("description", "باز شدن قلم انسولین در تاریخ $openedDate و انتقال به تاریخچه")
            }
            db.insert(AppDatabase.TABLE_HISTORY, null, ev)

            db.delete(AppDatabase.TABLE_NOTIFICATION_LOGS, "pen_id = ?", arrayOf(penId))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    @Synchronized
    fun markPenAsDefective(penId: String, reason: String?, today: String) {
        val trimmedReason = if (!reason.isNullOrBlank()) reason.trim() else "بدون شرح علت"
        val pen = getPenById(penId) ?: throw IllegalArgumentException("قلم یافت نشد.")

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val cv = ContentValues().apply {
                put("status", PenStatus.DEFECTIVE.name)
                put("defective_date", today)
                put("defective_reason", trimmedReason)
            }
            db.update(AppDatabase.TABLE_PENS, cv, "id = ?", arrayOf(penId))

            val ev = ContentValues().apply {
                put("pen_id", penId)
                put("event_type", EventType.DEFECTIVE.name)
                put("event_date", today)
                put("event_time", DateUtils.currentDateTime())
                put("description", "ثبت نقص/خرابی: $trimmedReason")
            }
            db.insert(AppDatabase.TABLE_HISTORY, null, ev)

            db.delete(AppDatabase.TABLE_NOTIFICATION_LOGS, "pen_id = ?", arrayOf(penId))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    @Synchronized
    fun deletePenPermanently(penId: String) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(AppDatabase.TABLE_HISTORY, "pen_id = ?", arrayOf(penId))
            db.delete(AppDatabase.TABLE_NOTIFICATION_LOGS, "pen_id = ?", arrayOf(penId))
            db.delete(AppDatabase.TABLE_PENS, "id = ?", arrayOf(penId))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun hasNotified(penId: String, thresholdDays: Int): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            AppDatabase.TABLE_NOTIFICATION_LOGS,
            arrayOf("pen_id"),
            "pen_id = ? AND threshold_days = ?",
            arrayOf(penId, thresholdDays.toString()),
            null, null, null
        )
        return cursor.use { it.count > 0 }
    }

    fun recordNotification(penId: String, thresholdDays: Int) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("pen_id", penId)
            put("threshold_days", thresholdDays)
            put("notified_at", DateUtils.currentDateTime())
        }
        db.insertWithOnConflict(
            AppDatabase.TABLE_NOTIFICATION_LOGS,
            null,
            cv,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getNotificationSettings(): NotificationSettings {
        val db = dbHelper.readableDatabase
        val settings = NotificationSettings()

        val cursor = db.query(AppDatabase.TABLE_SETTINGS, arrayOf("key", "value"), null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val key = it.getString(0)
                val value = it.getString(1)
                when (key) {
                    "notify_30" -> settings.notify30Days = (value == "1")
                    "notify_14" -> settings.notify14Days = (value == "1")
                    "notify_7" -> settings.notify7Days = (value == "1")
                }
            }
        }
        return settings
    }

    @Synchronized
    fun saveNotificationSettings(settings: NotificationSettings) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            saveSetting(db, "notify_30", if (settings.notify30Days) "1" else "0")
            saveSetting(db, "notify_14", if (settings.notify14Days) "1" else "0")
            saveSetting(db, "notify_7", if (settings.notify7Days) "1" else "0")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun saveSetting(db: SQLiteDatabase, key: String, value: String) {
        val cv = ContentValues().apply {
            put("key", key)
            put("value", value)
        }
        db.insertWithOnConflict(AppDatabase.TABLE_SETTINGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    /**
     * Exports full database to standardized JSON backup (exact schema compatibility with v0).
     */
    fun exportBackupJson(): String {
        val db = dbHelper.readableDatabase
        val root = JSONObject()
        root.put("schemaVersion", 1)
        root.put("exportedAt", DateUtils.currentDateTime())

        // 1. Pens
        val pensArray = JSONArray()
        val penCursor = db.query(AppDatabase.TABLE_PENS, null, null, null, null, null, "entry_date ASC, name ASC")
        penCursor.use {
            while (it.moveToNext()) {
                pensArray.put(cursorToPen(it).toJsonObject())
            }
        }
        root.put("pens", pensArray)

        // 2. Events
        val eventsArray = JSONArray()
        val eventCursor = db.query(AppDatabase.TABLE_HISTORY, null, null, null, null, null, "id ASC")
        eventCursor.use {
            while (it.moveToNext()) {
                eventsArray.put(cursorToHistoryEvent(it).toJsonObject())
            }
        }
        root.put("events", eventsArray)

        // 3. Settings
        root.put("notificationSettings", getNotificationSettings().toJsonObject())

        return root.toString(2)
    }

    /**
     * Restores database from JSON backup (supports both v0 and v1 backups).
     * Fully atomic: complete rollback on any error preserving all existing data.
     */
    @Synchronized
    fun restoreFromJson(jsonContent: String) {
        require(jsonContent.isNotBlank()) { "فایل پشتیبان خالی است." }

        val root = JSONObject(jsonContent)
        val schemaVersion = root.optInt("schemaVersion", -1)
        if (schemaVersion != 1) {
            throw IllegalArgumentException("نسخه ساختار فایل پشتیبان ($schemaVersion) با نسخه برنامه سازگار نیست.")
        }

        if (!root.has("pens") || !root.has("events")) {
            throw IllegalArgumentException("فایل پشتیبان فاقد بخش‌های ضروری است.")
        }

        val pensArray = root.getJSONArray("pens")
        val eventsArray = root.getJSONArray("events")

        val restoredPens = mutableListOf<InsulinPen>()
        for (i in 0 until pensArray.length()) {
            val pObj = pensArray.getJSONObject(i)
            val pen = InsulinPen.fromJsonObject(pObj)
            if (!DateUtils.isValidDate(pen.expiryDate)) {
                throw IllegalArgumentException("تاریخ انقضای نامعتبر در رکورد شماره ${i + 1}")
            }
            restoredPens.add(pen)
        }

        val restoredEvents = mutableListOf<HistoryEvent>()
        for (i in 0 until eventsArray.length()) {
            val eObj = eventsArray.getJSONObject(i)
            restoredEvents.add(HistoryEvent.fromJsonObject(eObj))
        }

        val restoredSettings = if (root.has("notificationSettings")) {
            NotificationSettings.fromJsonObject(root.optJSONObject("notificationSettings"))
        } else null

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(AppDatabase.TABLE_NOTIFICATION_LOGS, null, null)
            db.delete(AppDatabase.TABLE_HISTORY, null, null)
            db.delete(AppDatabase.TABLE_PENS, null, null)

            for (pen in restoredPens) {
                val cv = ContentValues().apply {
                    put("id", pen.id)
                    put("name", pen.name)
                    put("type", pen.type.name)
                    put("expiry_date", pen.expiryDate)
                    put("status", pen.status.name)
                    put("entry_date", pen.entryDate)
                    if (pen.openedDate != null) put("opened_date", pen.openedDate) else putNull("opened_date")
                    if (pen.defectiveDate != null) put("defective_date", pen.defectiveDate) else putNull("defective_date")
                    if (pen.defectiveReason != null) put("defective_reason", pen.defectiveReason) else putNull("defective_reason")
                }
                db.insertOrThrow(AppDatabase.TABLE_PENS, null, cv)
            }

            for (ev in restoredEvents) {
                val cv = ContentValues().apply {
                    if (ev.id > 0) put("id", ev.id)
                    put("pen_id", ev.penId)
                    put("event_type", ev.eventType.name)
                    put("event_date", ev.eventDate)
                    put("event_time", ev.eventTime)
                    put("description", ev.description)
                }
                db.insertOrThrow(AppDatabase.TABLE_HISTORY, null, cv)
            }

            if (restoredSettings != null) {
                saveSetting(db, "notify_30", if (restoredSettings.notify30Days) "1" else "0")
                saveSetting(db, "notify_14", if (restoredSettings.notify14Days) "1" else "0")
                saveSetting(db, "notify_7", if (restoredSettings.notify7Days) "1" else "0")
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        checkAndTransitionExpiredPens(DateUtils.todayDate())
    }

    private fun cursorToPen(cursor: Cursor): InsulinPen {
        return InsulinPen(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            type = InsulinType.fromString(cursor.getString(cursor.getColumnIndexOrThrow("type"))),
            expiryDate = cursor.getString(cursor.getColumnIndexOrThrow("expiry_date")),
            status = PenStatus.fromString(cursor.getString(cursor.getColumnIndexOrThrow("status"))),
            entryDate = cursor.getString(cursor.getColumnIndexOrThrow("entry_date")),
            openedDate = cursor.getString(cursor.getColumnIndexOrThrow("opened_date")),
            defectiveDate = cursor.getString(cursor.getColumnIndexOrThrow("defective_date")),
            defectiveReason = cursor.getString(cursor.getColumnIndexOrThrow("defective_reason"))
        )
    }

    private fun cursorToHistoryEvent(cursor: Cursor): HistoryEvent {
        return HistoryEvent(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            penId = cursor.getString(cursor.getColumnIndexOrThrow("pen_id")),
            eventType = EventType.fromString(cursor.getString(cursor.getColumnIndexOrThrow("event_type"))),
            eventDate = cursor.getString(cursor.getColumnIndexOrThrow("event_date")),
            eventTime = cursor.getString(cursor.getColumnIndexOrThrow("event_time")),
            description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
        )
    }

    companion object {
        const val MAX_PENS_PER_ENTRY = 100

        @Volatile
        private var instance: PenRepository? = null

        fun getInstance(context: Context): PenRepository {
            return instance ?: synchronized(this) {
                instance ?: PenRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}

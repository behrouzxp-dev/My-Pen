package com.mypen.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_PENS (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                expiry_date TEXT NOT NULL,
                status TEXT NOT NULL,
                entry_date TEXT NOT NULL,
                opened_date TEXT,
                defective_date TEXT,
                defective_reason TEXT
            );
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX idx_pens_status_type ON $TABLE_PENS(status, type);")
        db.execSQL("CREATE INDEX idx_pens_expiry ON $TABLE_PENS(expiry_date);")

        db.execSQL(
            """
            CREATE TABLE $TABLE_HISTORY (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pen_id TEXT NOT NULL,
                event_type TEXT NOT NULL,
                event_date TEXT NOT NULL,
                event_time TEXT NOT NULL,
                description TEXT NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX idx_history_pen ON $TABLE_HISTORY(pen_id);")
        db.execSQL("CREATE INDEX idx_history_type ON $TABLE_HISTORY(event_type);")

        db.execSQL(
            """
            CREATE TABLE $TABLE_NOTIFICATION_LOGS (
                pen_id TEXT NOT NULL,
                threshold_days INTEGER NOT NULL,
                notified_at TEXT NOT NULL,
                PRIMARY KEY(pen_id, threshold_days)
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_SETTINGS (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            );
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migrations for future updates
    }

    companion object {
        private const val DATABASE_NAME = "mypen_database.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_PENS = "pens"
        const val TABLE_HISTORY = "history_events"
        const val TABLE_NOTIFICATION_LOGS = "notification_logs"
        const val TABLE_SETTINGS = "settings"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: AppDatabase(context.applicationContext).also { instance = it }
            }
        }
    }
}

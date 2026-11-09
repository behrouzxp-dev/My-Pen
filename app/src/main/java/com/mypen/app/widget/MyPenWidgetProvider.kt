package com.mypen.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.mypen.app.MainActivity
import com.mypen.app.R
import com.mypen.app.data.PenRepository
import com.mypen.app.util.DateUtils

/**
 * Home screen widget that always answers one question: which pens expire next?
 *
 * Implemented with the platform `RemoteViews` API (no extra dependency) and the
 * same database asthe application, so it never shows stale data for long.
 */
class MyPenWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val views = buildRemoteViews(context)
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {

        private const val MAX_ROWS = 3

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(
                ComponentName(context, MyPenWidgetProvider::class.java)
            )
            if (ids.isEmpty()) return
            val views = buildRemoteViews(context)
            ids.forEach { id -> manager.updateAppWidget(id, views) }
        }

        private fun buildRemoteViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_mypen)
            val today = DateUtils.todayDate()

            val pens = runCatching {
                PenRepository.getInstance(context)
                    .getAllActivePens(today)
                    .sortedBy { it.expiryDate }
                    .take(MAX_ROWS)
            }.getOrDefault(emptyList())

            val rowIds = intArrayOf(
                R.id.widget_row1,
                R.id.widget_row2,
                R.id.widget_row3
            )
            val nameIds = intArrayOf(
                R.id.widget_row1_name,
                R.id.widget_row2_name,
                R.id.widget_row3_name
            )
            val daysIds = intArrayOf(
                R.id.widget_row1_days,
                R.id.widget_row2_days,
                R.id.widget_row3_days
            )
            val dateIds = intArrayOf(
                R.id.widget_row1_date,
                R.id.widget_row2_date,
                R.id.widget_row3_date
            )

            for (index in 0 until MAX_ROWS) {
                val pen = pens.getOrNull(index)
                if (pen == null) {
                    views.setViewVisibility(rowIds[index], View.GONE)
                    continue
                }
                val daysRemaining = DateUtils.calculateDaysRemaining(pen.expiryDate, today)
                val accent = when {
                    daysRemaining <= 0L -> R.color.widget_accent_expired
                    daysRemaining <= 30L -> R.color.widget_accent_warning
                    else -> R.color.widget_accent_ok
                }
                views.setViewVisibility(rowIds[index], View.VISIBLE)
                views.setTextViewText(nameIds[index], pen.name)
                views.setTextColor(nameIds[index], ContextCompat.getColor(context, R.color.widget_text_primary))
                views.setTextViewText(daysIds[index], DateUtils.remainingLabel(daysRemaining))
                views.setTextColor(daysIds[index], ContextCompat.getColor(context, accent))
                views.setTextViewText(dateIds[index], DateUtils.displayDate(pen.expiryDate))
                views.setTextColor(dateIds[index], ContextCompat.getColor(context, R.color.widget_text_muted))
            }

            if (pens.isEmpty()) {
                views.setViewVisibility(R.id.widget_empty, View.VISIBLE)
                views.setTextViewText(
                    R.id.widget_empty,
                    context.getString(R.string.widget_empty)
                )
            } else {
                views.setViewVisibility(R.id.widget_empty, View.GONE)
            }

            views.setTextViewText(
                R.id.widget_title,
                context.getString(R.string.widget_title)
            )
            views.setTextViewText(
                R.id.widget_subtitle,
                context.getString(R.string.widget_subtitle)
            )

            val openIntent = Intent(context, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_OPEN_TAB, MainActivity.OPEN_TAB_INVENTORY)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            return views
        }
    }
}

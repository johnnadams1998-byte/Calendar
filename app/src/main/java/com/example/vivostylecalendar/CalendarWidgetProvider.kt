package com.example.vivostylecalendar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class CalendarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateAllWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        super.onReceive(context, intent)

        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {

                val manager =
                    AppWidgetManager.getInstance(context)

                val component =
                    ComponentName(
                        context,
                        CalendarWidgetProvider::class.java
                    )

                val ids =
                    manager.getAppWidgetIds(component)

                updateAllWidgets(
                    context,
                    manager,
                    ids
                )
            }
        }
    }

    private fun updateAllWidgets(
        context: Context,
        manager: AppWidgetManager,
        ids: IntArray
    ) {

        val today = LocalDate.now()

        val dayName =
            today.dayOfWeek.getDisplayName(
                TextStyle.SHORT,
                Locale.getDefault()
            )

        val dateNumber =
            today.dayOfMonth.toString()

        ids.forEach { widgetId ->

            val views =
                RemoteViews(
                    context.packageName,
                    R.layout.widget_calendar
                )

            views.setTextViewText(
                R.id.widgetDay,
                dayName
            )

            views.setTextViewText(
                R.id.widgetDate,
                dateNumber
            )

            val launchIntent =
                Intent(
                    context,
                    MainActivity::class.java
                )

            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
                )

            views.setOnClickPendingIntent(
                R.id.widgetDay,
                pendingIntent
            )

            views.setOnClickPendingIntent(
                R.id.widgetDate,
                pendingIntent
            )

            manager.updateAppWidget(
                widgetId,
                views
            )
        }
    }
}

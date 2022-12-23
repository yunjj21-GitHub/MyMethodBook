package com.example.mymethodbook.widget

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.mymethodbook.MyApplication
import com.example.mymethodbook.R
import com.example.mymethodbook.activity.MainActivity

class ExampleAppWidgetProvider : AppWidgetProvider() {
    // [필수 구현 요소] 앱 위젯을 업데이트할 때 실행된다.
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let { intent ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.getActivity(context, 0, intent, FLAG_MUTABLE)
                    } else {
                        PendingIntent.getActivity(context, 0, intent, 0)
                    }
                }

            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.example_widget
            ).apply {
                setOnClickPendingIntent(R.id.example_widget_layout, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
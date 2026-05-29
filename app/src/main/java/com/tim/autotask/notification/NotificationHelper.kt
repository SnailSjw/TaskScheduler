package com.tim.autotask.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.tim.autotask.AutoTaskApp
import com.tim.autotask.MainActivity
import com.tim.autotask.R

class NotificationHelper(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showTaskCompleteNotification(
        taskId: Long,
        taskName: String,
        appLabel: String,
        permissions: List<String>
    ) {
        val contentText = buildString {
            appendLine("已启动应用：$appLabel")
            if (permissions.isNotEmpty()) {
                appendLine("使用权限：${permissions.joinToString("、")}")
            }
        }

        val detailIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to_task", taskId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingDetail = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, AutoTaskApp.CHANNEL_TASK)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("任务完成：$taskName")
            .setContentText(contentText.trim())
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText.trim()))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingDetail)
            .build()

        notificationManager.notify(taskId.toInt(), notification)
    }

    fun buildForegroundNotification(taskName: String): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, AutoTaskApp.CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("定位监控中")
            .setContentText("任务：$taskName")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }
}

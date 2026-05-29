package com.tim.autotask.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.tim.autotask.data.model.RepeatType
import com.tim.autotask.data.model.Task
import com.tim.autotask.data.model.TaskType

class TaskScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleTask(task: Task) {
        if (task.scheduledTime <= System.currentTimeMillis() && task.repeatType == RepeatType.NONE) {
            Log.w(TAG, "任务时间已过，跳过调度: ${task.name}")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TASK_TRIGGER
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TYPE, task.type.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        when (task.repeatType) {
            RepeatType.NONE -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    task.scheduledTime,
                    pendingIntent
                )
            }
            RepeatType.DAILY -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    task.scheduledTime,
                    pendingIntent
                )
            }
            RepeatType.WEEKLY -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    task.scheduledTime,
                    pendingIntent
                )
            }
        }

        Log.i(TAG, "任务已调度: ${task.name} at ${task.scheduledTime}")
    }

    fun cancelTask(taskId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TASK_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.i(TAG, "任务已取消: $taskId")
    }

    fun rescheduleRepeatingTask(task: Task) {
        val nextTime = calculateNextTriggerTime(task)
        if (nextTime > System.currentTimeMillis()) {
            val updatedTask = task.copy(scheduledTime = nextTime)
            scheduleTask(updatedTask)
        }
    }

    private fun calculateNextTriggerTime(task: Task): Long {
        val now = System.currentTimeMillis()
        var next = task.scheduledTime
        return when (task.repeatType) {
            RepeatType.NONE -> next
            RepeatType.DAILY -> {
                while (next <= now) next += DAY_MILLIS
                next
            }
            RepeatType.WEEKLY -> {
                while (next <= now) next += WEEK_MILLIS
                next
            }
        }
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    companion object {
        private const val TAG = "TaskScheduler"
        const val ACTION_TASK_TRIGGER = "com.tim.autotask.ACTION_TASK_TRIGGER"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TYPE = "task_type"
        private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
        private const val WEEK_MILLIS = 7 * DAY_MILLIS
    }
}

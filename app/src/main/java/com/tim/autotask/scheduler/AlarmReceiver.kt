package com.tim.autotask.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.tim.autotask.AutoTaskApp
import com.tim.autotask.data.model.LogLevel
import com.tim.autotask.data.model.TaskStatus
import com.tim.autotask.data.model.TaskType
import com.tim.autotask.notification.NotificationHelper
import com.tim.autotask.service.LocationMonitorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TaskScheduler.ACTION_TASK_TRIGGER) return

        val taskId = intent.getLongExtra(TaskScheduler.EXTRA_TASK_ID, -1)
        val taskTypeName = intent.getStringExtra(TaskScheduler.EXTRA_TASK_TYPE) ?: return

        if (taskId == -1L) return

        Log.i(TAG, "闹钟触发: taskId=$taskId, type=$taskTypeName")

        val wakeLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AutoTask::AlarmWakeLock_$taskId")
            .apply { acquire(30_000) }

        val app = context.applicationContext as AutoTaskApp
        val repository = app.repository

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = repository.getTask(taskId)
                if (task == null) {
                    Log.w(TAG, "任务不存在: $taskId")
                    return@launch
                }

                if (!task.isEnabled) {
                    Log.i(TAG, "任务已禁用，跳过: ${task.name}")
                    return@launch
                }

                repository.updateTaskStatus(taskId, TaskStatus.EXECUTING)
                repository.addLog(taskId, "定时触发已激活", LogLevel.INFO)

                when (task.type) {
                    TaskType.SCHEDULED_LAUNCH -> {
                        repository.addLog(taskId, "准备启动应用: ${task.targetAppLabel}", LogLevel.INFO)
                        launchApp(context, task.targetPackageName, task.targetAppLabel)
                        repository.addLog(taskId, "应用启动成功: ${task.targetAppLabel}", LogLevel.SUCCESS)

                        NotificationHelper(context).showTaskCompleteNotification(
                            taskId = taskId,
                            taskName = task.name,
                            appLabel = task.targetAppLabel,
                            permissions = listOf("定时闹钟")
                        )
                        repository.addLog(taskId, "已发送通知栏通知", LogLevel.INFO)

                        repository.updateTaskStatus(taskId, TaskStatus.COMPLETED)
                        repository.addLog(taskId, "任务执行完成", LogLevel.SUCCESS)
                    }

                    TaskType.LOCATION_TRIGGERED -> {
                        repository.addLog(taskId, "启动定位监控服务", LogLevel.INFO)
                        LocationMonitorService.start(context, taskId)
                    }
                }

                if (task.repeatType != com.tim.autotask.data.model.RepeatType.NONE) {
                    val scheduler = TaskScheduler(context)
                    scheduler.rescheduleRepeatingTask(task)
                    repository.addLog(taskId, "已重新调度下一次执行", LogLevel.INFO)
                }
            } catch (e: Exception) {
                Log.e(TAG, "任务执行异常: $taskId", e)
                try {
                    repository.updateTaskStatus(taskId, TaskStatus.FAILED)
                    repository.addLog(taskId, "任务执行失败: ${e.message}", LogLevel.ERROR)
                } catch (_: Exception) {}
            } finally {
                wakeLock.release()
                pendingResult.finish()
            }
        }
    }

    private fun launchApp(context: Context, packageName: String, appLabel: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            Log.i(TAG, "已启动应用: $appLabel ($packageName)")
        } else {
            Log.e(TAG, "无法启动应用: $packageName")
            throw RuntimeException("无法找到应用启动 Intent: $appLabel")
        }
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}

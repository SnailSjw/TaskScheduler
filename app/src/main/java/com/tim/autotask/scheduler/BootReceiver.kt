package com.tim.autotask.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tim.autotask.AutoTaskApp
import com.tim.autotask.data.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.i(TAG, "设备已启动，恢复所有活跃任务")

        val app = context.applicationContext as AutoTaskApp
        val scheduler = TaskScheduler(context)
        val repository = app.repository

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val activeTasks = repository.getActiveTasks()
                Log.i(TAG, "需要恢复 ${activeTasks.size} 个任务")

                activeTasks.forEach { task ->
                    if (task.status == TaskStatus.PENDING || task.status == TaskStatus.EXECUTING) {
                        scheduler.scheduleTask(task)
                        Log.i(TAG, "已恢复任务: ${task.name}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "恢复任务失败", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}

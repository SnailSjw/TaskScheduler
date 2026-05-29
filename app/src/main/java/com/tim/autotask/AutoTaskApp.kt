package com.tim.autotask

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.room.Room
import com.tim.autotask.data.db.AppDatabase
import com.tim.autotask.data.repository.TaskRepository

class AutoTaskApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: TaskRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "autotask.db"
        ).build()

        repository = TaskRepository(
            database.taskDao(),
            database.taskLogDao()
        )

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val taskChannel = NotificationChannel(
            CHANNEL_TASK,
            "任务执行通知",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "任务执行完成后的通知"
        }

        val serviceChannel = NotificationChannel(
            CHANNEL_SERVICE,
            "后台服务",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "定位监控服务运行中"
        }

        manager.createNotificationChannel(taskChannel)
        manager.createNotificationChannel(serviceChannel)
    }

    companion object {
        lateinit var instance: AutoTaskApp
            private set

        const val CHANNEL_TASK = "task_notification"
        const val CHANNEL_SERVICE = "service_notification"
    }
}

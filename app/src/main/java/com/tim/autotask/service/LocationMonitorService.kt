package com.tim.autotask.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.tim.autotask.AutoTaskApp
import com.tim.autotask.data.model.LogLevel
import com.tim.autotask.data.model.TaskStatus
import com.tim.autotask.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LocationMonitorService : Service(), AMapLocationListener {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var locationClient: AMapLocationClient? = null
    private var taskId: Long = -1L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        taskId = intent?.getLongExtra(EXTRA_TASK_ID, -1L) ?: -1L

        if (taskId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }

        val app = application as AutoTaskApp
        val repository = app.repository

        scope.launch {
            val task = repository.getTask(taskId)
            if (task == null) {
                stopSelf()
                return@launch
            }

            val notification = NotificationHelper(this@LocationMonitorService)
                .buildForegroundNotification(task.name)
            startForeground(NOTIFICATION_ID, notification)

            repository.addLog(taskId, "定位监控服务已启动", LogLevel.INFO)

            try {
                locationClient = AMapLocationClient(applicationContext).apply {
                    val option = AMapLocationClientOption().apply {
                        locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                        isOnceLocation = false
                        interval = 5000
                        isNeedAddress = false
                        isMockEnable = false
                    }
                    setLocationOption(option)
                    setLocationListener(this@LocationMonitorService)
                    startLocation()
                }
                repository.addLog(taskId, "高精度定位已开启，持续监听中", LogLevel.INFO)
            } catch (e: Exception) {
                Log.e(TAG, "定位初始化失败", e)
                repository.addLog(taskId, "定位初始化失败: ${e.message}", LogLevel.ERROR)
                repository.updateTaskStatus(taskId, TaskStatus.FAILED)
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (amapLocation == null) return

        if (amapLocation.errorCode != 0) {
            Log.e(TAG, "定位错误: ${amapLocation.errorInfo}")
            scope.launch {
                val repo = (application as AutoTaskApp).repository
                repo.addLog(taskId, "定位错误: ${amapLocation.errorInfo}", LogLevel.WARNING)
            }
            return
        }

        val lat = amapLocation.latitude
        val lng = amapLocation.longitude

        scope.launch {
            val repo = (application as AutoTaskApp).repository
            val task = repo.getTask(taskId) ?: return@launch

            if (task.targetLatitude == null || task.targetLongitude == null) {
                repo.addLog(taskId, "任务缺少目标坐标", LogLevel.ERROR)
                repo.updateTaskStatus(taskId, TaskStatus.FAILED)
                stopMonitoring()
                return@launch
            }

            val currentLoc = Location("amap").apply {
                latitude = lat
                longitude = lng
            }
            val targetLoc = Location("target").apply {
                latitude = task.targetLatitude
                longitude = task.targetLongitude
            }

            val distance = currentLoc.distanceTo(targetLoc)
            repo.addLog(
                taskId,
                "当前坐标: (${String.format("%.6f", lat)}, ${String.format("%.6f", lng)})，距目标: ${String.format("%.1f", distance)}米",
                LogLevel.INFO
            )

            if (distance <= task.locationRadius) {
                repo.addLog(
                    taskId,
                    "已到达目标范围（${String.format("%.1f", distance)}米 ≤ ${task.locationRadius}米），触发操作",
                    LogLevel.SUCCESS
                )

                val launchIntent = packageManager.getLaunchIntentForPackage(task.targetPackageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(launchIntent)
                    repo.addLog(taskId, "应用启动成功: ${task.targetAppLabel}", LogLevel.SUCCESS)
                } else {
                    repo.addLog(taskId, "无法启动应用: ${task.targetAppLabel}", LogLevel.ERROR)
                    repo.updateTaskStatus(taskId, TaskStatus.FAILED)
                    stopMonitoring()
                    return@launch
                }

                NotificationHelper(this@LocationMonitorService).showTaskCompleteNotification(
                    taskId = taskId,
                    taskName = task.name,
                    appLabel = task.targetAppLabel,
                    permissions = listOf("精确定位", "后台定位")
                )
                repo.addLog(taskId, "已发送通知栏通知", LogLevel.INFO)

                repo.updateTaskStatus(taskId, TaskStatus.COMPLETED)
                repo.addLog(taskId, "任务执行完成，停止定位监控", LogLevel.SUCCESS)

                stopMonitoring()
            }
        }
    }

    private fun stopMonitoring() {
        locationClient?.stopLocation()
        locationClient?.onDestroy()
        locationClient = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        locationClient?.stopLocation()
        locationClient?.onDestroy()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "LocationMonitor"
        private const val EXTRA_TASK_ID = "task_id"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context, taskId: Long) {
            val intent = Intent(context, LocationMonitorService::class.java).apply {
                putExtra(EXTRA_TASK_ID, taskId)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LocationMonitorService::class.java))
        }
    }
}

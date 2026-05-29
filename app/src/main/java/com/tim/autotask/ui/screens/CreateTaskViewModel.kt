package com.tim.autotask.ui.screens

import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tim.autotask.AutoTaskApp
import com.tim.autotask.data.model.RepeatType
import com.tim.autotask.data.model.Task
import com.tim.autotask.data.model.TaskType
import com.tim.autotask.scheduler.TaskScheduler
import kotlinx.coroutines.launch
import java.util.Calendar

class CreateTaskViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AutoTaskApp
    private val repository = app.repository
    private val scheduler = TaskScheduler(application)

    fun createTask(
        name: String,
        type: TaskType,
        scheduledTime: Long,
        repeatType: RepeatType,
        targetPackageName: String,
        targetAppLabel: String,
        targetLatitude: Double?,
        targetLongitude: Double?,
        targetAddress: String?,
        locationRadius: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val task = Task(
                name = name,
                type = type,
                scheduledTime = scheduledTime,
                repeatType = repeatType,
                targetPackageName = targetPackageName,
                targetAppLabel = targetAppLabel,
                targetLatitude = targetLatitude,
                targetLongitude = targetLongitude,
                targetAddress = targetAddress,
                locationRadius = locationRadius
            )
            val id = repository.createTask(task)
            scheduler.scheduleTask(task.copy(id = id))
            onSuccess()
        }
    }
}

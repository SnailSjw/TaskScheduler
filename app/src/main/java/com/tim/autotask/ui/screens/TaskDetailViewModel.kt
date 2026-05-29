package com.tim.autotask.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tim.autotask.AutoTaskApp
import com.tim.autotask.data.model.Task
import com.tim.autotask.data.model.TaskLog
import com.tim.autotask.scheduler.TaskScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AutoTaskApp
    private val repository = app.repository
    private val scheduler = TaskScheduler(application)

    fun observeTask(taskId: Long): StateFlow<Task?> = repository.observeTask(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun observeLogs(taskId: Long): Flow<List<TaskLog>> = repository.observeTaskLogs(taskId)

    fun deleteTask(task: Task, onDeleted: () -> Unit) {
        viewModelScope.launch {
            scheduler.cancelTask(task.id)
            repository.deleteTask(task)
            onDeleted()
        }
    }

    fun resetTask(task: Task) {
        viewModelScope.launch {
            repository.updateTaskStatus(
                task.id,
                com.tim.autotask.data.model.TaskStatus.PENDING
            )
            scheduler.scheduleTask(task)
            repository.addLog(task.id, "任务已重置，重新调度", com.tim.autotask.data.model.LogLevel.INFO)
        }
    }
}

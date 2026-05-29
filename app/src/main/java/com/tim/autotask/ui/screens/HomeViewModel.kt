package com.tim.autotask.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tim.autotask.AutoTaskApp
import com.tim.autotask.data.model.Task
import com.tim.autotask.data.model.TaskStatus
import com.tim.autotask.scheduler.TaskScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AutoTaskApp
    private val repository = app.repository
    private val scheduler = TaskScheduler(application)

    val tasks: StateFlow<List<Task>> = repository.observeAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            val newEnabled = !task.isEnabled
            repository.setTaskEnabled(task.id, newEnabled)
            if (newEnabled && task.status == TaskStatus.PENDING) {
                scheduler.scheduleTask(task)
            } else {
                scheduler.cancelTask(task.id)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            scheduler.cancelTask(task.id)
            repository.deleteTask(task)
        }
    }

    fun executeTaskNow(task: Task) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val updated = task.copy(scheduledTime = now + 3000)
            repository.updateTask(updated)
            scheduler.scheduleTask(updated)
        }
    }
}

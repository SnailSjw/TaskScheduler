package com.tim.autotask.data.repository

import com.tim.autotask.data.db.TaskDao
import com.tim.autotask.data.db.TaskLogDao
import com.tim.autotask.data.model.LogLevel
import com.tim.autotask.data.model.Task
import com.tim.autotask.data.model.TaskLog
import com.tim.autotask.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val taskLogDao: TaskLogDao
) {
    fun observeAllTasks(): Flow<List<Task>> = taskDao.observeAll()

    fun observeTask(id: Long): Flow<Task?> = taskDao.observeById(id)

    fun observeTaskLogs(taskId: Long): Flow<List<TaskLog>> = taskLogDao.observeByTaskId(taskId)

    suspend fun getTask(id: Long): Task? = taskDao.getById(id)

    suspend fun getActiveTasks(): List<Task> = taskDao.getActiveTasks()

    suspend fun createTask(task: Task): Long = taskDao.insert(task)

    suspend fun updateTask(task: Task) {
        taskDao.update(task.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTask(task: Task) {
        taskLogDao.deleteByTaskId(task.id)
        taskDao.delete(task)
    }

    suspend fun updateTaskStatus(id: Long, status: TaskStatus) {
        taskDao.updateStatus(id, status)
    }

    suspend fun setTaskEnabled(id: Long, enabled: Boolean) {
        taskDao.setEnabled(id, enabled)
    }

    suspend fun addLog(
        taskId: Long,
        message: String,
        level: LogLevel = LogLevel.INFO
    ) {
        taskLogDao.insert(
            TaskLog(
                taskId = taskId,
                message = message,
                level = level
            )
        )
    }

    suspend fun getTaskLogs(taskId: Long): List<TaskLog> = taskLogDao.getByTaskId(taskId)
}

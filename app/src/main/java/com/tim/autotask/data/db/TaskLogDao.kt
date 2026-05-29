package com.tim.autotask.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tim.autotask.data.model.TaskLog
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskLogDao {

    @Query("SELECT * FROM task_logs WHERE taskId = :taskId ORDER BY timestamp ASC")
    fun observeByTaskId(taskId: Long): Flow<List<TaskLog>>

    @Query("SELECT * FROM task_logs WHERE taskId = :taskId ORDER BY timestamp ASC")
    suspend fun getByTaskId(taskId: Long): List<TaskLog>

    @Insert
    suspend fun insert(log: TaskLog): Long

    @Query("DELETE FROM task_logs WHERE taskId = :taskId")
    suspend fun deleteByTaskId(taskId: Long)
}

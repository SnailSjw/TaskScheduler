package com.tim.autotask.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tim.autotask.data.model.Task
import com.tim.autotask.data.model.TaskLog

@Database(
    entities = [Task::class, TaskLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskLogDao(): TaskLogDao
}

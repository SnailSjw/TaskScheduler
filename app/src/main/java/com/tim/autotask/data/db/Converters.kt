package com.tim.autotask.data.db

import androidx.room.TypeConverter
import com.tim.autotask.data.model.LogLevel
import com.tim.autotask.data.model.RepeatType
import com.tim.autotask.data.model.TaskStatus
import com.tim.autotask.data.model.TaskType

class Converters {
    @TypeConverter
    fun fromTaskType(value: TaskType): String = value.name

    @TypeConverter
    fun toTaskType(value: String): TaskType = TaskType.valueOf(value)

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun fromRepeatType(value: RepeatType): String = value.name

    @TypeConverter
    fun toRepeatType(value: String): RepeatType = RepeatType.valueOf(value)

    @TypeConverter
    fun fromLogLevel(value: LogLevel): String = value.name

    @TypeConverter
    fun toLogLevel(value: String): LogLevel = LogLevel.valueOf(value)
}

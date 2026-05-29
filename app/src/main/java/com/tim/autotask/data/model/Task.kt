package com.tim.autotask.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TaskType,
    val scheduledTime: Long,
    val repeatType: RepeatType = RepeatType.NONE,
    val targetPackageName: String,
    val targetAppLabel: String,
    val targetLatitude: Double? = null,
    val targetLongitude: Double? = null,
    val targetAddress: String? = null,
    val locationRadius: Int = 100,
    val status: TaskStatus = TaskStatus.PENDING,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

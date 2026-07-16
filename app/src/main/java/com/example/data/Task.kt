package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH"
    val dueDate: Long? = null, // timestamp
    val creationDate: Long = System.currentTimeMillis(),
    val reminderTime: Long? = null, // timestamp for when to show reminder
    val isReminderActive: Boolean = false
)

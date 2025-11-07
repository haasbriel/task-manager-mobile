package com.example.task_manager

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromTaskType(value: TaskType): String {
        return value.name
    }

    @TypeConverter
    fun toTaskType(value: String): TaskType {
        return TaskType.valueOf(value)
    }
}

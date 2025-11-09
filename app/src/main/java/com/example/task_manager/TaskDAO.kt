package com.example.task_manager

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TaskDAO {
    @Insert
    suspend fun create(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE task SET completed = 1 WHERE id = :taskId")
    suspend fun markAsCompleted(taskId: Int)

    @Query("UPDATE task SET completed = 0 WHERE id = :taskId")
    suspend fun markAsUncompleted(taskId: Int)

    @Query("SELECT * FROM task WHERE type = :type AND completed = 0")
    suspend fun getTasksByType(type: TaskType): List<Task>

    @Query("SELECT * FROM task WHERE completed = 1 ORDER BY id DESC")
    suspend fun getCompletedTasks(): List<Task>

    @Query("SELECT * FROM task ORDER BY id DESC")
    suspend fun getAllTasks(): List<Task>
}
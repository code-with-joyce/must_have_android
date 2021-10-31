package com.example.todolist.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao // 어떤 구성요소인지 알려주려면 꼭 어노테이션을 써야 합니다.
interface ToDoDao {
    @Query("SELECT * FROM ToDoEntity")
    fun getAll() : List<ToDoEntity>

    @Insert
    fun insertTodo(todo: ToDoEntity)

    @Delete
    fun deleteTodo(todo : ToDoEntity)
}

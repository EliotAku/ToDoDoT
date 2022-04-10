package fr.yashubeta.tododot.database

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    // --> Modifiers

    @Insert
    suspend fun insertTodo(todo: Todo)

    @Update(onConflict = REPLACE)
    suspend fun updateTodos(todos: List<Todo>)

    @Update(onConflict = REPLACE)
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodos(todos: List<Todo>)

    @Delete
    suspend fun deleteTodo(todo: Todo)

    // --> Getters

    @Query("SELECT * FROM todos ORDER BY position ASC")
    fun getTodos(): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE isChecked = 0 ORDER BY position ASC")
    fun getUncheckedTodos(): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE isChecked = 1 ORDER BY position ASC")
    fun getCheckedTodos(): Flow<List<Todo>>

    @Query("SELECT MAX(position) FROM todos")
    suspend fun getHighestPosition(): Int

}
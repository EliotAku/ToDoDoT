package fr.yashubeta.tododot.database

import kotlinx.coroutines.flow.Flow


class TodoRepository(private val todoDao: TodoDao) {

    // --> Modify
    suspend fun insertTodo(todo: Todo) { todoDao.insertTodo(todo) }
    suspend fun updateTodos(todos: List<Todo>) { todoDao.updateTodos(todos) }
    suspend fun updateTodo(todo: Todo) { todoDao.updateTodo(todo) }
    suspend fun deleteTodos(todos: List<Todo>) { todoDao.deleteTodos(todos) }
    suspend fun deleteTodo(todo: Todo) { todoDao.deleteTodo(todo) }

    // --> Get
    fun allTodosByIsChecked(): Flow<List<Todo>> = todoDao.getTodosByIsChecked()
    fun uncheckedTodos(): Flow<List<Todo>> = todoDao.getUncheckedTodos()
    fun checkedTodos(): Flow<List<Todo>> = todoDao.getCheckedTodos()
    suspend fun getHighestPosition(): Int = todoDao.getHighestPosition()

}
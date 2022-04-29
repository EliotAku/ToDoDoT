package fr.yashubeta.tododot

import android.app.Application
import androidx.lifecycle.*
import fr.yashubeta.tododot.database.AppDatabase
import fr.yashubeta.tododot.database.Todo
import fr.yashubeta.tododot.database.TodoRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = TodoRepository(AppDatabase.getDatabase(application).todoDao())

    private val vms = viewModelScope

    // --> Modify
    fun insertTodo(todo: Todo) {
        vms.launch {
            repo.insertTodo(todo.apply { position = getHighestPosition()?.plus(1) ?: 0 })
        }
    }

    fun updateTodos(todos: List<Todo>) {
        vms.launch { repo.updateTodos(todos) }
    }

    fun updateTodo(todo: Todo) {
        vms.launch { repo.updateTodo(todo) }
    }

    fun deleteTodos(todos: List<Todo>) {
        vms.launch { repo.deleteTodos(todos) }
    }

    fun deleteTodo(todo: Todo) {
        vms.launch {
            deletedTodo.value = todo
            repo.deleteTodo(todo)
        }
    }

    // --> Get
    fun allTodosByIsChecked(): LiveData<List<Todo>> = repo.allTodosByIsChecked().asLiveData()
    fun uncheckedTodos(): LiveData<List<Todo>> = repo.uncheckedTodos().asLiveData()
    fun checkedTodos(): LiveData<List<Todo>> = repo.checkedTodos().asLiveData()

    @Suppress("RedundantNullableReturnType")
    suspend fun getHighestPosition(): Int? = repo.getHighestPosition()

    val deletedTodo: MutableLiveData<Todo> by lazy { MutableLiveData<Todo>() }
}
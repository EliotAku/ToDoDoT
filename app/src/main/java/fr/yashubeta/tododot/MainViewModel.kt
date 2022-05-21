package fr.yashubeta.tododot

import androidx.lifecycle.*
import fr.yashubeta.tododot.database.Todo
import fr.yashubeta.tododot.database.TodoRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel(
    private val repo: TodoRepository,
    userPrefRepo: UserPreferencesRepository
) : ViewModel() {

    //private val repo = TodoRepository(AppDatabase.getDatabase(application).todoDao())

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
    fun getTodoChildren(parentId: Int): LiveData<List<Todo>> = repo.getTodoChildren(parentId).asLiveData()
    //fun allTodosByIsChecked(): LiveData<List<Todo>> = repo.allTodosByIsChecked().asLiveData()
    //fun uncheckedTodos(): LiveData<List<Todo>> = repo.uncheckedTodos().asLiveData()
    //fun checkedTodos(): LiveData<List<Todo>> = repo.checkedTodos().asLiveData()

    @Suppress("RedundantNullableReturnType")
    suspend fun getHighestPosition(): Int? = repo.getHighestPosition()

    val deletedTodo: MutableLiveData<Todo> by lazy { MutableLiveData<Todo>() }

    private val todosUiModelFlow = combine(
        repo.allTodosByIsChecked(),
        userPrefRepo.userPreferencesFlow
    ) { todos: List<Todo>, userPreferences: UserPreferences ->
        return@combine TodoUiModel(
            todos = todos,
            showSubTasks = userPreferences.showSubTasks
        )
    }
    val todosUiModel = todosUiModelFlow.asLiveData()
}

class MainViewModelFactory(
    private val repository: TodoRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class TodoUiModel(
    val todos: List<Todo>,
    val showSubTasks: Boolean
)
package fr.yashubeta.tododot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import fr.yashubeta.tododot.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        @Suppress("UNUSED_VARIABLE")
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setSupportActionBar(binding.toolBar)

        // -- RecyclerView -- \\
        val adapter = MainAdapter(this, viewModel)
        binding.views.recyclerViewUncheckedTodos.apply {
            this.adapter = adapter
        }

        // -- LISTENERS & OBSERVERS -- \\
        binding.views.root.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) binding.floatingActionButton.shrink()
            else if (scrollY < oldScrollY) binding.floatingActionButton.extend()
        }

        binding.floatingActionButton.setOnClickListener {
            binding.floatingActionButton.hide()
            AddTodoDialogFragment.newInstance(30).show(supportFragmentManager, "dialog")
        }

        viewModel.allTodosByIsChecked().observe(this) { allTodos ->
            if (allTodos.isNullOrEmpty()) return@observe
            val checkedTodosIndex = allTodos.indexOfFirst { it.isChecked }
            CoroutineScope(Dispatchers.IO).launch {
                if (checkedTodosIndex < 0) {
                    adapter.submitLists(allTodos, null)
                } else {
                    val uncheckedList = allTodos
                        .subList(0, checkedTodosIndex)
                        .sortedBy { it.position }
                    val checkedList = allTodos
                        .subList(checkedTodosIndex, allTodos.size)
                        .sortedBy { it.position }
                    adapter.submitLists(uncheckedList, checkedList)
                }
            }
        }

        viewModel.deletedTodo.observe(this) { deletedTodo ->
            Snackbar.make(binding.floatingActionButton, "Task deleted!", Snackbar.LENGTH_LONG)
                .setAction(R.string.all_undo) { viewModel.insertTodo(deletedTodo) }
                .show()
        }
    }
}
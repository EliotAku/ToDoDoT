package fr.yashubeta.tododot

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import fr.yashubeta.tododot.database.Todo
import fr.yashubeta.tododot.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private lateinit var binding: ActivityMainBinding

    var checkedTodos: List<Todo> = emptyList()

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
            //edgeEffectFactory = BounceEdgeEffectFactory()
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

        viewModel.deletedTodo.observe(this) { deletedTodo ->
            Snackbar.make(binding.floatingActionButton, "Task deleted!", Snackbar.LENGTH_LONG)
                .setAction(R.string.all_undo) { viewModel.insertTodo(deletedTodo) }
                .show()
        }

        viewModel.uncheckedTodos().observe(this) { todos ->
            adapter.submitUncheckedList(todos)
        }

        viewModel.checkedTodos().observe(this) { todos ->
            adapter.submitCheckedList(todos)
        }

    }

    private fun switchVisibilityWithTransition(view: View) {
        TransitionManager.beginDelayedTransition(binding.views.root, ChangeBounds())
        when(view.visibility) {
            View.INVISIBLE -> view.visibility = View.VISIBLE
            View.VISIBLE -> view.visibility = View.GONE
            View.GONE -> view.visibility = View.VISIBLE
        }
    }

    private fun showDeleteCheckedTodosDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.dialog_delete_checked_todos_title))
            .setNegativeButton(resources.getString(android.R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                viewModel.deleteTodos(checkedTodos)
            }
            .show()
    }

}
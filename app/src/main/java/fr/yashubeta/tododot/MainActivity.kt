package fr.yashubeta.tododot

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.doOnLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import fr.yashubeta.tododot.databinding.ActivityMainBinding


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
            this.attachToFab(binding.floatingActionButton)
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
            adapter.submitTodoList(allTodos)
        }

        viewModel.deletedTodo.observe(this) { deletedTodo ->
            Snackbar.make(binding.floatingActionButton, "Task deleted!", Snackbar.LENGTH_LONG)
                .setAction(R.string.all_undo) { viewModel.insertTodo(deletedTodo) }
                .show()
        }
    }

    private fun RecyclerView.attachToFab(fab: View) {
        clipToPadding = false
        fab.doOnLayout {
            val fabSizeWithMargin = fab.height + fab.marginBottom + fab.marginTop
            updatePadding(bottom = fabSizeWithMargin)
        }
    }
}
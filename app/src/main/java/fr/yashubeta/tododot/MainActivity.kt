package fr.yashubeta.tododot

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.doOnLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import fr.yashubeta.tododot.database.Todo
import fr.yashubeta.tododot.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private lateinit var binding: ActivityMainBinding

    private var actionMode: ActionMode? = null

    private val selectionList: MutableList<Todo> = mutableListOf()

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

        adapter.tracker = SelectionTracker.Builder(
            "my-selection-id",
            binding.views.recyclerViewUncheckedTodos,
            MyItemKeyProvider(binding.views.recyclerViewUncheckedTodos),
            MyItemDetailsLookup(binding.views.recyclerViewUncheckedTodos),
            StorageStrategy.createLongStorage())
            .withBandPredicate(MyBandPredicate())
            //.withOnDragInitiatedListener { false }
            .withSelectionPredicate(MySelectionPredicate(binding.views.recyclerViewUncheckedTodos))
            .build()

        val actionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                val inflater: MenuInflater = mode.menuInflater
                inflater.inflate(R.menu.selection_menu, menu)
                return true

            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.action_delete -> {
                        selectionList.let { viewModel.deleteTodos(it) }
                        mode?.finish() // Action picked, so close the CAB
                        true
                    }
                    else -> false
                }

            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                adapter.tracker?.clearSelection()
                actionMode = null
            }

        }

        // -- LISTENERS & OBSERVERS -- \\
        adapter.tracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onItemStateChanged(key: Long, selected: Boolean) {
                super.onItemStateChanged(key, selected)
                binding.views.root.findViewHolderForItemId(key)?.itemView?.isActivated = selected
                val newList = adapter.currentList.mapNotNull { (it as? DataItem.TodoItem)?.todo }
                val item = newList.find { it.todoId == key.toInt() }
                if (selected) {
                    item?.let { selectionList.add(it) }
                } else {
                    selectionList.remove(item)
                }

            }

            override fun onSelectionChanged() {
                super.onSelectionChanged()
                val selection = adapter.tracker?.selection
                if (selection?.isEmpty == true) {
                    actionMode?.finish()
                    return
                }
                if (actionMode == null) actionMode = startSupportActionMode(actionModeCallback)
                actionMode?.title = "${selection?.size()} selected"
            }
        })

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

private class MySelectionPredicate(private val recyclerView: RecyclerView
): SelectionTracker.SelectionPredicate<Long>() {
    override fun canSelectMultiple(): Boolean = true

    override fun canSetStateForKey(key: Long, nextState: Boolean): Boolean {
        val holder = recyclerView.findViewHolderForItemId(key)
        return holder is MainAdapter.TodoViewHolder
    }

    override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean {
        if (position < 0) return false
        val holder = recyclerView.findViewHolderForAdapterPosition(position)
        return holder is MainAdapter.TodoViewHolder
    }
}

private class MyBandPredicate(): BandPredicate() {
    override fun canInitiate(e: MotionEvent): Boolean {
        return false
    }

}

private class MyItemKeyProvider(private val recyclerView: RecyclerView
) : ItemKeyProvider<Long>(SCOPE_CACHED) {
    override fun getKey(position: Int): Long? = recyclerView.adapter?.getItemId(position)
    override fun getPosition(key: Long): Int {
        val viewHolder = recyclerView.findViewHolderForItemId(key)
        return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
    }
}
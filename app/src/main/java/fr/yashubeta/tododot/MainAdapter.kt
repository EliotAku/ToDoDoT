package fr.yashubeta.tododot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.yashubeta.tododot.database.Todo
import fr.yashubeta.tododot.databinding.ItemSectionBinding
import fr.yashubeta.tododot.databinding.ItemTodoBinding
import fr.yashubeta.tododot.databinding.RecyclerViewCheckedBinding
import java.util.*

private const val ITEM_VIEW_TYPE_SECTION = 0
private const val ITEM_VIEW_TYPE_UNCHECKED = 1
private const val ITEM_VIEW_TYPE_CHECKED = 2

class MainAdapter(
    private val activity: MainActivity,
    private val viewModel: MainViewModel
): ListAdapter<DataItem, RecyclerView.ViewHolder>(ItemDiffCallBack) {

    private var uncheckedList = emptyList<DataItem.TodoItem>()
    private var checkedList = emptyList<DataItem.TodoItem>()

    private val sectionItem = DataItem.Section

    class TodoViewHolder(
        val binding: ItemTodoBinding,
        var isChecked: Boolean = false
    ) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): TodoViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemTodoBinding.inflate(layoutInflater, parent, false)
                return TodoViewHolder(binding)
            }
        }

        fun bind(todo: Todo, clickListener: View.OnClickListener) {
            binding.root.setOnClickListener(clickListener)
            binding.textViewItemTodo.text = todo.title
            binding.textViewNote.apply {
                if (todo.note.isNullOrEmpty()) {
                    this.visibility = View.GONE
                } else {
                    this.visibility = View.VISIBLE
                    this.text = todo.note
                }
            }
            binding.checkBox.isChecked = todo.isChecked
        }
    }
    class SectionViewHolder(
        val binding: ItemSectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): SectionViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSectionBinding.inflate(layoutInflater, parent, false)
                return SectionViewHolder(binding)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        itemTouchHelper.attachToRecyclerView(recyclerView)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun getItemViewType(position: Int): Int {
        return when(val item = getItem(position)) {
            is DataItem.Section -> ITEM_VIEW_TYPE_SECTION
            is DataItem.TodoItem -> {
                if (item.isChecked) ITEM_VIEW_TYPE_CHECKED
                else ITEM_VIEW_TYPE_UNCHECKED
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_SECTION -> {
                SectionViewHolder.from(parent)
            }
            ITEM_VIEW_TYPE_UNCHECKED -> {
                TodoViewHolder.from(parent)
            }
            ITEM_VIEW_TYPE_CHECKED -> {
                TodoViewHolder.from(parent).apply { isChecked = true }
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, holderPosition: Int) {
        val item = getItem(holderPosition)
        when(holder) {
            is SectionViewHolder -> {
                holder.binding.root.text = activity.resources
                    .getText(R.string.header_title_checked_todos)
            }
            is TodoViewHolder -> {
                if (item !is DataItem.TodoItem) return

                val clickListener = View.OnClickListener {
                    // TODO: À déplacer dans MainActivity
                    TodoDialogFragment(viewModel, item.todo)
                        .show(activity.supportFragmentManager, "dialog")
                }

                when(getItemViewType(holderPosition)) {
                    ITEM_VIEW_TYPE_UNCHECKED ->  holder.bind(item.todo, clickListener)
                    ITEM_VIEW_TYPE_CHECKED -> {
                        holder.bind(item.todo, clickListener)
                        viewModel.updateTodo(item.todo.apply {
                            position = holderPosition - uncheckedList.size - 1
                        })
                    }
                }

                holder.binding.checkBox.setOnClickListener {
                    viewModel.updateTodo(item.todo.apply { isChecked = !isChecked })
                }

            }
            /*is CheckedViewHolder -> {
                holder.adapter.submitList(checkedList)
                holder.binding.textViewCheckedNumber.text = checkedList.size.toString()
                if (checkedList.isEmpty()) holder.binding.cardViewChecked.visibility = View.GONE
                else holder.binding.cardViewChecked.visibility = View.VISIBLE
            }*/
        }
    }

    fun submitLists(unchecked: List<Todo>, checked: List<Todo>) {
        uncheckedList = unchecked.map { DataItem.TodoItem(it) }
        checkedList = checked.map { DataItem.TodoItem(it, true) }
        val list: List<DataItem> = uncheckedList + sectionItem + checkedList
        submitList(list)
    }

    private fun switchVisibilityWithTransition(binding: RecyclerViewCheckedBinding,view: View) {
        TransitionManager.beginDelayedTransition(binding.root, ChangeBounds())
        when(view.visibility) {
            View.INVISIBLE -> view.visibility = View.VISIBLE
            View.VISIBLE -> view.visibility = View.GONE
            View.GONE -> view.visibility = View.VISIBLE
        }
    }

    private fun showDeleteCheckedTodosDialog() {
        MaterialAlertDialogBuilder(activity)
            .setTitle(activity.resources.getString(R.string.dialog_delete_checked_todos_title))
            .setNegativeButton(activity.resources.getString(android.R.string.cancel)) { _, _ -> }
            .setPositiveButton(activity.resources.getString(android.R.string.ok)) { _, _ ->
                viewModel.deleteTodos(uncheckedList.map { it.todo })
            }
            .show()
    }

    object ItemDiffCallBack : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }
    }

    private val itemTouchHelper by lazy {
        val itemTouchCallback = object: ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                if (target is TodoViewHolder && target.isChecked || target is SectionViewHolder)
                    return false
                
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(uncheckedList, i, i + 1)
                        val position1 = uncheckedList[i].todo.position
                        val position2 = uncheckedList[i+1].todo.position
                        uncheckedList[i].todo.position = position2
                        uncheckedList[i+1].todo.position = position1
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(uncheckedList, i, i - 1)
                        val position1 = uncheckedList[i].todo.position
                        val position2 = uncheckedList[i-1].todo.position
                        uncheckedList[i].todo.position = position2
                        uncheckedList[i-1].todo.position = position1
                    }
                }
                submitLists(uncheckedList.map { it.todo }, checkedList.map { it.todo })
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {  }

            override fun clearView(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, holder)
                viewModel.updateTodos(uncheckedList.map { it.todo } + checkedList.map { it.todo })
            }
        }
        ItemTouchHelper(itemTouchCallback)
    }
}

sealed class DataItem {
    abstract val id: Int

    object Section: DataItem() {
        override val id: Int = Int.MIN_VALUE
    }

    data class TodoItem(val todo: Todo, val isChecked: Boolean = false): DataItem() {
        override val id = todo.todoId
    }
}
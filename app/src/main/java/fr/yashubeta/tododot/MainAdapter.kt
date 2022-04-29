package fr.yashubeta.tododot

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.yashubeta.tododot.database.Todo
import fr.yashubeta.tododot.databinding.ItemSectionBinding
import fr.yashubeta.tododot.databinding.ItemTodoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

private const val ITEM_VIEW_TYPE_SECTION = 0
private const val ITEM_VIEW_TYPE_UNCHECKED = 1
private const val ITEM_VIEW_TYPE_CHECKED = 2

class MainAdapter(
    private val activity: MainActivity,
    private val viewModel: MainViewModel
): ListAdapter<DataItem, RecyclerView.ViewHolder>(ItemDiffCallBack) {

    var tracker: SelectionTracker<Long>? = null
    private var adapterRecyclerView: RecyclerView? = null

    private var uncheckedList = emptyList<DataItem.TodoItem>()
    private var checkedList = emptyList<DataItem.TodoItem>()
    private val sectionItem = DataItem.Section

    private var isCheckedTodosVisible: Boolean = false

    init {
        setHasStableIds(true)
    }

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

        fun getItemDetails() = object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = bindingAdapterPosition
            override fun getSelectionKey(): Long = itemId
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
        //recyclerView.edgeEffectFactory = BounceEdgeEffectFactory()
        adapterRecyclerView = recyclerView
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun getItemId(position: Int): Long { return currentList[position].id }

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
                SectionViewHolder.from(parent).apply {
                    itemView.setOnClickListener {
                        isCheckedTodosVisible = !isCheckedTodosVisible
                        binding.arrowHeader.isChecked = isCheckedTodosVisible
                        submitList(sectionedList)
                    }
                    binding.buttonDeleteCheckedTodos.setOnClickListener {
                        showDeleteCheckedTodosDialog()
                    }
                }
            }
            ITEM_VIEW_TYPE_UNCHECKED -> {
                TodoViewHolder.from(parent)
            }
            ITEM_VIEW_TYPE_CHECKED -> {
                TodoViewHolder.from(parent)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, holderPosition: Int) {
        val item = getItem(holderPosition)
        //holder.itemView.isActivated = tracker?.isSelected(item.id) ?: false
        when(holder) {
            is SectionViewHolder -> {

            }
            is TodoViewHolder -> {
                if (item !is DataItem.TodoItem) return
                holder.isChecked = getItemViewType(holderPosition) == ITEM_VIEW_TYPE_CHECKED

                val clickListener = View.OnClickListener {
                    // TODO: À déplacer dans MainActivity
                    TodoDialogFragment(viewModel, item.todo)
                        .show(activity.supportFragmentManager, "dialog")
                }

                when(getItemViewType(holderPosition)) {
                    ITEM_VIEW_TYPE_UNCHECKED -> {
                        holder.bind(item.todo, clickListener)
                        tracker?.let {

                        }
                        if (item.todo.position != holderPosition)
                            viewModel.updateTodo(item.todo.apply {
                                position = holderPosition
                            })

                    }
                    ITEM_VIEW_TYPE_CHECKED -> {
                        holder.bind(item.todo, clickListener)
                        if (item.todo.position != holderPosition - 1)
                            viewModel.updateTodo(item.todo.apply {
                                position = holderPosition - 1
                            })
                    }
                }

                holder.binding.checkBox.setOnClickListener {
                    viewModel.updateTodo(item.todo.apply {
                        position = if (isChecked) 9999 else -1
                        isChecked = !isChecked
                    })
                }
            }
        }
    }

    fun submitTodoList(allTodos: List<Todo>) {
        val checkedTodosIndex = allTodos.indexOfFirst { it.isChecked }
        CoroutineScope(Dispatchers.IO).launch {
            if (checkedTodosIndex < 0) {
                val unchecked: List<DataItem.TodoItem> = allTodos
                    .sortedBy { it.position }
                    .map { DataItem.TodoItem(it) }
                uncheckedList = unchecked

                withContext(Dispatchers.Main) { this@MainAdapter.submitList(uncheckedList) }
            } else {
                val unchecked = allTodos
                    .subList(0, checkedTodosIndex)
                    .sortedBy { it.position }
                    .map { DataItem.TodoItem(it) }
                uncheckedList = unchecked

                val checked = allTodos
                    .subList(checkedTodosIndex, allTodos.size)
                    .sortedBy { it.position }
                    .map { DataItem.TodoItem(it, isChecked = true) }
                checkedList = checked

                withContext(Dispatchers.Main) { this@MainAdapter.submitList(sectionedList) }
            }
            withContext(Dispatchers.Main) { setSectionCheckedTodoNumber() }
        }
    }

    private val sectionedList: List<DataItem> get() {
        return if (isCheckedTodosVisible) {
            uncheckedList + sectionItem + checkedList
        } else {
            uncheckedList + sectionItem
        }
    }

    private fun showDeleteCheckedTodosDialog() {
        MaterialAlertDialogBuilder(activity)
            .setTitle(activity.resources.getString(R.string.dialog_delete_checked_todos_title))
            .setNegativeButton(activity.resources.getString(android.R.string.cancel)) { _, _ -> }
            .setPositiveButton(activity.resources.getString(android.R.string.ok)) { _, _ ->
                viewModel.deleteTodos(checkedList.map { it.todo })
            }
            .show()
    }

    fun List<DataItem>.mapToTodo() : List<Todo> {
        return this.mapNotNull { if (it is DataItem.TodoItem) it.todo else null }
    }

    private fun setSectionCheckedTodoNumber() {
        val section = adapterRecyclerView?.findViewHolderForAdapterPosition(
            currentList.indexOf(sectionItem)) as? SectionViewHolder
        section?.binding?.textViewCheckedNumber?.text = checkedList.size.toString()
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
            UP or DOWN, 0) {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val sectionIndex = currentList.indexOfFirst { it is DataItem.Section }
                val isCheckedItem = viewHolder
                    .bindingAdapterPosition < sectionIndex || sectionIndex < 0
                return makeMovementFlags(
                    if (viewHolder is TodoViewHolder && isCheckedItem) UP or DOWN else 0,
                    0
                )
            }

            override fun canDropOver(
                recyclerView: RecyclerView,
                current: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val sectionIndex = currentList.indexOfFirst { it is DataItem.Section }
                return target.bindingAdapterPosition < sectionIndex || sectionIndex < 0
            }

            override fun onMove(
                recyclerView: RecyclerView,
                current: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val dataset: MutableList<DataItem> = currentList.toMutableList()
                val fromPosition = current.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                Collections.swap(dataset, fromPosition, toPosition)

                val item = dataset[fromPosition] as? DataItem.TodoItem
                item?.todo?.position = fromPosition

                val nextItem = dataset[toPosition] as? DataItem.TodoItem
                nextItem?.todo?.position = toPosition

                tracker?.clearSelection()

                submitList(dataset)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            @SuppressLint("RestrictedApi")
            override fun clearView(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, holder)
                // TODO: Find another way to disable the range selection
                //tracker?.endRange()
                val newList = currentList.mapNotNull { //(it as? DataItem.TodoItem)?.todo
                    if (it is DataItem.TodoItem) it.todo else null
                }
                viewModel.updateTodos(newList)
            }
        }
        ItemTouchHelper(itemTouchCallback)
    }
}

sealed class DataItem {
    abstract val id: Long

    object Section: DataItem() {
        override val id = Long.MIN_VALUE
    }

    data class TodoItem(val todo: Todo, val isChecked: Boolean = false): DataItem() {
        override val id = todo.todoId.toLong()
    }
}
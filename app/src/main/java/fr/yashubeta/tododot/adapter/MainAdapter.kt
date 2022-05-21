package fr.yashubeta.tododot.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.yashubeta.tododot.MainActivity
import fr.yashubeta.tododot.MainViewModel
import fr.yashubeta.tododot.R
import fr.yashubeta.tododot.database.Todo
import fr.yashubeta.tododot.fragment.TodoDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.roundToInt

private const val ITEM_VIEW_TYPE_SECTION = 0
private const val ITEM_VIEW_TYPE_TODO = 1

class MainAdapter(
    private val activity: MainActivity,
    private val viewModel: MainViewModel
) : ListAdapter<DataItem, RecyclerView.ViewHolder>(ItemDiffCallBack) {

    var tracker: SelectionTracker<Long>? = null
    private var adapterRecyclerView: RecyclerView? = null

    private var uncheckedList = emptyList<DataItem.TodoItem>()
    private var checkedList = emptyList<DataItem.TodoItem>()
    private val sectionItem = DataItem.Section

    private var isCheckedTodosVisible: Boolean = false

    init {
        setHasStableIds(true)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        itemTouchHelper.attachToRecyclerView(recyclerView)
        //recyclerView.edgeEffectFactory = BounceEdgeEffectFactory()
        adapterRecyclerView = recyclerView
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].id
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Section -> ITEM_VIEW_TYPE_SECTION
            is DataItem.TodoItem -> ITEM_VIEW_TYPE_TODO
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
            ITEM_VIEW_TYPE_TODO -> {
                TodoViewHolder.from(parent)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, holderPosition: Int) {
        val item = getItem(holderPosition)
        holder.itemView.isActivated = tracker?.isSelected(item.id) ?: false
        when (holder) {
            is SectionViewHolder -> {

            }
            is TodoViewHolder -> {
                if (item !is DataItem.TodoItem) return
                //holder.isChecked = getItemViewType(holderPosition) == ITEM_VIEW_TYPE_CHECKED
                val clickListener = View.OnClickListener {
                    // TODO: À déplacer dans MainActivity
                    TodoDialogFragment(viewModel, item.todo)
                        .show(activity.supportFragmentManager, "BottomSheet")
                }

                /*if (item.to/do.parentId != null) {
                    item.to/do.parentId?.let {
                        val parent = currentList.mapToTodo().firstOrNull {
                            it.todoId == item.to/do.parentId
                        }
                        if (parent != null) {
                            if (parent.position > item.to/do.position) item.to/do.parentId = null
                        } else item.to/do.parentId = null
                        updateTodoInDb(item.to/do)
                    }
                }*/

                val cardView = holder.itemView.findViewById<MaterialCardView>(R.id.card_view_item)
                cardView.strokeWidth = if (holder.itemView.isActivated) 6 else 0

                holder.itemView.updatePadding(
                    left = if (item.todo.parentId != null) intToDp(32) else intToDp(16)
                )

                holder.binding.checkBox.setOnClickListener {
                    viewModel.updateTodo(item.todo.apply {
                        position = if (item.isChecked) 9999 else -1
                        isChecked = !isChecked
                    })
                }

                holder.bind(item.todo, clickListener)
            }
        }
    }

    private fun intToDp(dp: Int): Int {
        val density: Float = activity.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    fun submitTodoList(allTodos: List<Todo>, showSub: Boolean = true) {
        val checkedTodosIndex = allTodos.indexOfFirst { it.isChecked }
        CoroutineScope(Dispatchers.IO).launch {
            if (checkedTodosIndex < 0) {
                var needToUpdate = false
                val rawUnchecked = allTodos.sortedBy { it.position }

                val unchecked = rawUnchecked.mapNotNull { todo ->
                    if (showSub || todo.parentId == null) {
                        val index = rawUnchecked.indexOf(todo)
                        if (todo.position == index) {
                            DataItem.TodoItem(todo)
                        } else {
                            todo.position = index
                            needToUpdate = true
                            DataItem.TodoItem(todo)
                        }
                    } else null
                }
                uncheckedList = unchecked
                checkedList = emptyList()

                if (needToUpdate) viewModel.updateTodos(sectionedList.mapToTodo())
                withContext(Dispatchers.Main) { this@MainAdapter.submitList(uncheckedList) }
            } else {
                var needToUpdate = false
                val rawUnchecked = allTodos
                    .subList(0, checkedTodosIndex)
                    .sortedBy { it.position }
                val rawChecked = allTodos
                    .subList(checkedTodosIndex, allTodos.size)
                    .sortedBy { it.position }
                val resolvedList = rawUnchecked + rawChecked

                val unchecked = rawUnchecked.map { todo ->
                    val index = resolvedList.indexOf(todo)
                    if (todo.position == index) {
                        DataItem.TodoItem(todo)
                    } else {
                        todo.position = index
                        needToUpdate = true
                        DataItem.TodoItem(todo)
                    }
                }
                uncheckedList = unchecked

                val checked = rawChecked.map { todo ->
                    val index = resolvedList.indexOf(todo)
                    if (todo.position == index) {
                        DataItem.TodoItem(todo, isChecked = true)
                    } else {
                        todo.position = index
                        needToUpdate = true
                        DataItem.TodoItem(todo, isChecked = true)
                    }
                }
                checkedList = checked

                if (needToUpdate) viewModel.updateTodos((rawUnchecked + rawChecked))
                withContext(Dispatchers.Main) { this@MainAdapter.submitList(sectionedList) }
            }
            withContext(Dispatchers.Main) { setSectionCheckedTodoNumber() }
        }
    }

    private val sectionedList: List<DataItem>
        get() {
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
                viewModel.deleteTodos(checkedList.mapToTodo())
            }
            .show()
    }

    fun List<DataItem>.mapToTodo(): List<Todo> {
        return this.mapNotNull { if (it is DataItem.TodoItem) it.todo else null }
    }

    private fun setSectionCheckedTodoNumber() {
        val section = adapterRecyclerView?.findViewHolderForAdapterPosition(
            currentList.indexOf(sectionItem)
        ) as? SectionViewHolder
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
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            UP or DOWN, LEFT or RIGHT
        ) {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val sectionIndex = currentList.indexOfFirst { it is DataItem.Section }
                val isCheckedItem = viewHolder
                    .bindingAdapterPosition < sectionIndex || sectionIndex < 0
                return makeMovementFlags(
                    if (viewHolder is TodoViewHolder && isCheckedItem) UP or DOWN else 0,
                    if (viewHolder is TodoViewHolder && isCheckedItem) LEFT or RIGHT else 0
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
                if (direction == RIGHT) {
                    val item = currentList[viewHolder.bindingAdapterPosition]
                    if (item !is DataItem.TodoItem) return
                    val aboveTodo: Todo? = (currentList.getOrNull(
                        currentList.indexOf(item) - 1
                    ) as? DataItem.TodoItem)?.todo
                    val aboveId = aboveTodo?.parentId ?: aboveTodo?.todoId
                    val newParentId: Int? =
                        if (item.todo.parentId == null) aboveId ?: 0 else null
                    viewModel.updateTodo(item.todo.apply {parentId = newParentId })
                }
                notifyItemChanged(viewHolder.bindingAdapterPosition)
            }

            @SuppressLint("RestrictedApi")
            override fun clearView(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, holder)
                // TODO: Find another way to disable the range selection
                tracker?.endRange()
                viewModel.updateTodos(currentList.mapToTodo())
            }
        }
        ItemTouchHelper(itemTouchCallback)
    }
}

sealed class DataItem {
    abstract val id: Long

    object Section : DataItem() {
        override val id = Long.MIN_VALUE
    }

    data class TodoItem(val todo: Todo, val isChecked: Boolean = false) : DataItem() {
        override val id = todo.todoId.toLong()
    }
}
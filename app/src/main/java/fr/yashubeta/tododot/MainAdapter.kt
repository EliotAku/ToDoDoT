package fr.yashubeta.tododot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.yashubeta.tododot.database.Todo
import fr.yashubeta.tododot.databinding.RecyclerViewCheckedBinding
import fr.yashubeta.tododot.databinding.RecyclerViewUncheckedBinding

private val ITEM_VIEW_TYPE_UNCHECKED = 0
private val ITEM_VIEW_TYPE_CHECKED = 1

class MainAdapter(
    private val activity: MainActivity,
    private val viewModel: MainViewModel,
    private val isCheckedAdapter: Boolean = false
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var uncheckedList = emptyList<Todo>()
    private var checkedList = emptyList<Todo>()

    private val holderList = listOf<DataItem>(
        DataItem.UncheckedLayout(uncheckedList),
        DataItem.CheckedLayout(checkedList)
    )

    class UncheckedViewHolder(val binding: RecyclerViewUncheckedBinding) : RecyclerView.ViewHolder(binding.root)
    class CheckedViewHolder(val binding: RecyclerViewCheckedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = holderList.size

    override fun getItemViewType(position: Int): Int {
        return when(holderList[position]) {
            is DataItem.UncheckedLayout -> ITEM_VIEW_TYPE_UNCHECKED
            is DataItem.CheckedLayout -> ITEM_VIEW_TYPE_CHECKED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_UNCHECKED -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecyclerViewUncheckedBinding.inflate(layoutInflater, parent, false)
                UncheckedViewHolder(binding)
            }
            ITEM_VIEW_TYPE_CHECKED -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecyclerViewCheckedBinding.inflate(layoutInflater, parent, false)
                CheckedViewHolder(binding)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, holderPosition: Int) {

        when(holder) {
            is UncheckedViewHolder -> {
                val adapter = TodoAdapter(activity, viewModel, false)
                holder.binding.recyclerViewUncheckedTodos.adapter = adapter
                adapter.submitList(uncheckedList)
            }
            is CheckedViewHolder -> {
                val adapter = TodoAdapter(activity, viewModel, true)
                holder.binding.recyclerViewCheckedTodos.adapter = adapter
                adapter.submitList(checkedList)
            }
        }
    }

    fun submitUncheckedList(todos: List<Todo>) {
        uncheckedList = todos
        notifyDataSetChanged()
    }

    fun submitCheckedList(todos: List<Todo>) {
        checkedList = todos
        notifyDataSetChanged()
    }
}

sealed class DataItem {
    abstract val id: Int

    data class UncheckedLayout(val todos: List<Todo>): DataItem() {
        override val id = ITEM_VIEW_TYPE_UNCHECKED
    }

    data class CheckedLayout(val todos: List<Todo>): DataItem() {
        override val id = ITEM_VIEW_TYPE_CHECKED
    }
}
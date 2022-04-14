package fr.yashubeta.tododot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.yashubeta.tododot.database.Todo
import fr.yashubeta.tododot.databinding.RecyclerViewCheckedBinding
import fr.yashubeta.tododot.databinding.RecyclerViewUncheckedBinding

private const val ITEM_VIEW_TYPE_UNCHECKED = 0
private const val ITEM_VIEW_TYPE_CHECKED = 1

class MainAdapter(
    private val activity: MainActivity,
    private val viewModel: MainViewModel
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var uncheckedList = emptyList<Todo>()
    private var checkedList = emptyList<Todo>()

    private val holderList = listOf(
        DataItem.UncheckedLayout(uncheckedList),
        DataItem.CheckedLayout(checkedList)
    )

    class UncheckedViewHolder(binding: RecyclerViewUncheckedBinding, val adapter: TodoAdapter
    ) : RecyclerView.ViewHolder(binding.root)
    class CheckedViewHolder(val binding: RecyclerViewCheckedBinding, val adapter: TodoAdapter
    ) : RecyclerView.ViewHolder(binding.root)

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
                val adapter = TodoAdapter(activity, viewModel, false)
                binding.recyclerViewUncheckedTodos.adapter = adapter
                UncheckedViewHolder(binding, adapter)
            }
            ITEM_VIEW_TYPE_CHECKED -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecyclerViewCheckedBinding.inflate(layoutInflater, parent, false)
                val adapter = TodoAdapter(activity, viewModel, true)
                binding.recyclerViewCheckedTodos.adapter = adapter
                binding.recyclerViewCheckedTodos.visibility = View.GONE
                binding.layoutHeaderCheckedTodos.setOnClickListener {
                    switchVisibilityWithTransition(binding, binding.recyclerViewCheckedTodos)
                    binding.arrowHeader.isChecked = !binding.arrowHeader.isChecked
                }
                binding.buttonDeleteCheckedTodos.setOnClickListener { showDeleteCheckedTodosDialog() }
                CheckedViewHolder(binding, adapter)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, holderPosition: Int) {
        when(holder) {
            is UncheckedViewHolder -> {
                holder.adapter.submitList(uncheckedList)
            }
            is CheckedViewHolder -> {
                holder.adapter.submitList(checkedList)
                holder.binding.textViewCheckedNumber.text = checkedList.size.toString()
                if (checkedList.isEmpty()) holder.binding.cardViewChecked.visibility = View.GONE
                else holder.binding.cardViewChecked.visibility = View.VISIBLE
            }
        }
    }

    fun submitUncheckedList(todos: List<Todo>) {
        uncheckedList = todos
        notifyItemChanged(0)
    }

    fun submitCheckedList(todos: List<Todo>) {
        checkedList = todos
        notifyItemChanged(1)
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
                viewModel.deleteTodos(checkedList)
            }
            .show()
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
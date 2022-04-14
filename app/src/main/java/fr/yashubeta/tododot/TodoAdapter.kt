package fr.yashubeta.tododot

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.yashubeta.tododot.database.Todo
import fr.yashubeta.tododot.databinding.ItemTodoBinding
import java.util.*


class TodoAdapter(
    private val activity: MainActivity,
    private val viewModel: MainViewModel,
    private val isCheckedAdapter: Boolean = false
): ListAdapter<Todo, TodoAdapter.ItemViewHolder>(TodoDiffCallBack) {

    class ItemViewHolder(val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (!isCheckedAdapter) itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemTodoBinding.inflate(layoutInflater, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, holderPosition: Int) {
        val item = getItem(holderPosition)
        // Update position if it's wrong
        // (TODO: A optimiser car fait une double update de la base de donnée)
        if (item.position != holderPosition)
            viewModel.updateTodo(item.also { it.position = holderPosition })

        with(holder.binding) {
            textViewItemTodo.text = item.title
            textViewNote.text = item.note

            // Hide note's TextView if it's null or empty
            if (item.note.isNullOrEmpty()) textViewNote.visibility = View.GONE
            else textViewNote.visibility = View.VISIBLE

            if (item.isChecked) {
                checkBox.isChecked = true
                textViewItemTodo.alpha = 0.5f
            } else {
                checkBox.isChecked = false
                textViewItemTodo.alpha = 1.0f
            }

            checkBox.setOnClickListener {
                viewModel.updateTodo(item.apply {
                    // For placing item at top or bottom
                    // (Real DB position is updated at top of onBindViewHolder())
                    position =
                        if(isCheckedAdapter) 9999 // Place the item at the bottom if it's unchecked
                        else 0 // And at the top if it's checked
                    isChecked = !isChecked
                })
            }
        }

        holder.itemView.setOnClickListener {
            TodoDialogFragment(viewModel, item).show(activity.supportFragmentManager, "dialog")
        }
    }

    object TodoDiffCallBack : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem.todoId == newItem.todoId
        }

        override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
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
                val dataset = currentList.toMutableList()
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(dataset, i, i + 1)
                        val position1 = dataset[i].position
                        val position2 = dataset[i+1].position
                        dataset[i].position = position2
                        dataset[i+1].position = position1
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(dataset, i, i - 1)
                        val position1 = dataset[i].position
                        val position2 = dataset[i-1].position
                        dataset[i].position = position2
                        dataset[i-1].position = position1
                    }
                }
                submitList(dataset)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {  }

            override fun clearView(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, holder)
                viewModel.updateTodos(currentList)
            }
        }
        ItemTouchHelper(itemTouchCallback)
    }

}
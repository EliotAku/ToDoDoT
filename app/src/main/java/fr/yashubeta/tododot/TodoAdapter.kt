package fr.yashubeta.tododot

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import fr.yashubeta.tododot.database.Todo
import fr.yashubeta.tododot.databinding.ItemTodoBinding
import java.util.*


class TodoAdapter(
    private val activity: MainActivity,
    private val viewModel: MainViewModel,
    private val isCheckedAdapter: Boolean = false
): RecyclerView.Adapter<TodoAdapter.ItemViewHolder>() {

    private var dataset = mutableListOf<Todo>()

    class ItemViewHolder(val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = dataset.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (!isCheckedAdapter) itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, holderPosition: Int) {
        val item = dataset[holderPosition]
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

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(todos: List<Todo>) {
        dataset = todos.toMutableList()
        notifyDataSetChanged()
    }

    private val itemTouchHelper by lazy {
        val itemTouchCallback = object: ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
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
                this@TodoAdapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {  }

            override fun clearView(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, holder)
                viewModel.updateTodos(dataset)
            }
        }
        ItemTouchHelper(itemTouchCallback)
    }

}
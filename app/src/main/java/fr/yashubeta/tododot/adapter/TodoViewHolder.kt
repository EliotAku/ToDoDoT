package fr.yashubeta.tododot.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import fr.yashubeta.tododot.database.Todo
import fr.yashubeta.tododot.databinding.ItemTodoBinding

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
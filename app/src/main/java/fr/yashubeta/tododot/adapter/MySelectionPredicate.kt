package fr.yashubeta.tododot.adapter

import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView

class MySelectionPredicate(private val recyclerView: RecyclerView
): SelectionTracker.SelectionPredicate<Long>() {
    override fun canSelectMultiple(): Boolean = true

    override fun canSetStateForKey(key: Long, nextState: Boolean): Boolean {
        val holder = recyclerView.findViewHolderForItemId(key)
        return holder is TodoViewHolder
    }

    override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean {
        if (position < 0) return false
        val holder = recyclerView.findViewHolderForAdapterPosition(position)
        return holder is TodoViewHolder
    }
}
package fr.yashubeta.tododot.adapter

import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView

class MyItemKeyProvider(private val recyclerView: RecyclerView
) : ItemKeyProvider<Long>(SCOPE_CACHED) {
    override fun getKey(position: Int): Long? = recyclerView.adapter?.getItemId(position)
    override fun getPosition(key: Long): Int {
        val viewHolder = recyclerView.findViewHolderForItemId(key)
        return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
    }
}
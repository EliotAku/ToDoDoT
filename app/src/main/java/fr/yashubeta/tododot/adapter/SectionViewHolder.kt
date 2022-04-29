package fr.yashubeta.tododot.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.yashubeta.tododot.databinding.ItemSectionBinding

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
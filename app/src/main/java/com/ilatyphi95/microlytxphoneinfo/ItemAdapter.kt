package com.ilatyphi95.microlytxphoneinfo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ilatyphi95.microlytxphoneinfo.databinding.ItemLayoutBinding

class ItemAdapter(
    val onClick: (position: Int, itemAtPosition: PhoneItem) -> Unit
) : ListAdapter<PhoneItem, ItemAdapter.ViewHolder>(diffCallback) {

    inner class ViewHolder(private val binding: ItemLayoutBinding, onItemClick: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { onItemClick(adapterPosition) }
        }

        fun bind(viewmodel: PhoneItem) {
            binding.viewmodel = viewmodel
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLayoutBinding.inflate(inflater, parent, false)

        return ViewHolder(binding = binding,
            onItemClick = { position ->
                val itemAtPosition = getItem(position)
                this.onClick(position, itemAtPosition)
            }
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

val diffCallback = object : DiffUtil.ItemCallback<PhoneItem>() {
    override fun areItemsTheSame(oldItem: PhoneItem, newItem: PhoneItem) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: PhoneItem, newItem: PhoneItem) =
        oldItem == newItem

}
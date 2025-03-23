package com.example.fastaf.ui.home.searchable

import androidx.recyclerview.widget.DiffUtil

class DrugsDiffCallback(
    private val oldList: List<ResponseSearchRecItem>,
    private val newList: List<ResponseSearchRecItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
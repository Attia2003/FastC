package com.example.fastaf.ui.home.searchable

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fastaf.databinding.ItemSearchBinding

class SearchRecAdapter(
    private var drugsList: MutableList<ResponseSearchRecItem> = mutableListOf(),
) : RecyclerView.Adapter<SearchRecAdapter.DrugViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrugViewHolder {
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DrugViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DrugViewHolder, position: Int) {
        holder.bind(drugsList[position])
    }

    override fun getItemCount(): Int = drugsList.size

    fun updateData(newList: List<ResponseSearchRecItem>) {
        drugsList.clear()
        drugsList.addAll(newList)
        Log.d("UI_UPDATE", "Updated RecyclerView with ${newList.size} items")
        notifyDataSetChanged()
    }

    inner class DrugViewHolder(private val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ResponseSearchRecItem) {
            binding.titleName.text = item.name
            binding.type.text = item.form

        }
    }
}
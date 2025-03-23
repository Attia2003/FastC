package com.example.fastaf.ui.home.searchable

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.fastaf.databinding.ItemSearchBinding

class SearchRecAdapter(
    private var drugsList: MutableList<ResponseSearchRecItem> = mutableListOf(),
    private val onDeleteClick: (ResponseSearchRecItem) -> Unit,
    private val onCameraClicked: (Int) -> Unit
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
        val diffCallback = DrugsDiffCallback(drugsList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        drugsList.clear()
        drugsList.addAll(newList)
        Log.d("UI_UPDATE", "Updated RecyclerView with ${newList.size} items")

        diffResult.dispatchUpdatesTo(this)
    }


    fun removeItem(item: ResponseSearchRecItem) {
        val newList = drugsList.toMutableList()
        newList.remove(item)
        updateData(newList)
    }

    inner class DrugViewHolder(private val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ResponseSearchRecItem) {
            binding.titleName.text = item.name
            binding.type.text = item.form

            binding.ICONCAM.setOnClickListener { onCameraClicked(item.id) }
            binding.ICOnDELETE.setOnClickListener {
                if (item.status == "AVAILABLE") {
                    onDeleteClick(item)
                } else {
                    Toast.makeText(binding.root.context, "Already ${item.status}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
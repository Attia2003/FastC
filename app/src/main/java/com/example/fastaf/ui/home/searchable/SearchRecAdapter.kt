package com.example.fastaf.ui.home.searchable

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fastaf.databinding.ItemSearchBinding

class SearchRecAdapter(var items :MutableList<ResponseSearchRecItem>?=null) :RecyclerView.Adapter<SearchRecAdapter.SearchRecViewHolder>() {

    class SearchRecViewHolder(val binding : ItemSearchBinding) : RecyclerView.ViewHolder(binding.root) {
                fun  bind(item : ResponseSearchRecItem){
                    binding.titleName.text=item.name
                    binding.type.text=item.description
                }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchRecViewHolder{
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SearchRecViewHolder(binding)
    }

    override fun getItemCount(): Int = items?.size ?: 0

    override fun onBindViewHolder(holder: SearchRecViewHolder, position: Int) {
        holder.bind(items!![position])
    }
    fun updateData(newList: List<ResponseSearchRecItem?>?) {
        items?.clear()
        items = newList as MutableList<ResponseSearchRecItem>
        notifyDataSetChanged()
    }

}
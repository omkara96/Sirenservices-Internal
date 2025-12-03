package com.omkara.sirenservices_internal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VehicleDriverSearchListAdapter(
    private var items: List<String>,
    private val onSelect: (String) -> Unit
) : RecyclerView.Adapter<VehicleDriverSearchListAdapter.ViewHolder>() {

    private var filteredList = items.toMutableList()

    fun filter(query: String) {
        filteredList = items.filter {
            it.contains(query, ignoreCase = true)
        }.toMutableList()
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text = itemView.findViewById<TextView>(android.R.id.text1)

        init {
            itemView.setOnClickListener {
                onSelect(filteredList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.text = filteredList[position]
    }

    override fun getItemCount(): Int = filteredList.size
}

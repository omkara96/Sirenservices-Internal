package com.omkara.sirenservices_internal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.ServiceRecord

class ServiceHistoryAdapter(
    private var items: List<ServiceRecord>
) : RecyclerView.Adapter<ServiceHistoryAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val type: TextView = itemView.findViewById(R.id.tvServiceType)
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val odo: TextView = itemView.findViewById(R.id.tvOdometer)
        val cost: TextView = itemView.findViewById(R.id.tvCost)
        val notes: TextView = itemView.findViewById(R.id.tvNotes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_record, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val it = items[position]

        h.type.text = it.serviceType
        h.date.text = "Date: ${it.serviceDate}"
        h.odo.text = "Odometer: ${it.odometer_km} km"
        h.cost.text = "Cost: ₹${it.serviceCost}"
        h.notes.text = if (it.notes.isNotEmpty()) "Notes: ${it.notes}" else ""
    }

    override fun getItemCount() = items.size

    fun updateList(newList: List<ServiceRecord>) {
        items = newList
        notifyDataSetChanged()
    }
}

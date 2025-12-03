package com.omkara.sirenservices_internal.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.ServiceRecord

class VehicleShowServiceRecordAdapter(
    private var list: List<ServiceRecord>
) : RecyclerView.Adapter<VehicleShowServiceRecordAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val card: MaterialCardView = v.findViewById(R.id.cardService)
        val txtType: TextView = v.findViewById(R.id.txtServiceType)
        val txtDate: TextView = v.findViewById(R.id.txtServiceDate)
        val txtOdo: TextView = v.findViewById(R.id.txtServiceOdometer)
        val txtCost: TextView = v.findViewById(R.id.txtServiceCost)
        val txtNotes: TextView = v.findViewById(R.id.txtServiceNotes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_record, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]

        // Highlight latest record (position 0)
        if (position == 0) {
            holder.card.setCardBackgroundColor(Color.parseColor("#E3F2FD"))  // Light Blue
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE)
        }

        holder.txtType.text = item.serviceType
        holder.txtDate.text = "Date: ${item.serviceDate}"
        holder.txtOdo.text = "Odometer: ${item.odometer_km} km"
        holder.txtCost.text = "Cost: ₹${item.serviceCost}"
        holder.txtNotes.text = "Notes: ${item.notes}"
    }

    override fun getItemCount(): Int = list.size

    fun update(newList: List<ServiceRecord>) {
        list = newList
        notifyDataSetChanged()
    }
}

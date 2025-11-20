package com.omkara.sirenservices_internal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omkara.sirenservices_internal.R

data class VehicleItem(
    val id: String,
    val vehicleNumber: String,
    val make: String,
    val model: String,
    val odometerKm: Double?,
    val status: String
)

class VehicleAdapter(
    private var items: List<VehicleItem>,
    private val onClick: (VehicleItem) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvVehicleNumber: TextView = itemView.findViewById(R.id.tvVehicleNumber)
        val tvMakeModel: TextView = itemView.findViewById(R.id.tvMakeModel)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvOdometer: TextView = itemView.findViewById(R.id.tvOdometer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vehicle, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.tvVehicleNumber.text = item.vehicleNumber
        holder.tvMakeModel.text = "${item.make} • ${item.model}"
        holder.tvStatus.text = item.status
        holder.tvOdometer.text = item.odometerKm?.let { "$it km" } ?: "-"

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<VehicleItem>) {
        items = newList
        notifyDataSetChanged()
    }
}

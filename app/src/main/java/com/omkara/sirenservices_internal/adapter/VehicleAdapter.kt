package com.omkara.sirenservices_internal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.omkara.sirenservices_internal.R


data class VehicleListItem(
    val id: String,
    val vehicleNumber: String,
    val make: String,
    val model: String,
    val odometerKm: Double?,
    val status: String,
    val lastServiceDate: String?,
    val photoUrl: String?,
    val revenue: Double = 0.0,
    val expenses: Double = 0.0
)


class VehicleAdapter(
    private var items: List<VehicleListItem>,
    private val onClick: (VehicleListItem) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgThumb: ImageView = itemView.findViewById(R.id.imgThumb)
        val tvVehicleNumber: TextView = itemView.findViewById(R.id.tvVehicleNumber)
        val tvMakeModel: TextView = itemView.findViewById(R.id.tvMakeModel)
        val tvLastService: TextView = itemView.findViewById(R.id.tvLastService)
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
        holder.tvLastService.text = "Last service: ${item.lastServiceDate ?: "Not available"}"

        holder.tvStatus.text = item.status
        holder.tvOdometer.text = item.odometerKm?.let { "$it km" } ?: "-"

        // Load cached photo from S3 URL
        holder.imgThumb.load(item.photoUrl ?: "") {
            placeholder(R.drawable.ic_ambulance)
            error(R.drawable.ic_att_absent)
            crossfade(true)
            diskCachePolicy(CachePolicy.ENABLED)   // cache in disk
            memoryCachePolicy(CachePolicy.ENABLED) // cache in memory
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<VehicleListItem>) {
        items = newList
        notifyDataSetChanged()
    }
}

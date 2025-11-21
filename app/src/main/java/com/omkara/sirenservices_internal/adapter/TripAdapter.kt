package com.omkara.sirenservices_internal.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.TripModel
import com.omkara.sirenservices_internal.activities.ViewTripActivity
import com.omkara.sirenservices_internal.activities.EditTripActivity

class TripAdapter(
    private var items: List<TripModel>,
    private val onClick: (TripModel) -> Unit
) : RecyclerView.Adapter<TripAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTripNumber: TextView = itemView.findViewById(R.id.tvTripNumber)
        val tvVehicle: TextView = itemView.findViewById(R.id.tvVehicle)
        val tvDriver: TextView = itemView.findViewById(R.id.tvDriver)
        val tvRoute: TextView = itemView.findViewById(R.id.tvRoute)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val btnView: TextView = itemView.findViewById(R.id.btnView)
        val btnEdit: TextView = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_trip, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = items[position]
        val context = holder.itemView.context

        holder.tvTripNumber.text = t.trip_number.ifEmpty { "—" }
        holder.tvVehicle.text = "Vehicle: ${t.vehicle_display ?: "-"}"
        holder.tvDriver.text = "Driver: ${t.driver_display ?: "-"}"

        val route = buildString {
            append(t.pickup ?: "-")
            if (!t.intermediate_stops.isNullOrEmpty()) {
                append(" → ${t.intermediate_stops.joinToString(" → ")}")
            }
            append(" → ${t.final_drop ?: "-"}")
        }

        holder.tvRoute.text = route
        holder.tvDate.text = "Date: ${t.trip_date ?: "-"}"
        holder.tvStatus.text = t.status ?: "-"

        // --- View Button ---
        holder.btnView.setOnClickListener {
            val i = Intent(context, ViewTripActivity::class.java)
            i.putExtra("trip_id", t.id)
            context.startActivity(i)
        }

        // Hide edit button if completed
        holder.btnEdit.visibility =
            if (t.status == "COMPLETED") View.GONE else View.VISIBLE

        // --- Edit Button ---
        holder.btnEdit.setOnClickListener {
            val i = Intent(context, EditTripActivity::class.java)
            i.putExtra("trip_id", t.id)
            context.startActivity(i)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<TripModel>) {
        items = newList
        notifyDataSetChanged()
    }
}

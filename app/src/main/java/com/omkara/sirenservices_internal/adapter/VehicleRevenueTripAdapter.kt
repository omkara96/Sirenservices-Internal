package com.omkara.sirenservices_internal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.TripRevenueModel

class VehicleRevenueTripAdapter(
    private var list: List<TripRevenueModel>,
    private val onOpenClicked: (TripRevenueModel) -> Unit
) : RecyclerView.Adapter<VehicleRevenueTripAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val txtId: TextView = v.findViewById(R.id.txtTripId)
        val txtRevenue: TextView = v.findViewById(R.id.txtTripRevenue)
        val txtExpense: TextView = v.findViewById(R.id.txtTripExpense)
        val btnOpen: Button = v.findViewById(R.id.btnOpenTrip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vehicle_revenue, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]

        holder.txtId.text = "Trip ID: ${item.tripId}"
        holder.txtRevenue.text = "Revenue: ₹${item.revenue}"
        holder.txtExpense.text = "Expenses: ₹${item.expenses}"

        holder.btnOpen.setOnClickListener { onOpenClicked(item) }
    }

    override fun getItemCount(): Int = list.size

    fun update(newList: List<TripRevenueModel>) {
        list = newList
        notifyDataSetChanged()
    }
}

package com.omkara.sirenservices_internal.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.activities.TripEditActivity
import com.omkara.sirenservices_internal.models.TripModel

class TripsAdapter(private val trips: List<TripModel>) : RecyclerView.Adapter<TripsAdapter.TripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]
        holder.bind(trip)
    }

    override fun getItemCount(): Int {
        return trips.size
    }

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tripNumberTextView: TextView = itemView.findViewById(R.id.trip_number_text_view)
        private val vehicleNumberTextView: TextView = itemView.findViewById(R.id.vehicle_number_text_view)
        private val driverNameTextView: TextView = itemView.findViewById(R.id.driver_name_text_view)
        private val statusTextView: TextView = itemView.findViewById(R.id.status_text_view)

        fun bind(trip: TripModel) {
            tripNumberTextView.text = trip.trip_number
            vehicleNumberTextView.text = trip.vehicle_number
            driverNameTextView.text = trip.driver_name
            statusTextView.text = trip.status

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, TripEditActivity::class.java).apply {
                    putExtra("TRIP_ID", trip.trip_id)
                }
                context.startActivity(intent)
            }
        }
    }
}
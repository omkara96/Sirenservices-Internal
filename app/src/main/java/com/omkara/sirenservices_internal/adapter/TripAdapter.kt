package com.omkara.sirenservices_internal.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.activities.BillGeneratorActivity
import com.omkara.sirenservices_internal.activities.EditTripActivity
import com.omkara.sirenservices_internal.activities.ViewBillActivity
import com.omkara.sirenservices_internal.activities.ViewTripActivity
import com.omkara.sirenservices_internal.models.TripModel

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
        val tvCustomer: TextView = itemView.findViewById(R.id.tvCust)

        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)

        val btnView: MaterialButton = itemView.findViewById(R.id.btnView)
        val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)

        val layoutBillingActions: View =
            itemView.findViewById(R.id.layoutBillingActions)

        val btnGenerateBill: MaterialButton =
            itemView.findViewById(R.id.btnGenerateBill)

        val btnViewBill: MaterialButton =
            itemView.findViewById(R.id.btnViewBill)

        val btnEditBill: MaterialButton =
            itemView.findViewById(R.id.btnEditBill)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = items[position]
        val context = holder.itemView.context

        // ---------------- BASIC DATA ----------------

        holder.tvTripNumber.text = t.trip_number.ifEmpty { "Trip —" }
        holder.tvVehicle.text = "🚑 Vehicle: ${t.vehicle_display ?: "-"}"
        holder.tvDriver.text = "👨‍✈️ Driver: ${t.driver_display ?: "-"}"
        holder.tvCustomer.text = "👤️ Customer: ${t.patient_name ?: "-"}"

        val route = buildString {
            append(t.pickup ?: "-")
            if (!t.intermediate_stops.isNullOrEmpty()) {
                append(" → ${t.intermediate_stops.joinToString(" → ")}")
            }
            append(" → ${t.final_drop ?: "-"}")
        }
        holder.tvRoute.text = "📍 Route: $route"
        holder.tvDate.text = "🗓 Date: ${t.trip_date ?: "-"}"

        // ---------------- STATUS CHIP ----------------

        val status = t.status ?: "UNKNOWN"
        holder.chipStatus.text = status

        when (status) {
            "PLANNED" -> holder.chipStatus.setChipBackgroundColorResource(
                R.color.md_theme_secondaryContainer
            )

            "ONGOING" -> holder.chipStatus.setChipBackgroundColorResource(
                R.color.md_theme_primaryContainer
            )

            "COMPLETED" -> holder.chipStatus.setChipBackgroundColorResource(
                R.color.md_theme_tertiaryContainer
            )
        }

        // ---------------- VIEW TRIP ----------------

        holder.btnView.setOnClickListener {
            context.startActivity(
                Intent(context, ViewTripActivity::class.java)
                    .putExtra("trip_id", t.id)
            )
        }

        // ---------------- EDIT TRIP ----------------

        holder.btnEdit.visibility =
            if (status == "COMPLETED") View.GONE else View.VISIBLE

        holder.btnEdit.setOnClickListener {
            context.startActivity(
                Intent(context, EditTripActivity::class.java)
                    .putExtra("trip_id", t.id)
            )
        }

        // ---------------- BILLING LOGIC ----------------

        val isCompleted = status == "COMPLETED"
        val isBillGenerated = t.isBillGenerated
        Log.d(
            "TripAdapter",
            "bind → trip=${t.trip_number}, completed=$isCompleted, billGenerated=$isBillGenerated"
        )
        Log.i("TripAdapter", t.toString())
        // Reset states (IMPORTANT for RecyclerView)
        holder.layoutBillingActions.visibility = View.GONE
        holder.btnGenerateBill.visibility = View.GONE
        holder.btnViewBill.visibility = View.GONE
        holder.btnEditBill.visibility = View.GONE

        if (isCompleted) {
            holder.layoutBillingActions.visibility = View.VISIBLE

            if (!isBillGenerated!!) {
                holder.btnGenerateBill.visibility = View.VISIBLE
                holder.btnGenerateBill.setOnClickListener {
                    context.startActivity(
                        Intent(context, BillGeneratorActivity::class.java).apply {
                            putExtra("trip_id", t.id)
                            putExtra("trip_cost", t.trip_cost)
                        }
                    )
                }
            } else {
                holder.btnGenerateBill.visibility = View.GONE
                holder.btnViewBill.visibility = View.VISIBLE
                holder.btnEditBill.visibility = View.VISIBLE

                holder.btnViewBill.setOnClickListener {
                    context.startActivity(
                        Intent(context, ViewBillActivity::class.java).apply {
                            putExtra("trip_id", t.id)
                        }
                    )
                }

                holder.btnEditBill.setOnClickListener {
                    val intent = Intent(context, BillGeneratorActivity::class.java).apply {
                        putExtra("trip_id", t.id)
                        putExtra("trip_cost", t.trip_cost)
                        putExtra("mode", "EDIT") // optional
                    }
                    context.startActivity(intent)

                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<TripModel>) {
        items = newList
        notifyDataSetChanged()
    }
}

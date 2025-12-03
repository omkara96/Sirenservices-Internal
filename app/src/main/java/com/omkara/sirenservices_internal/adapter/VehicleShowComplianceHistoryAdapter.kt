package com.omkara.sirenservices_internal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.ComplianceCurrent
import java.text.SimpleDateFormat
import java.util.*

class VehicleShowComplianceHistoryAdapter(
    private var fullList: List<ComplianceCurrent>
) : RecyclerView.Adapter<VehicleShowComplianceHistoryAdapter.HV>() {

    private var filteredList: List<ComplianceCurrent> = fullList

    class HV(v: View) : RecyclerView.ViewHolder(v) {
        val txtProvider: TextView = v.findViewById(R.id.txtProvider)
        val txtDetails: TextView = v.findViewById(R.id.txtDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HV {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_entry, parent, false)
        return HV(v)
    }

    override fun onBindViewHolder(holder: HV, position: Int) {
        val item = filteredList[position]

        holder.txtProvider.text = item.provider ?: "-"

        holder.txtDetails.text = """
            Number: ${item.number ?: "-"}
            Certificate No: ${item.certificate_no ?: "-"}
            Type: ${item.type ?: "-"}
            Premium: ${item.premium ?: "-"}
            Validity: ${item.valid_from ?: "-"} → ${item.valid_till ?: "-"}
            Updated: ${formatDate(item.updated_at)}
        """.trimIndent()
    }

    override fun getItemCount(): Int = filteredList.size

    fun setFilter(keyword: String) {
        val lower = keyword.lowercase()

        filteredList = if (lower.isBlank()) {
            fullList
        } else {
            fullList.filter {
                (it.provider ?: "").lowercase().contains(lower) ||
                        (it.number ?: "").lowercase().contains(lower) ||
                        (it.type ?: "").lowercase().contains(lower)
            }
        }
        notifyDataSetChanged()
    }

    private fun formatDate(t: com.google.firebase.Timestamp?): String {
        if (t == null) return "-"
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return sdf.format(t.toDate())
    }
}

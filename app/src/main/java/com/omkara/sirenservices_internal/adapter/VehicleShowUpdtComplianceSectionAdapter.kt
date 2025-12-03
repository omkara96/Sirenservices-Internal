package com.omkara.sirenservices_internal.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.ComplianceCurrent
import java.text.SimpleDateFormat
import java.util.*

class VehicleShowUpdtComplianceSectionAdapter(
    private var items: MutableList<Triple<String, ComplianceCurrent?, List<ComplianceCurrent>>>,
    private val onEditClicked: (String, ComplianceCurrent?) -> Unit,
    private val onHistoryClicked: (String) -> Unit   // <-- FIX: Now history receives compliance type
) : RecyclerView.Adapter<VehicleShowUpdtComplianceSectionAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val cardRoot: CardView = v.findViewById(R.id.cardComplianceRoot)
        val txtTitle: TextView = v.findViewById(R.id.txtComplianceTitle)
        val txtDetails: TextView = v.findViewById(R.id.txtComplianceDetails)
        val btnEdit: View = v.findViewById(R.id.btnComplianceEdit)
        val btnHistory: View = v.findViewById(R.id.btnComplianceHistory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compliance_section, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (type, current, historyList) = items[position]

        val title = when (type) {
            "insurance" -> "Insurance"
            "puc" -> "PUC"
            "permit" -> "Permit"
            "fitness" -> "Fitness"
            else -> type.uppercase()
        }

        holder.txtTitle.text = title

        holder.txtDetails.text =
            if (current == null) {
                "No active $title data"
            } else {
                """
                Provider: ${current.provider ?: "-"}
                Number: ${current.number ?: "-"}
                Certificate No: ${current.certificate_no ?: "-"}
                Type: ${current.type ?: "-"}
                Premium: ${current.premium ?: "-"}
                Validity: ${current.valid_from ?: "-"} → ${current.valid_till ?: "-"}
                """.trimIndent()
            }

        // expired = red background
        if (current != null && isExpired(current.valid_till)) {
            holder.cardRoot.setCardBackgroundColor(Color.parseColor("#FFCDD2"))
        } else {
            holder.cardRoot.setCardBackgroundColor(Color.WHITE)
        }

        // actions
        holder.btnEdit.setOnClickListener { onEditClicked(type, current) }
        holder.btnHistory.setOnClickListener { onHistoryClicked(type) } // <-- FIX
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newList: List<Triple<String, ComplianceCurrent?, List<ComplianceCurrent>>>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    private fun isExpired(validTill: String?): Boolean {
        if (validTill.isNullOrBlank()) return false
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val exp = sdf.parse(validTill) ?: return false
            exp.before(Date())
        } catch (e: Exception) {
            false
        }
    }
}

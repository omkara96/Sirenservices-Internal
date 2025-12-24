package com.omkara.sirenservices_internal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.AlertType
import com.omkara.sirenservices_internal.models.DashboardAlert

class DashboardAlertAdapter(
    private val items: List<DashboardAlert>,
    private val onClick: (DashboardAlert) -> Unit
) : RecyclerView.Adapter<DashboardAlertAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val card: MaterialCardView = v.findViewById(R.id.cardAlert)
        val icon: TextView = v.findViewById(R.id.tvAlertIcon)
        val title: TextView = v.findViewById(R.id.tvAlertTitle)
        val desc: TextView = v.findViewById(R.id.tvAlertDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_alert, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val alert = items[position]
        val ctx = holder.itemView.context

        holder.title.text = alert.title
        holder.desc.text = alert.description

        // ---------- ICON ----------
        holder.icon.text = when (alert.type) {
            AlertType.BILL_PENDING -> "💰"
            AlertType.COMPLIANCE_EXPIRED -> "❌"
            AlertType.COMPLIANCE_EXPIRING -> "⏳"
        }

        // ---------- SEVERITY COLORS ----------
        when (alert.severity) {
            3 -> { // CRITICAL
                holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(ctx, R.color.alert_red_bg)
                )
                holder.title.setTextColor(
                    ContextCompat.getColor(ctx, R.color.alert_red)
                )
            }

            2 -> { // WARNING
                holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(ctx, R.color.alert_orange_bg)
                )
                holder.title.setTextColor(
                    ContextCompat.getColor(ctx, R.color.alert_orange)
                )
            }

            else -> { // INFO
                holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(ctx, R.color.alert_blue_bg)
                )
                holder.title.setTextColor(
                    ContextCompat.getColor(ctx, R.color.alert_blue)
                )
            }
        }

        holder.itemView.setOnClickListener { onClick(alert) }
    }

    override fun getItemCount(): Int = items.size
}

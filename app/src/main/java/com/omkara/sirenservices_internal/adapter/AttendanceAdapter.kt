package com.omkara.sirenservices_internal.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.AttendanceModel

class AttendanceAdapter(
    private var list: List<AttendanceModel>
) : RecyclerView.Adapter<AttendanceAdapter.AttViewHolder>() {

    class AttViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val txtDate: TextView = v.findViewById(R.id.txtAttDate)
        val txtStatus: TextView = v.findViewById(R.id.txtAttStatus)
        val txtTime: TextView = v.findViewById(R.id.txtAttTime)
        val txtNotes: TextView = v.findViewById(R.id.txtAttNotes)
        val iconStatus: ImageView = v.findViewById(R.id.imgAttIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return AttViewHolder(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: AttViewHolder, pos: Int) {
        val a = list[pos]

        holder.txtDate.text = a.date
        holder.txtStatus.text = a.status.uppercase()

        // Time
        holder.txtTime.text = when {
            a.checkInTime != null && a.checkOutTime != null ->
                "IN: ${a.checkInTime} | OUT: ${a.checkOutTime}"
            a.checkInTime != null ->
                "IN: ${a.checkInTime}"
            else -> "No Check-in"
        }

        holder.txtNotes.text = a.notes ?: ""

        // Color + Icon based on status
        when (a.status.lowercase()) {
            "present" -> {
                holder.iconStatus.setImageResource(R.drawable.ic_att_present)
                holder.txtStatus.setTextColor(holder.itemView.context.getColor(R.color.att_present_green))
            }
            "late" -> {
                holder.iconStatus.setImageResource(R.drawable.ic_att_late)
                holder.txtStatus.setTextColor(holder.itemView.context.getColor(R.color.att_late_orange))
            }
            "halfday" -> {
                holder.iconStatus.setImageResource(R.drawable.ic_att_half)
                holder.txtStatus.setTextColor(holder.itemView.context.getColor(R.color.att_half_yellow))
            }
            "leave" -> {
                holder.iconStatus.setImageResource(R.drawable.ic_att_leave)
                holder.txtStatus.setTextColor(holder.itemView.context.getColor(R.color.att_leave_blue))
            }
            "holiday" -> {
                holder.iconStatus.setImageResource(R.drawable.ic_att_holiday)
                holder.txtStatus.setTextColor(holder.itemView.context.getColor(R.color.att_holiday_teal))
            }
            else -> { // absent
                holder.iconStatus.setImageResource(R.drawable.ic_att_absent)
                holder.txtStatus.setTextColor(holder.itemView.context.getColor(R.color.att_absent_red))
            }
        }
    }

    fun update(newList: List<AttendanceModel>) {
        this.list = newList
        notifyDataSetChanged()
    }
}

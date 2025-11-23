package com.omkara.sirenservices_internal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.SalaryEntry
import java.text.SimpleDateFormat
import java.util.*

class SalaryEntryAdapter(private var list: List<SalaryEntry>) :
    RecyclerView.Adapter<SalaryEntryAdapter.SalaryVH>() {

    inner class SalaryVH(v: View) : RecyclerView.ViewHolder(v) {
        val type = v.findViewById<TextView>(R.id.txtEntryType)
        val amount = v.findViewById<TextView>(R.id.txtAmount)
        val note = v.findViewById<TextView>(R.id.txtNote)
        val time = v.findViewById<TextView>(R.id.txtTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalaryVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_salary_entry, parent, false)
        return SalaryVH(v)
    }

    override fun onBindViewHolder(holder: SalaryVH, pos: Int) {
        val e = list[pos]

        holder.type.text = e.type.replaceFirstChar { it.uppercase() }
        holder.amount.text = if (e.amount >= 0) "+₹${e.amount}" else "-₹${Math.abs(e.amount)}"
        holder.note.text = e.note

        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(e.timestamp))
        holder.time.text = date
    }

    override fun getItemCount(): Int = list.size

    fun update(newList: List<SalaryEntry>) {
        list = newList
        notifyDataSetChanged()
    }
}

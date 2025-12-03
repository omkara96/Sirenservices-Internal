package com.omkara.sirenservices_internal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.VehicleDocument

class VehicleDocumentsAdapter(
    private var list: List<VehicleDocument>,
    private val onOpenClicked: (VehicleDocument) -> Unit,
    private val onDownloadClicked: (VehicleDocument) -> Unit
) : RecyclerView.Adapter<VehicleDocumentsAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val card: MaterialCardView = v.findViewById(R.id.cardDocument)
        val txtName: TextView = v.findViewById(R.id.txtDocName)
        val txtDate: TextView = v.findViewById(R.id.txtDocDate)
        val btnOpen: ImageButton = v.findViewById(R.id.btnOpenDocument)
        val btnDownload: ImageButton = v.findViewById(R.id.btnDownloadDocument)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vehicle_document, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val doc = list[position]

        holder.txtName.text = doc.name
        holder.txtDate.text = "Uploaded: ${doc.uploadedAt}"

        holder.btnOpen.setOnClickListener { onOpenClicked(doc) }
        holder.btnDownload.setOnClickListener { onDownloadClicked(doc) }
    }

    override fun getItemCount(): Int = list.size

    fun update(newList: List<VehicleDocument>) {
        list = newList
        notifyDataSetChanged()
    }
}

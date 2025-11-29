package com.omkara.sirenservices_internal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.omkara.sirenservices_internal.R
import android.view.View
import android.net.Uri
import android.content.Context
import coil.load
import coil.request.CachePolicy

class PhotoAdapter(
    private val ctx: Context,
    private val items: List<String>,
    private val onClick: (String, ImageView) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.Holder>() {

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgThumb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_photo_thumb, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val url = items[position]
        // Use coil (or your load method) — you asked to avoid Glide; Coil is used elsewhere, but if you
        // use your own loader just replace this block. I keep disk read only so it reads local cache first.
        holder.img.load(url) {
            placeholder(R.drawable.ic_ambulance)
            error(R.drawable.ic_att_absent)
            diskCachePolicy(CachePolicy.ENABLED)
            memoryCachePolicy(CachePolicy.ENABLED)
            listener(onSuccess = { _, _ -> }, onError = { _, _ -> })
        }

        holder.img.setOnClickListener {
            onClick(url, holder.img)
        }
    }

    override fun getItemCount(): Int = items.size
}

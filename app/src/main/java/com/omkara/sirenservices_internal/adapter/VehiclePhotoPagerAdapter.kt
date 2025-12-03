package com.omkara.sirenservices_internal.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.activities.FullScreenImageActivity

class PhotoPagerAdapter(
    private val context: Context,
    private val urls: List<String>
) : RecyclerView.Adapter<PhotoPagerAdapter.PhotoVH>() {

    inner class PhotoVH(val img: ImageView) : RecyclerView.ViewHolder(img)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoVH {
        val img = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,   // IMPORTANT
                ViewGroup.LayoutParams.MATCH_PARENT    // MUST be match_parent
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return PhotoVH(img)
    }

    override fun onBindViewHolder(holder: PhotoVH, position: Int) {
        val url = urls[position]

        holder.img.load(url) {
            placeholder(R.drawable.ic_doc_placeholder)
            error(R.drawable.ic_doc_placeholder)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(true)
        }

        holder.img.setOnClickListener {
            val intent = Intent(context, FullScreenImageActivity::class.java)
            intent.putExtra("image_url", url)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = urls.size
}

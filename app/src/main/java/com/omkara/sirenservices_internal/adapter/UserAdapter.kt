package com.omkara.sirenservices_internal.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.UserModel

class UserAdapter(
    private val onClick: (UserModel) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserVH>() {

    private val fullList = mutableListOf<UserModel>()
    private val filtered = mutableListOf<UserModel>()

    fun submitList(list: List<UserModel>) {
        fullList.clear()
        fullList.addAll(list)
        filter("") // initial load
    }

    fun filter(query: String, filters: Set<String> = emptySet()) {
        filtered.clear()

        filtered.addAll(
            fullList.filter { u ->
                val matchQuery = (
                        u.firstName.contains(query, true) ||
                                u.mobile.contains(query)
                        )

                val matchFilters =
                    when {
                        filters.contains("available") && u.driver?.availability != "available" -> false
                        filters.contains("occupied") && u.driver?.availability != "occupied" -> false
                        filters.contains("active") && u.status != "active" -> false
                        filters.contains("inactive") && u.status != "inactive" -> false
                        else -> true
                    }

                matchQuery && matchFilters
            }
        )

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        UserVH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user_card, parent, false)
        )

    override fun getItemCount() = filtered.size

    override fun onBindViewHolder(holder: UserVH, pos: Int) {
        holder.bind(filtered[pos])
    }

    inner class UserVH(v: View) : RecyclerView.ViewHolder(v) {
        private val img = v.findViewById<ImageView>(R.id.imgProfile)
        private val tvName = v.findViewById<TextView>(R.id.tvName)
        private val tvMobile = v.findViewById<TextView>(R.id.tvMobile)
        private val tvAvailability = v.findViewById<TextView>(R.id.tvAvailability)
        private val tvStatus = v.findViewById<TextView>(R.id.tvStatus)
        private val tvTripsToday = v.findViewById<TextView>(R.id.tvTripsToday)
        private val tvPresent = v.findViewById<TextView>(R.id.tvPresentToday)
        private val tvRole = v.findViewById<TextView>(R.id.tvRole)

        fun bind(u: UserModel) {
            tvName.text = "${u.firstName} ${u.lastName ?: ""}"
            tvMobile.text = u.mobile
            tvRole.text = u.role.capitalize()

            // status & availability
            tvStatus.text = if (u.status == "active") "Active" else "Inactive"
            tvAvailability.text = u.driver?.availability ?: "N/A"
            tvPresent.text = if (u.attendanceToday == true) "Present" else "Absent"
            tvTripsToday.text = "Trips Today: ${u.tripsToday ?: 0}"

            img.load(u.profilePhoto ?: "") {
                placeholder(R.drawable.ic_user)
                error(R.drawable.ic_user)
                crossfade(true)
                diskCachePolicy(CachePolicy.READ_ONLY)   // always read from disk
            }

            itemView.setOnClickListener { onClick(u) }
        }
    }
}

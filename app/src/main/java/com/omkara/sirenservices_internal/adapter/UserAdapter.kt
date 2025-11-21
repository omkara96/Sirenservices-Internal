package com.omkara.sirenservices_internal.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.UserModel

class UserAdapter(
    private val userList: List<UserModel>,
    private val onUserClicked: (String) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserVH>() {

    inner class UserVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvMobile: TextView = itemView.findViewById(R.id.tvMobile)
        val tvRole: TextView = itemView.findViewById(R.id.tvRole)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserVH(v)
    }

    override fun onBindViewHolder(holder: UserVH, position: Int) {
        val u = userList[position]

        val fullName = "${u.firstName} ${u.middleName} ${u.lastName}".trim()

        holder.tvName.text = fullName
        holder.tvMobile.text = u.mobile
        holder.tvRole.text = u.role
        holder.tvStatus.text = u.status

        holder.itemView.setOnClickListener {
            onUserClicked(u.id)
        }
    }

    override fun getItemCount(): Int = userList.size
}

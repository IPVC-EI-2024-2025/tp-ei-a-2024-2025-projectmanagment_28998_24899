package com.baptistaz.taskwave.ui.home.admin.manageusers

import User
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R

class UserAdapter(private val users: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.text_name)
        val email = view.findViewById<TextView>(R.id.text_email)
        val role = view.findViewById<TextView>(R.id.text_role)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.name.text = user.name
        holder.email.text = user.email

        // Badge de perfil (Admin, Manager, User)
        when (user.profileType.lowercase()) {
            "admin" -> {
                holder.role.text = "Admin"
                holder.role.setBackgroundResource(R.drawable.role_badge_admin)
                holder.role.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.background_white))
            }
            "manager" -> {
                holder.role.text = "Manager"
                holder.role.setBackgroundResource(R.drawable.role_badge_manager)
                holder.role.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.background_white))
            }
            "user" -> {
                holder.role.text = "User"
                holder.role.setBackgroundResource(R.drawable.role_badge_user)
                holder.role.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.background_white))
            }
        }
    }

}

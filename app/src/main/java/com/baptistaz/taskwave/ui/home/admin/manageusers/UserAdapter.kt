package com.baptistaz.taskwave.ui.home.admin.manageusers

import User
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R

class UserAdapter(
    private val users: List<User>,
    private val onDeleteClick: ((String) -> Unit)? = null,
    private val onItemClick: ((User) -> Unit)? = null
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.text_name)
        val email = view.findViewById<TextView>(R.id.text_email)
        val role = view.findViewById<TextView>(R.id.text_role)
        val editIcon = view.findViewById<ImageView>(R.id.icon_edit_user)
        val deleteIcon = view.findViewById<ImageView>(R.id.icon_delete_user)
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

        // Só permite editar/apagar se NÃO for admin
        if (user.profileType.equals("ADMIN", ignoreCase = true)) {
            holder.editIcon.visibility = View.GONE
            holder.deleteIcon.visibility = View.GONE
        } else {
            holder.editIcon.visibility = View.VISIBLE
            holder.deleteIcon.visibility = View.VISIBLE

            holder.editIcon.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, EditUserActivity::class.java)
                intent.putExtra("userId", user.id_user)
                context.startActivity(intent)
            }

            holder.deleteIcon.setOnClickListener {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Eliminar Utilizador")
                    .setMessage("Tens a certeza que queres eliminar este utilizador?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        onDeleteClick?.invoke(user.id_user!!)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        // Badge de perfil (Admin, Manager, User)
        when (user.profileType.lowercase()) {
            "admin" -> {
                holder.role.text = "Admin"
                holder.role.setBackgroundResource(R.drawable.role_badge_admin)
                holder.role.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.background_white))
            }
            "gestor" -> {
                holder.role.text = "GESTOR"
                holder.role.setBackgroundResource(R.drawable.role_badge_manager)
                holder.role.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.background_white))
            }
            "user" -> {
                holder.role.text = "User"
                holder.role.setBackgroundResource(R.drawable.role_badge_user)
                holder.role.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.background_white))
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(user)
        }

    }

}

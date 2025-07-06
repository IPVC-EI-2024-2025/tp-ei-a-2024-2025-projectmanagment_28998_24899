package com.baptistaz.taskwave.ui.home.admin.manageusers

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
import com.baptistaz.taskwave.data.model.auth.User

/**
 * RecyclerView adapter for displaying and managing users in the admin panel.
 *
 * @param users List of users to display.
 * @param onDeleteClick Optional callback triggered when a user is deleted.
 * @param onItemClick Optional callback triggered when a user item is clicked.
 */
class UserAdapter(
    private val users: List<User>,
    private val onDeleteClick: ((String) -> Unit)? = null,
    private val onItemClick: ((User) -> Unit)? = null
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    /** ViewHolder class for user items. */
    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.text_name)
        val email: TextView = view.findViewById(R.id.text_email)
        val role: TextView = view.findViewById(R.id.text_role)
        val editIcon: ImageView = view.findViewById(R.id.icon_edit_user)
        val deleteIcon: ImageView = view.findViewById(R.id.icon_delete_user)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        val context = holder.itemView.context

        holder.name.text = user.name
        holder.email.text = user.email

        // Show/hide edit and delete icons depending on profile
        if (user.profileType.equals("ADMIN", ignoreCase = true)) {
            holder.editIcon.visibility = View.GONE
            holder.deleteIcon.visibility = View.GONE
        } else {
            holder.editIcon.visibility = View.VISIBLE
            holder.deleteIcon.visibility = View.VISIBLE

            holder.editIcon.setOnClickListener {
                val intent = Intent(context, EditUserActivity::class.java)
                intent.putExtra("USER_ID", user.id_user)
                context.startActivity(intent)
            }

            holder.deleteIcon.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Eliminar Utilizador")
                    .setMessage("Tens a certeza que queres eliminar este utilizador?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        onDeleteClick?.invoke(user.id_user!!)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        // Role badge and label styling
        when (user.profileType.lowercase()) {
            "admin" -> {
                holder.role.text = context.getString(R.string.role_admin)
                holder.role.setBackgroundResource(R.drawable.role_badge_admin)
            }
            "gestor" -> {
                holder.role.text = context.getString(R.string.role_manager)
                holder.role.setBackgroundResource(R.drawable.role_badge_manager)
            }
            "user" -> {
                holder.role.text = context.getString(R.string.role_user)
                holder.role.setBackgroundResource(R.drawable.role_badge_user)
            }
        }

        holder.role.setTextColor(ContextCompat.getColor(context, R.color.background_white))

        // Trigger external callback when item is clicked
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(user)
        }
    }
}

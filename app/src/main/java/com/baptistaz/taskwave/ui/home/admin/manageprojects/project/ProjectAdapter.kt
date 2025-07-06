package com.baptistaz.taskwave.ui.home.admin.manageprojects.project

import com.baptistaz.taskwave.data.model.auth.User
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.project.Project

class ProjectAdapter(
    private var projectList: List<Project>,
    private val managers: List<User>,
    private val context: Context,
    private val onDelete: (Project) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    class ProjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.text_project_name)
        val managerText: TextView = view.findViewById(R.id.text_manager_name)
        val statusText: TextView = view.findViewById(R.id.text_status)
        val editIcon: ImageView = view.findViewById(R.id.icon_edit)
        val deleteIcon: ImageView = view.findViewById(R.id.icon_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projectList[position]

        holder.nameText.text = project.name ?: "Projeto sem nome"

        // Manager
        val managerName = managers.firstOrNull { it.id_user == project.idManager }?.name ?: "No manager"
        holder.managerText.text = "Manager: $managerName"

        // Status
        holder.statusText.text = project.status ?: "N/A"
        val statusColor = when (project.status?.lowercase() ?: "") {
            "active"    -> R.color.green
            "completed" -> R.color.gray
            else        -> R.color.black
        }
        holder.statusText.setTextColor(holder.itemView.context.getColor(statusColor))

        // Só escondemos editar em completos
        val isComplete = project.status.equals("Completed", ignoreCase = true)
        holder.editIcon.visibility = if (isComplete) View.GONE else View.VISIBLE

        // Delete sempre visível
        holder.deleteIcon.visibility = View.VISIBLE

        // Clique no ícone de editar (só se não for completo)
        holder.editIcon.setOnClickListener {
            if (!isComplete) {
                val intent = Intent(context, EditProjectActivity::class.java)
                intent.putExtra("project", project)
                context.startActivity(intent)
            }
        }

        // Delete sempre disponível
        holder.deleteIcon.setOnClickListener {
            onDelete(project)
        }

        // ItemView sempre clicável para detalhes
        holder.itemView.isClickable = true
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProjectDetailsActivity::class.java)
            intent.putExtra("project", project)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = projectList.size

    fun updateData(newList: List<Project>) {
        projectList = newList
        notifyDataSetChanged()
    }
}

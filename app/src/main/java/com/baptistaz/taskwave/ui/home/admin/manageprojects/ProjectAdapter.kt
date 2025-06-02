package com.baptistaz.taskwave.ui.home.admin.manageprojects

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Project

class ProjectAdapter(
    private var projectList: List<Project>,
    private val onEdit: (Project) -> Unit,
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

        holder.nameText.text = project.name
        holder.managerText.text = "Manager: Em breve"
        holder.statusText.text = project.status

        val statusColor = when (project.status.lowercase()) {
            "active" -> R.color.green
            "completed" -> R.color.gray
            else -> R.color.black
        }
        holder.statusText.setTextColor(holder.itemView.context.getColor(statusColor))

        // Ações nos ícones
        holder.editIcon.setOnClickListener {
            onEdit(project)
        }
        holder.deleteIcon.setOnClickListener {
            onDelete(project)
        }
    }

    override fun getItemCount(): Int = projectList.size

    fun updateData(newList: List<Project>) {
        projectList = newList
        notifyDataSetChanged()
    }
}



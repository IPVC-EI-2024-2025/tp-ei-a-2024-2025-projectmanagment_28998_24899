package com.baptistaz.taskwave.ui.home.admin.manageprojects.project

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.auth.User
import com.baptistaz.taskwave.data.model.project.Project

/**
 * RecyclerView Adapter for displaying a list of projects in the Admin interface.
 *
 * @param projectList List of projects to display.
 * @param managers List of all users with manager profile.
 * @param context Android context for starting activities.
 * @param onDelete Callback for when a delete icon is clicked.
 */
class ProjectAdapter(
    private var projectList: List<Project>,
    private val managers: List<User>,
    private val context: Context,
    private val onDelete: (Project) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    /** Holds view references for each project item in the list. */
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

        // Project name
        holder.nameText.text = project.name ?: "Projeto sem nome"

        // Manager name
        val managerName = managers.firstOrNull { it.id_user == project.idManager }?.name ?: "No manager"
        holder.managerText.text = "Manager: $managerName"

        // Status and dynamic color
        holder.statusText.text = project.status ?: "N/A"
        val statusColor = when (project.status?.lowercase() ?: "") {
            "active"    -> R.color.green
            "completed" -> R.color.gray
            else        -> R.color.black
        }
        holder.statusText.setTextColor(holder.itemView.context.getColor(statusColor))

        // Hide edit icon if project is completed
        val isComplete = project.status.equals("Completed", ignoreCase = true)
        holder.editIcon.visibility = if (isComplete) View.GONE else View.VISIBLE

        // Delete icon always visible
        holder.deleteIcon.visibility = View.VISIBLE

        // Edit button action
        holder.editIcon.setOnClickListener {
            if (!isComplete) {
                val intent = Intent(context, EditProjectActivity::class.java)
                intent.putExtra("project", project)
                context.startActivity(intent)
            }
        }

        // Delete button action
        holder.deleteIcon.setOnClickListener {
            onDelete(project)
        }

        // Navigate to project details on item click
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProjectDetailsActivity::class.java)
            intent.putExtra("project", project)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = projectList.size

    /**
     * Updates the adapter's project list and refreshes the view.
     */
    fun updateData(newList: List<Project>) {
        projectList = newList
        notifyDataSetChanged()
    }
}

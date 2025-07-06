package com.baptistaz.taskwave.ui.home.manager.project

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.project.Project
import com.baptistaz.taskwave.ui.home.manager.project.details.ManagerProjectDetailsActivity
import com.baptistaz.taskwave.ui.home.manager.project.details.ManagerProjectDetailsCompletedActivity

/**
 * Adapter for displaying a list of projects assigned to a manager.
 *
 * @param activity Host activity used for starting detail views.
 */
class ManagerProjectAdapter(
    private val activity: Activity
) : RecyclerView.Adapter<ManagerProjectAdapter.Holder>() {

    private var data: List<Project> = emptyList()

    /** ViewHolder representing a single project item. */
    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.text_project_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project_manager, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val project = data[position]
        holder.tvName.text = project.name

        holder.itemView.setOnClickListener {
            // Open details screen based on project status
            val status = project.status?.lowercase() ?: ""
            val intent = if (status == "completed" || status == "concluido") {
                Intent(activity, ManagerProjectDetailsCompletedActivity::class.java)
            } else {
                Intent(activity, ManagerProjectDetailsActivity::class.java)
            }
            intent.putExtra("PROJECT_ID", project.idProject)
            activity.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = data.size

    /**
     * Updates the project list and refreshes the UI.
     *
     * @param newData New list of projects to display.
     */
    fun updateData(newData: List<Project>) {
        data = newData
        notifyDataSetChanged()
    }
}

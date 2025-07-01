package com.baptistaz.taskwave.ui.home.manager

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Project

class ManagerProjectAdapter(
    private val activity: Activity
) : RecyclerView.Adapter<ManagerProjectAdapter.Holder>() {

    private var data: List<Project> = emptyList()

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
            // Decide activity destino com base no status
            val status = project.status?.lowercase() ?: ""
            if (status == "completed" || status == "concluido") {
                val intent = Intent(activity, ManagerProjectDetailsCompletedActivity::class.java)
                intent.putExtra("PROJECT_ID", project.idProject)
                activity.startActivity(intent)
            } else {
                val intent = Intent(activity, ManagerProjectDetailsActivity::class.java)
                intent.putExtra("PROJECT_ID", project.idProject)
                activity.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: List<Project>) {
        data = newData
        notifyDataSetChanged()
    }
}

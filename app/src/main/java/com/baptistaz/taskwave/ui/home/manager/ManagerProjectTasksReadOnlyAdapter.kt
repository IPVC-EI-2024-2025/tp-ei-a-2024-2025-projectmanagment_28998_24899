package com.baptistaz.taskwave.ui.home.manager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Task

class ManagerProjectTasksReadOnlyAdapter(
    private val tasks: List<Task>
) : RecyclerView.Adapter<ManagerProjectTasksReadOnlyAdapter.Holder>() {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.text_task_title)
        val tvStatus: TextView = view.findViewById(R.id.text_task_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_readonly, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val task = tasks[position]
        holder.tvTitle.text = task.title
        holder.tvStatus.text = task.state
    }

    override fun getItemCount(): Int = tasks.size
}

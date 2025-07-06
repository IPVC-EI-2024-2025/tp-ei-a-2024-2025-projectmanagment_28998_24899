package com.baptistaz.taskwave.ui.home.manager.tasks.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.task.Task

/**
 * Adapter for displaying tasks in read-only mode.
 * Used by managers to view project tasks without editing.
 *
 * @param tasks List of Task objects to display.
 * @param onItemClick Callback triggered when a task is clicked.
 */
class ManagerProjectTasksReadOnlyAdapter(
    private val tasks: List<Task>,
    private val onItemClick: (Task) -> Unit
) : RecyclerView.Adapter<ManagerProjectTasksReadOnlyAdapter.Holder>() {

    /** ViewHolder representing a single task row. */
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

        // Open task update details when item is clicked
        holder.itemView.setOnClickListener {
            onItemClick(task)
        }
    }

    override fun getItemCount(): Int = tasks.size
}

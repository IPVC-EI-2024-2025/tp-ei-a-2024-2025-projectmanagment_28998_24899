package com.baptistaz.taskwave.ui.home.admin.manageprojects.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.task.Task
import com.baptistaz.taskwave.data.model.task.TaskWithUser

/**
 * RecyclerView adapter for displaying a list of tasks.
 *
 * @param data List of TaskWithUser objects.
 * @param onClick Callback when an item is clicked.
 * @param onDelete Optional callback for delete action.
 * @param canEdit Whether delete button should be visible.
 * @param showResponsible Whether to show the responsible user.
 */
class TaskAdapter(
    private var data: List<TaskWithUser>,
    private val onClick: (Task) -> Unit,
    private val onDelete: ((Task) -> Unit)? = null,
    private val canEdit: Boolean = false,
    private val showResponsible: Boolean = true
) : RecyclerView.Adapter<TaskAdapter.Holder>() {

    /** ViewHolder that binds task data to the layout views. */
    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.text_task_title)
        val description: TextView = v.findViewById(R.id.text_task_description)
        val status: TextView = v.findViewById(R.id.text_task_status)
        val dueDate: TextView = v.findViewById(R.id.text_task_due_date)
        val responsible: TextView = v.findViewById(R.id.text_task_responsavel)
        val responsibleLayout: View = v.findViewById(R.id.layout_responsible)
        val btnDelete: Button = v.findViewById(R.id.button_delete_task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false))

    override fun onBindViewHolder(h: Holder, pos: Int) {
        val twu = data[pos]
        val task = twu.task

        h.title.text = task.title
        h.description.text = task.description
        h.status.text = task.state
        h.dueDate.text = task.conclusionDate ?: ""
        h.responsible.text = "Responsible: ${twu.responsavel}"

        // Handle click on item
        h.itemView.setOnClickListener { onClick(task) }

        // Handle delete button visibility and action
        if (canEdit && onDelete != null) {
            h.btnDelete.visibility = View.VISIBLE
            h.btnDelete.setOnClickListener { onDelete.invoke(task) }
        } else {
            h.btnDelete.visibility = View.GONE
            h.btnDelete.setOnClickListener(null)
        }

        // Show/hide responsible section
        h.responsibleLayout.visibility =
            if (showResponsible) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = data.size

    /**
     * Updates the task list and refreshes the adapter.
     *
     * @param newData The new task list to display.
     */
    fun updateData(newData: List<TaskWithUser>) {
        data = newData
        notifyDataSetChanged()
    }
}

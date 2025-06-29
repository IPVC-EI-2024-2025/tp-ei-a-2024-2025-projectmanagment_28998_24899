import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Task
import com.baptistaz.taskwave.data.model.TaskWithUser

class TaskAdapter(
    private var tasksWithUsers: List<TaskWithUser>,
    private val onClick: (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.text_task_title)
        val description: TextView = view.findViewById(R.id.text_task_description)
        val status: TextView = view.findViewById(R.id.text_task_status)
        val dueDate: TextView = view.findViewById(R.id.text_task_due_date)
        val responsible: TextView = view.findViewById(R.id.text_task_responsavel)
        val buttonDelete: Button = view.findViewById(R.id.button_delete_task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val item = tasksWithUsers[position]
        val task = item.task
        holder.title.text = task.title
        holder.description.text = task.description
        holder.status.text = task.state
        holder.dueDate.text = task.conclusionDate ?: ""
        holder.responsible.text = "Responsible: ${item.responsavel ?: "N/A"}"

        holder.itemView.setOnClickListener { onClick(task) }
        holder.buttonDelete.setOnClickListener { onDelete(task) }
    }

    override fun getItemCount(): Int = tasksWithUsers.size

    fun updateData(newTasksWithUsers: List<TaskWithUser>) {
        tasksWithUsers = newTasksWithUsers
        notifyDataSetChanged()
    }
}

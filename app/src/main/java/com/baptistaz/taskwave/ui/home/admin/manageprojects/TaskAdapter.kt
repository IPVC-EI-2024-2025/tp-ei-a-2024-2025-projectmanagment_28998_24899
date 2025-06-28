import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Task

class TaskAdapter(private var tasks: List<Task>, private val onClick: (Task) -> Unit, private val onDelete: (Task) -> Unit) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.text_task_title)
        val description: TextView = view.findViewById(R.id.text_task_description)
        val status: TextView = view.findViewById(R.id.text_task_status)
        val dueDate: TextView = view.findViewById(R.id.text_task_due_date)
        val buttonDelete: Button = view.findViewById(R.id.button_delete_task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.title.text = task.title
        holder.description.text = task.description
        holder.status.text = task.state
        holder.dueDate.text = task.conclusion_date ?: ""

        holder.itemView.setOnClickListener { onClick(task) }
        holder.buttonDelete.setOnClickListener { onDelete(task) }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateData(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}

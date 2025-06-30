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
    private var data: List<TaskWithUser>,
    private val onClick: (Task) -> Unit,
    private val onDelete: ((Task) -> Unit)? = null,
    private val canEdit: Boolean = false
) : RecyclerView.Adapter<TaskAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val title       : TextView = v.findViewById(R.id.text_task_title)
        val description : TextView = v.findViewById(R.id.text_task_description)
        val status      : TextView = v.findViewById(R.id.text_task_status)
        val dueDate     : TextView = v.findViewById(R.id.text_task_due_date)
        val responsible : TextView = v.findViewById(R.id.text_task_responsavel)
        val btnDelete   : Button = v.findViewById(R.id.button_delete_task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false))

    override fun onBindViewHolder(h: Holder, pos: Int) {
        val twu  = data[pos]
        val task = twu.task

        h.title.text       = task.title
        h.description.text = task.description
        h.status.text      = task.state
        h.dueDate.text     = task.conclusionDate ?: ""
        h.responsible.text = "Responsible: ${twu.responsavel}"

        h.itemView.setOnClickListener { onClick(task) }

        if (canEdit && onDelete != null) {
            h.btnDelete.visibility = View.VISIBLE
            h.btnDelete.setOnClickListener { onDelete?.invoke(task) }
        } else {
            h.btnDelete.visibility = View.GONE
            h.btnDelete.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: List<TaskWithUser>) {
        data = newData
        notifyDataSetChanged()
    }
}

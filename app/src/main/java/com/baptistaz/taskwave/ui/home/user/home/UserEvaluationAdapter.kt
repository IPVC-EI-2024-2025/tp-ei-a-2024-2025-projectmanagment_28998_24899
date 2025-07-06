package com.baptistaz.taskwave.ui.home.user.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.auth.Evaluation

/**
 * Adapter for displaying the evaluations a user has received.
 *
 * @param items List of evaluations to show (score, project name, comment).
 */
class UserEvaluationAdapter(
    private val items: List<Evaluation>
) : RecyclerView.Adapter<UserEvaluationAdapter.VH>() {

    /** ViewHolder for a single evaluation item. */
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvProj: TextView = view.findViewById(R.id.text_project_name)
        val rbScore: RatingBar = view.findViewById(R.id.rating_bar)
        val tvComm: TextView = view.findViewById(R.id.text_comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_evaluation, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ev = items[position]

        // Show project name
        holder.tvProj.text = ev.projectName

        // Show rating and comment
        holder.rbScore.rating = ev.score.toFloat()
        holder.tvComm.text = ev.comment.orEmpty()
    }
}

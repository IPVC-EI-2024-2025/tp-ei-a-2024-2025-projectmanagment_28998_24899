package com.baptistaz.taskwave.ui.home.manager.evaluations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.auth.Evaluation

/**
 * Adapter for displaying a list of evaluations in the manager UI.
 *
 * @param evaluations List of evaluation objects to display.
 */
class EvaluationAdapter(
    private val evaluations: List<Evaluation>
) : RecyclerView.Adapter<EvaluationAdapter.EvaluationViewHolder>() {

    /** ViewHolder for a single evaluation item. */
    inner class EvaluationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tv_user_name)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val tvComment: TextView = view.findViewById(R.id.tv_comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvaluationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evaluation, parent, false)
        return EvaluationViewHolder(view)
    }

    override fun onBindViewHolder(holder: EvaluationViewHolder, position: Int) {
        val evaluation = evaluations[position]

        // In practice, id_user is already replaced with the user's name externally
        holder.tvUserName.text = evaluation.id_user
        holder.ratingBar.rating = evaluation.score.toFloat()
        holder.tvComment.text = evaluation.comment ?: ""
    }

    override fun getItemCount(): Int = evaluations.size
}

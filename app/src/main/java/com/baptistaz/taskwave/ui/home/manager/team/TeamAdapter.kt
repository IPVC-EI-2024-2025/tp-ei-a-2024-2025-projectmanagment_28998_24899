package com.baptistaz.taskwave.ui.home.manager.team

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.ui.home.manager.evaluations.TeamMember

/**
 * Adapter for evaluating team members with a score and optional comment.
 *
 * @param members List of team members to be evaluated.
 */
class TeamAdapter(
    private val members: List<TeamMember>
) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    // Maps user ID to rating value
    private val ratings = mutableMapOf<String, Int>()

    // Maps user ID to comment text
    private val comments = mutableMapOf<String, String>()

    /** ViewHolder for each team member entry. */
    inner class TeamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.txt_member_name)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val etComment: EditText = view.findViewById(R.id.et_comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team_member_evaluation, parent, false)
        return TeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val member = members[position]

        holder.txtName.text = member.name
        holder.ratingBar.rating = ratings[member.id]?.toFloat() ?: member.rating.toFloat()
        holder.etComment.setText(comments[member.id] ?: "")

        // Save rating value to internal map and update model
        holder.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            ratings[member.id] = rating.toInt()
            member.rating = rating.toInt()
        }

        // Save comment text to internal map
        holder.etComment.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                comments[member.id] = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun getItemCount(): Int = members.size

    /** Returns a map of user IDs to their selected rating values. */
    fun getRatings(): Map<String, Int> = ratings

    /** Returns a map of user IDs to their written comments. */
    fun getComments(): Map<String, String> = comments
}

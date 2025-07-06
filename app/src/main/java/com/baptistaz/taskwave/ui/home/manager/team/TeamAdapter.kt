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

class TeamAdapter(
    private val members: List<TeamMember>
) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    // Mapa userId -> rating
    private val ratings = mutableMapOf<String, Int>()

    // Mapa userId -> comment
    private val comments = mutableMapOf<String, String>()

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

        holder.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            ratings[member.id] = rating.toInt()
            member.rating = rating.toInt()
        }

        holder.etComment.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                comments[member.id] = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun getItemCount(): Int = members.size

    fun getRatings(): Map<String, Int> = ratings

    fun getComments(): Map<String, String> = comments
}

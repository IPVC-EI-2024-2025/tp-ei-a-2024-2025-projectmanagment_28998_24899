package com.baptistaz.taskwave.ui.home.manager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R

class TeamAdapter(
    private val members: List<TeamMember>
) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    // Mapa userId -> rating
    private val ratings = mutableMapOf<String, Int>()

    inner class TeamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.txt_member_name)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team_member_evaluation, parent, false)
        return TeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val member = members[position]
        holder.txtName.text = member.name
        holder.ratingBar.rating = member.rating.toFloat()
        holder.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            ratings[member.id] = rating.toInt()
            member.rating = rating.toInt()
        }
    }

    override fun getItemCount(): Int = members.size

    fun getRatings(): Map<String, Int> =
        members.associate { it.id to it.rating }
}


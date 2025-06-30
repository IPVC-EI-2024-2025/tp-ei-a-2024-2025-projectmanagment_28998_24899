package com.baptistaz.taskwave.ui.home.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.TaskUpdate

class UpdateAdapter(
    private var list: List<TaskUpdate>,
    private val onLongClick: (TaskUpdate) -> Unit
) : RecyclerView.Adapter<UpdateAdapter.UpdVH>() {

    inner class UpdVH(v: View) : RecyclerView.ViewHolder(v) {
        val title  : TextView = v.findViewById(R.id.text_title)
        val notes  : TextView = v.findViewById(R.id.text_notes)
        val meta   : TextView = v.findViewById(R.id.text_meta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpdVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_update, parent, false)
        return UpdVH(view)
    }

    override fun onBindViewHolder(holder: UpdVH, position: Int) {
        val u = list[position]

        holder.title.text = u.title
        holder.notes.text = u.notes ?: ""

        // Meta = "2025-06-30 • Porto • 2 h"
        val datePart = u.date?.take(10) ?: ""
        val locPart  = u.location ?: ""
        val timePart = u.timeSpent ?: ""
        holder.meta.text = listOf(datePart, locPart, timePart)
            .filter { it.isNotBlank() }
            .joinToString(" • ")

        holder.itemView.setOnLongClickListener {
            onLongClick(u)
            true
        }
    }

    override fun getItemCount(): Int = list.size

    fun update(newList: List<TaskUpdate>) {
        list = newList
        notifyDataSetChanged()
    }
}

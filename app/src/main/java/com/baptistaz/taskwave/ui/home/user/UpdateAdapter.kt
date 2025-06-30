package com.baptistaz.taskwave.ui.home.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.TaskUpdate

private const val TYPE_ITEM   = 0
private const val TYPE_FOOTER = 1

class UpdateAdapter(
    private val data            : MutableList<TaskUpdate>,
    private val onFooterClick   : () -> Unit,
    private val onItemClick     : (TaskUpdate) -> Unit,
    private val onItemLongClick : (TaskUpdate) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /* ───────── ITEM ───────── */
    inner class ItemVH(v: View) : RecyclerView.ViewHolder(v) {
        private val title  = v.findViewById<TextView>(R.id.text_title)
        private val notes  = v.findViewById<TextView>(R.id.text_notes)
        private val meta   = v.findViewById<TextView>(R.id.text_meta)
        private val top    = v.findViewById<View>(R.id.line_top)
        private val bottom = v.findViewById<View>(R.id.line_bottom)

        fun bind(pos: Int) {
            val u = data[pos]

            title.text = u.title
            notes.text = u.notes ?: ""

            meta.text = listOf(
                u.date?.take(10) ?: "",
                u.location ?: "",
                u.timeSpent ?: ""
            ).filter { it.isNotBlank() }.joinToString(" • ")

            top.visibility    = if (pos == 0)              View.INVISIBLE else View.VISIBLE
            bottom.visibility = if (pos == data.lastIndex) View.INVISIBLE else View.VISIBLE

            itemView.setOnClickListener  { onItemClick(u) }
            itemView.setOnLongClickListener { onItemLongClick(u); true }
        }
    }

    /* ───────── FOOTER (+) ───────── */
    inner class FooterVH(v: View) : RecyclerView.ViewHolder(v) {
        init { v.setOnClickListener { onFooterClick() } }
    }

    /* ───────── Adapter ───────── */
    override fun getItemViewType(position: Int) =
        if (position == data.size) TYPE_FOOTER else TYPE_ITEM

    override fun onCreateViewHolder(p: ViewGroup, type: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(p.context)
        return if (type == TYPE_ITEM)
            ItemVH(inf.inflate(R.layout.item_update_timeline, p, false))
        else
            FooterVH(inf.inflate(R.layout.item_update_footer, p, false))
    }

    override fun getItemCount() = data.size + 1
    override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
        if (h is ItemVH) h.bind(pos)
    }

    fun setData(newList: List<TaskUpdate>) {
        data.clear()
        data.addAll(newList)
        notifyDataSetChanged()
    }
}


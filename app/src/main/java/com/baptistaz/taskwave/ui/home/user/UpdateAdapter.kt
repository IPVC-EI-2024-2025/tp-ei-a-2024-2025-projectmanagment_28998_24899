package com.baptistaz.taskwave.ui.home.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.task.TaskUpdate

private const val TYPE_ITEM = 0
private const val TYPE_FOOTER = 1

/**
 * Adapter for displaying task updates in a vertical timeline with optional footer.
 *
 * @param data List of TaskUpdate objects.
 * @param onFooterClick Called when + is clicked.
 * @param onItemClick Called when an item is clicked.
 * @param onItemLongClick Called when an item is long-clicked.
 * @param showFooter Whether to show the footer (default: true).
 */
class UpdateAdapter(
    private val data: MutableList<TaskUpdate>,
    private val onFooterClick: () -> Unit,
    private val onItemClick: (TaskUpdate) -> Unit,
    private val onItemLongClick: (TaskUpdate) -> Unit = {},
    private val showFooter: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /** ViewHolder for regular update items. */
    inner class ItemVH(v: View) : RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.text_title)
        private val notes: TextView = v.findViewById(R.id.text_notes)
        private val meta: TextView = v.findViewById(R.id.text_meta)
        private val top: View = v.findViewById(R.id.line_top)
        private val bottom: View = v.findViewById(R.id.line_bottom)

        fun bind(pos: Int) {
            val u = data[pos]
            title.text = u.title
            notes.text = u.notes ?: ""

            // Compose meta string: date • location • timeSpent
            meta.text = listOf(
                u.date?.take(10) ?: "",
                u.location ?: "",
                u.timeSpent ?: ""
            ).filter { it.isNotBlank() }.joinToString(" • ")

            top.visibility = if (pos == 0) View.INVISIBLE else View.VISIBLE
            bottom.visibility = if (pos == data.lastIndex) View.INVISIBLE else View.VISIBLE

            itemView.setOnClickListener { onItemClick(u) }
            itemView.setOnLongClickListener { onItemLongClick(u); true }
        }
    }

    /** ViewHolder for the footer (+ button to add new update). */
    inner class FooterVH(v: View) : RecyclerView.ViewHolder(v) {
        init {
            v.setOnClickListener { onFooterClick() }
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (showFooter && position == data.size) TYPE_FOOTER else TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ITEM)
            ItemVH(inf.inflate(R.layout.item_update_timeline, parent, false))
        else
            FooterVH(inf.inflate(R.layout.item_update_footer, parent, false))
    }

    override fun getItemCount(): Int = data.size + if (showFooter) 1 else 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemVH) holder.bind(position)
        // No binding needed for FooterVH
    }

    /**
     * Replace data list and refresh adapter.
     */
    fun setData(newList: List<TaskUpdate>) {
        data.clear()
        data.addAll(newList)
        notifyDataSetChanged()
    }
}

package com.baptistaz.taskwave.ui.home.user.updates.details

import android.os.Bundle
import android.widget.TextView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.task.TaskUpdate
import com.baptistaz.taskwave.utils.BaseLocalizedActivity

class UpdateDetailsReadonlyActivity : BaseLocalizedActivity() {

    private lateinit var upd: TaskUpdate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_details) // usa o mesmo layout

        setSupportActionBar(findViewById(R.id.toolbar_update_detail))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.update_details)

        upd = intent.getSerializableExtra("UPDATE") as TaskUpdate

        bind(upd)

        // Esconde os botões de editar e eliminar
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_edit).visibility = android.view.View.GONE
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_delete).visibility = android.view.View.GONE
    }

    private fun bind(u: TaskUpdate) = with(u) {
        findViewById<TextView>(R.id.text_title).text = title
        findViewById<TextView>(R.id.text_date).text = date?.take(10) ?: ""
        findViewById<TextView>(R.id.text_notes).text = notes ?: "-"
        findViewById<TextView>(R.id.text_loc).text = location ?: "-"
        findViewById<TextView>(R.id.text_time).text = timeSpent ?: "-"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

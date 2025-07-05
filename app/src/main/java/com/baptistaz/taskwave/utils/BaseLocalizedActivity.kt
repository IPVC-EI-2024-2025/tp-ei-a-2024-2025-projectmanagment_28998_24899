package com.baptistaz.taskwave.utils

import android.content.Context
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.baptistaz.taskwave.R

open class BaseLocalizedActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val lang = SessionManager.getLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Tenta encontrar um Spinner comum (se existir nesta Activity)
        findViewById<Spinner?>(R.id.spinner_language)?.let { spinner ->
            val languages = listOf("PT", "EN")
            val codes = listOf("pt", "en")
            val currentLang = SessionManager.getLanguage(this)

            spinner.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                languages
            )

            spinner.setSelection(codes.indexOf(currentLang))

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    val selectedLang = codes[position]
                    if (selectedLang != currentLang) {
                        SessionManager.setLanguage(this@BaseLocalizedActivity, selectedLang)
                        recreate()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    // ✅ Método extra para ser usado nas Settings (sem mexer no spinner)
    fun showLanguageDialog() {
        val languages = arrayOf("Português", "English")
        val codes = arrayOf("pt", "en")
        val current = SessionManager.getLanguage(this)
        val selected = codes.indexOf(current)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.change_language))
            .setSingleChoiceItems(languages, selected) { dialog, which ->
                val newLang = codes[which]
                if (newLang != current) {
                    SessionManager.setLanguage(this, newLang)
                    recreate()
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }
}

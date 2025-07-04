package com.baptistaz.taskwave.utils

import android.content.Context
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.baptistaz.taskwave.R

open class BaseLocalizedActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val lang = newBase.getSharedPreferences("prefs", MODE_PRIVATE)
            .getString("language", "pt") ?: "pt"
        val context = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Tenta encontrar um Spinner comum (se existir nesta Activity)
        findViewById<Spinner?>(R.id.spinner_language)?.let { spinner ->
            val languages = listOf("PT", "EN")
            val codes = listOf("pt", "en")
            val currentLang = getSharedPreferences("prefs", MODE_PRIVATE)
                .getString("language", "pt")

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
                        getSharedPreferences("prefs", MODE_PRIVATE)
                            .edit()
                            .putString("language", selectedLang)
                            .apply()
                        recreate()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }
}

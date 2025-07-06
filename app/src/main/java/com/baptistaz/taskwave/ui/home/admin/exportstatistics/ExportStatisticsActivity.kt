package com.baptistaz.taskwave.ui.home.admin.exportstatistics

import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter

class ExportStatisticsActivity : BaseLocalizedActivity() {

    private lateinit var cbProjects: CheckBox
    private lateinit var cbUsers: CheckBox
    private lateinit var cbTasks: CheckBox
    private lateinit var btnExportCSV: android.widget.Button
    private lateinit var btnExportPDF: android.widget.Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_statistics)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_export)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.export_statistics_title)

        cbProjects = findViewById(R.id.checkbox_projects)
        cbUsers = findViewById(R.id.checkbox_users)
        cbTasks = findViewById(R.id.checkbox_tasks)
        btnExportCSV = findViewById(R.id.button_export_csv)
        btnExportPDF = findViewById(R.id.button_export_pdf)

        btnExportCSV.setOnClickListener {
            Toast.makeText(this, getString(R.string.exporting_csv), Toast.LENGTH_SHORT).show()
            exportCSV()
        }

        btnExportPDF.setOnClickListener {
            Toast.makeText(this, getString(R.string.exporting_pdf), Toast.LENGTH_SHORT).show()
            CoroutineScope(Dispatchers.IO).launch {
                exportPDF()
            }
        }
    }

    private fun exportCSV() {
        val token = SessionManager.getAccessToken(this)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_session_expired), Toast.LENGTH_SHORT).show()
            return
        }
        if (!cbProjects.isChecked && !cbUsers.isChecked && !cbTasks.isChecked) {
            Toast.makeText(this, getString(R.string.select_at_least_one), Toast.LENGTH_SHORT).show()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (cbProjects.isChecked) {
                    exportFileToDownloads("projects.csv", "ID,Name,Description,Status,Start,End\n") { writer ->
                        val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
                        val projects = projectRepo.getAllProjects()
                        projects.forEach {
                            writer.append("${it.idProject},${it.name},${it.description},${it.status},${it.startDate},${it.endDate}\n")
                        }
                    }
                }
                if (cbUsers.isChecked) {
                    exportFileToDownloads("users.csv", "ID,Name,Email,Username,Phone,Profile\n") { writer ->
                        val userRepo = UserRepository()
                        val users = userRepo.getAllUsers(token)
                        users?.forEach {
                            writer.append("${it.id_user},${it.name},${it.email},${it.username},${it.phoneNumber},${it.profileType}\n")
                        }
                    }
                }
                if (cbTasks.isChecked) {
                    exportFileToDownloads("tasks.csv", "ID,Name,Description,State,Deadline\n") { writer ->
                        val taskRepo = TaskRepository(RetrofitInstance.getTaskService(token))
                        val tasks = taskRepo.getAllTasks()
                        tasks.forEach { task ->
                            writer.append("${task.idTask},${task.title},${task.description},${task.state},${task.conclusionDate}\n")
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExportStatisticsActivity, getString(R.string.csv_export_success), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("EXPORT_CSV", "Erro durante a exportação: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExportStatisticsActivity, "${getString(R.string.error)}: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun exportFileToDownloads(
        filename: String,
        header: String,
        content: suspend (writer: OutputStreamWriter) -> Unit
    ) {
        val resolver = contentResolver
        val contentValues = android.content.ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }
        val uri = resolver.insert(collection, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                val writer = OutputStreamWriter(outputStream)
                writer.write(header)
                withContext(Dispatchers.IO) {
                    content(writer)
                }
                writer.flush()
                writer.close()
            }
        } ?: throw Exception(getString(R.string.error_creating_csv))
    }

    private suspend fun exportPDF() {
        val token = SessionManager.getAccessToken(this)
        if (token.isNullOrEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ExportStatisticsActivity, getString(R.string.error_session_expired), Toast.LENGTH_SHORT).show()
            }
            return
        }
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            paint.textSize = 12f
            var y = 40f

            fun drawLine(text: String) {
                canvas.drawText(text, 20f, y, paint)
                y += 20f
            }

            drawLine("Relatório de Estatísticas - Project Management")
            y += 20f

            val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
            val userRepo = UserRepository()
            val taskRepo = TaskRepository(RetrofitInstance.getTaskService(token))

            drawLine("Projetos:")
            val projects = projectRepo.getAllProjects()
            projects.forEach {
                drawLine("- ${it.idProject}: ${it.name} | ${it.description} | ${it.status} | ${it.startDate} - ${it.endDate}")
            }
            y += 10f

            drawLine("Utilizadores:")
            val users = userRepo.getAllUsers(token)
            users?.forEach {
                drawLine("- ${it.id_user}: ${it.name} | ${it.email} | ${it.username} | ${it.phoneNumber} | ${it.profileType}")
            }
            y += 10f

            drawLine("Tarefas:")
            val tasks = taskRepo.getAllTasks()
            tasks.forEach {
                drawLine("- ${it.idTask}: ${it.title} | ${it.description} | ${it.state} | ${it.conclusionDate}")
            }
            y += 10f

            pdfDocument.finishPage(page)

            val resolver = contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "estatisticas.pdf")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Files.getContentUri("external")
            }
            val uri = resolver.insert(collection, contentValues)
                ?: throw Exception(getString(R.string.error_creating_pdf))
            resolver.openOutputStream(uri).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ExportStatisticsActivity, getString(R.string.pdf_export_success), Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ExportStatisticsActivity, "${getString(R.string.error)}: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            exportCSV()
        } else {
            Toast.makeText(this, getString(R.string.permission_denied_export), Toast.LENGTH_SHORT).show()
        }
    }
}

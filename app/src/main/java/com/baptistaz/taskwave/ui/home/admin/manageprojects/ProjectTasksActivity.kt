package com.baptistaz.taskwave.ui.home.admin.manageprojects

import TaskAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog

class ProjectTasksActivity : AppCompatActivity() {
    private lateinit var adapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonCreateTask: Button
    private lateinit var repository: TaskRepository
    private lateinit var projectId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_tasks)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tarefas do Projeto"

        projectId = intent.getStringExtra("project_id") ?: return
        repository = TaskRepository(RetrofitInstance.taskService)

        recyclerView = findViewById(R.id.recycler_tasks)
        buttonCreateTask = findViewById(R.id.button_create_task)

        adapter = TaskAdapter(
            emptyList(),
            onClick = { selectedTask ->
                val intent = Intent(this, TaskDetailActivity::class.java)
                intent.putExtra("task", selectedTask)
                startActivity(intent)
            },
            onDelete = { selectedTask ->
                AlertDialog.Builder(this)
                    .setTitle("Eliminar Tarefa")
                    .setMessage("Tens a certeza que queres eliminar '${selectedTask.title}'?")
                    .setPositiveButton("Sim") { _, _ ->
                        lifecycleScope.launch {
                            try {
                                repository.deleteTask(selectedTask.id_task)
                                Toast.makeText(this@ProjectTasksActivity, "Tarefa eliminada!", Toast.LENGTH_SHORT).show()
                                // Atualiza a lista
                                val tasks = repository.getTasksByProject("eq.$projectId")
                                adapter.updateData(tasks)
                            } catch (e: Exception) {
                                Toast.makeText(this@ProjectTasksActivity, "Erro ao eliminar: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Carregar tarefas do projeto
        lifecycleScope.launch {
            try {
                val tasks = repository.getTasksByProject("eq.$projectId")
                adapter.updateData(tasks)
            } catch (e: Exception) {
                Toast.makeText(this@ProjectTasksActivity, "Erro ao carregar tarefas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        buttonCreateTask.setOnClickListener {
            val intent = Intent(this, CreateTaskActivity::class.java)
            intent.putExtra("project_id", projectId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Atualiza a lista de tarefas
        lifecycleScope.launch {
            try {
                val tasks = repository.getTasksByProject("eq.$projectId")
                adapter.updateData(tasks)
            } catch (e: Exception) {
                Toast.makeText(this@ProjectTasksActivity, "Erro ao atualizar tarefas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}

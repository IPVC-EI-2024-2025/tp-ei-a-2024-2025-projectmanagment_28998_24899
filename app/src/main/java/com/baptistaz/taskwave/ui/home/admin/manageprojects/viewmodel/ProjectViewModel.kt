package com.baptistaz.taskwave.ui.home.admin.manageprojects.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baptistaz.taskwave.data.model.project.Project
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProjectViewModel(private val repository: ProjectRepository) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadProjects() {
        viewModelScope.launch {
            try {
                val result = repository.getAllProjects()
                _projects.value = result
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    suspend fun deleteProject(projectId: String) {
        repository.deleteProject(projectId)
    }

    fun getTotalCount(): Int = _projects.value.size

    fun getActiveCount(): Int = _projects.value.count { it.status.equals("active", true) }

    fun getCompletedCount(): Int = _projects.value.count { it.status.equals("completed", true) }

}

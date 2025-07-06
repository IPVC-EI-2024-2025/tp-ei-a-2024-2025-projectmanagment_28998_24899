package com.baptistaz.taskwave.ui.home.admin.manageprojects.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baptistaz.taskwave.data.model.project.Project
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Responsible for loading and managing project data.
 * Used by the Admin to list, filter, and delete projects.
 *
 * @param repository Repository to fetch and modify project data.
 */
class ProjectViewModel(private val repository: ProjectRepository) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Loads all projects and updates the state.
     */
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

    /**
     * Deletes a project by its ID.
     *
     * @param projectId UUID of the project to delete.
     */
    suspend fun deleteProject(projectId: String) {
        repository.deleteProject(projectId)
    }

    /**
     * Total number of projects loaded.
     */
    fun getTotalCount(): Int = _projects.value.size

    /**
     * Count of active projects.
     */
    fun getActiveCount(): Int = _projects.value.count { it.status.equals("active", true) }

    /**
     * Count of completed projects.
     */
    fun getCompletedCount(): Int = _projects.value.count { it.status.equals("completed", true) }
}

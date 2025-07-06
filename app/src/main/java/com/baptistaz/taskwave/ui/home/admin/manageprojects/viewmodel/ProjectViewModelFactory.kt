package com.baptistaz.taskwave.ui.home.admin.manageprojects.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository

/**
 * Creates ProjectViewModel instances with a ProjectRepository dependency.
 *
 * @param repository Repository used to fetch and manage project data.
 */
class ProjectViewModelFactory(
    private val repository: ProjectRepository
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of ProjectViewModel if requested.
     *
     * @param modelClass Class of the ViewModel requested.
     * @return An instance of ProjectViewModel.
     * @throws IllegalArgumentException if the model class does not match.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectViewModel::class.java)) {
            return ProjectViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

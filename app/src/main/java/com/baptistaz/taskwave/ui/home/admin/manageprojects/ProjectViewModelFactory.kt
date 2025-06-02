package com.baptistaz.taskwave.ui.home.admin.manageprojects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baptistaz.taskwave.data.remote.project.ProjectRepository

class ProjectViewModelFactory(
    private val repository: ProjectRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectViewModel::class.java)) {
            return ProjectViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

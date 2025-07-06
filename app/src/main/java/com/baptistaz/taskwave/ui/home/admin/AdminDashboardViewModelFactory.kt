package com.baptistaz.taskwave.ui.home.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.repository.TaskRepository
import com.baptistaz.taskwave.data.remote.user.UserRepository

class AdminDashboardViewModelFactory(private val token: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
        val userRepo = UserRepository()
        val taskRepo = TaskRepository(RetrofitInstance.getTaskService(token))
        return AdminDashboardViewModel(projectRepo, userRepo, taskRepo) as T
    }
}

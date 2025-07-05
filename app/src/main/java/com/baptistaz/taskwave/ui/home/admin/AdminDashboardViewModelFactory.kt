package com.baptistaz.taskwave.ui.home.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.TaskRepository

class AdminDashboardViewModelFactory(private val token: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
        val userRepo = UserRepository()
        val taskRepo = TaskRepository(RetrofitInstance.getTaskService(token))
        return AdminDashboardViewModel(projectRepo, userRepo, taskRepo) as T
    }
}

package com.baptistaz.taskwave.ui.home.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.repository.TaskRepository
import com.baptistaz.taskwave.data.remote.user.UserRepository

/**
 * Creates AdminDashboardViewModel instances with the required repositories
 *
 * @param token JWT token used to instantiate Retrofit services.
 */
class AdminDashboardViewModelFactory(private val token: String) : ViewModelProvider.Factory {

    /**
     * Creates the AdminDashboardViewModel with injected dependencies.
     *
     * @param modelClass The ViewModel class requested.
     * @return A fully constructed AdminDashboardViewModel.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
        val userRepo = UserRepository()
        val taskRepo = TaskRepository(RetrofitInstance.getTaskService(token))
        return AdminDashboardViewModel(projectRepo, userRepo, taskRepo) as T
    }
}

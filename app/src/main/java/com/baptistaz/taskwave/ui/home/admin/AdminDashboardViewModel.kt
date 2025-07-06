package com.baptistaz.taskwave.ui.home.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.repository.TaskRepository
import com.baptistaz.taskwave.data.remote.user.UserRepository
import kotlinx.coroutines.launch

class AdminDashboardViewModel(
    private val projectRepo: ProjectRepository,
    private val userRepo: UserRepository,
    private val taskRepo: TaskRepository
) : ViewModel() {

    private val _projectCount = MutableLiveData<Int>()
    val projectCount: LiveData<Int> = _projectCount

    private val _userCount = MutableLiveData<Int>()
    val userCount: LiveData<Int> = _userCount

    private val _taskCount = MutableLiveData<Int>()
    val taskCount: LiveData<Int> = _taskCount

    fun loadDashboardData(token: String) {
        viewModelScope.launch {
            try {
                _projectCount.value = projectRepo.getAllProjects()?.size ?: 0
                _userCount.value = userRepo.getAllUsers(token)?.size ?: 0
                _taskCount.value = taskRepo.getAllTasks().size
            } catch (e: Exception) {
                _projectCount.value = 0
                _userCount.value = 0
                _taskCount.value = 0
            }
        }
    }
}

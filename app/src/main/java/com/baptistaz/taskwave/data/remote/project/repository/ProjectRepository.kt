package com.baptistaz.taskwave.data.remote.project.repository

import com.baptistaz.taskwave.data.model.project.Project
import com.baptistaz.taskwave.data.model.project.ProjectUpdate
import com.baptistaz.taskwave.data.remote.project.service.ProjectService

/**
 * Manages project-related operations.
 * API operations to ProjectService.
 */
class ProjectRepository(private val service: ProjectService) {

    /**
     * All projects.
     *
     * @return List of all projects.
     * @throws Exception if the request fails.
     */
    suspend fun getAllProjects(): List<Project> {
        val response = service.getAllProjects()
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to fetch projects: ${response.code()}")
        }
    }

    /**
     * Creates a new project.
     *
     * @param project Project object to be created.
     * @return The created project.
     * @throws Exception if the request fails or response is empty.
     */
    suspend fun createProject(project: Project): Project {
        val response = service.createProject(project)
        if (response.isSuccessful) {
            return response.body()?.firstOrNull()
                ?: throw Exception("Empty response while creating project")
        } else {
            throw Exception("Failed to create project: ${response.code()}")
        }
    }

    /**
     * Updates a project by ID.
     *
     * @param id Project ID.
     * @param updatedProject Updated fields.
     * @return The updated project.
     * @throws Exception if the update fails or response is empty.
     */
    suspend fun updateProject(id: String, updatedProject: ProjectUpdate): Project {
        val url = "project?id_project=eq.$id"
        val response = service.updateProject(url, updatedProject)
        if (response.isSuccessful) {
            return response.body()?.firstOrNull()
                ?: throw Exception("Empty response while updating project")
        } else {
            val errorBody = response.errorBody()?.string()
            throw Exception("Failed to update project: ${response.code()} - $errorBody")
        }
    }

    /**
     * Deletes a project by ID.
     *
     * @param id Project ID.
     * @throws Exception if the deletion fails.
     */
    suspend fun deleteProject(id: String) {
        val url = "project?id_project=eq.$id"
        val response = service.deleteProject(url)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete project: ${response.code()}")
        }
    }

    /**
     * All projects managed by a specific user.
     *
     * @param managerId ID of the manager.
     * @return List of projects assigned to that manager.
     * @throws Exception if the request fails.
     */
    suspend fun getProjectsByManager(managerId: String): List<Project> {
        val response = service.getProjectsByManager("eq.$managerId")
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            val error = response.errorBody()?.string()
            throw Exception("Failed to fetch manager's projects: ${response.code()} - $error")
        }
    }

    /**
     * Gets a Project by its unique ID.
     *
     * @param id Project ID.
     * @return The project if found, null if not.
     */
    suspend fun getProjectById(id: String): Project? {
        val url = "project?id_project=eq.$id"
        val response = service.getProjectById(url)
        return if (response.isSuccessful) response.body()?.firstOrNull() else null
    }
}

package com.baptistaz.taskwave.data.remote.manager.service

import com.baptistaz.taskwave.data.model.project.ProjectTeamMember
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Access project team members.
 */
interface ProjectTeamService {

    /**
     * List of team members assigned to a specific project.
     *
     * @param projectId ID of the project to filter by.
     * @return List of users assigned to the given project.
     */
    @GET("project_team")
    suspend fun getTeamMembersByProject(
        @Query("id_project") projectId: String
    ): List<ProjectTeamMember>
}

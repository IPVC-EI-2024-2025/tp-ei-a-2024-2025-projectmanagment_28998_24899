package com.baptistaz.taskwave.data.remote.manager.service

import com.baptistaz.taskwave.data.model.project.ProjectTeamMember
import retrofit2.http.GET
import retrofit2.http.Query

interface ProjectTeamService {
    @GET("project_team")
    suspend fun getTeamMembersByProject(
        @Query("id_project") projectId: String
    ): List<ProjectTeamMember>
}
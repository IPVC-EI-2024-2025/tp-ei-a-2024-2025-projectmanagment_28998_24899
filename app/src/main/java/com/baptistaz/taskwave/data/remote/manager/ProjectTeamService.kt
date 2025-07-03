package com.baptistaz.taskwave.data.remote.manager

import com.baptistaz.taskwave.data.model.ProjectTeamMember
import retrofit2.http.GET
import retrofit2.http.Query

interface ProjectTeamService {
    @GET("project_team")
    suspend fun getTeamMembersByProject(
        @Query("id_project") projectId: String
    ): List<ProjectTeamMember>
}
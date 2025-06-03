package com.baptistaz.taskwave.data.remote.project

import com.baptistaz.taskwave.data.model.Project
import com.baptistaz.taskwave.data.model.ProjectUpdate
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Url

interface ProjectService {

    @GET("project")
    suspend fun getAllProjects(): Response<List<Project>>

    @Headers("Prefer: return=representation")
    @POST("project")
    suspend fun createProject(
        @Body project: Project
    ): Response<List<Project>>


    @PATCH
    @Headers("Prefer: return=representation")
    suspend fun updateProject(
        @Url url: String,
        @Body updatedProject: ProjectUpdate
    ): Response<List<Project>>

    @DELETE
    @Headers("Prefer: return=representation")
    suspend fun deleteProject(
        @Url url: String
    ): Response<List<Project>>
}

package com.baptistaz.taskwave.data.remote.project

import com.baptistaz.taskwave.data.model.Project
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface ProjectService {

    @GET("project")
    suspend fun getAllProjects(): Response<List<Project>>

    @Headers("Prefer: return=representation")
    @POST("project")
    suspend fun createProject(
        @Body project: Project
    ): Response<List<Project>>


    @PATCH("project")
    suspend fun updateProject(
        @Query("id_project") id: String,
        @Body updatedProject: Project
    ): Response<Project>

    @DELETE("project")
    suspend fun deleteProject(
        @Query("id_project") id: String
    ): Response<Unit>
}

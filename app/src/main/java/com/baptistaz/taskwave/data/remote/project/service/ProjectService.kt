package com.baptistaz.taskwave.data.remote.project.service

import com.baptistaz.taskwave.data.model.project.Project
import com.baptistaz.taskwave.data.model.project.ProjectUpdate
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Manages projects in the 'project' table.
 */
interface ProjectService {

    /**
     * All projects from the DB.
     */
    @GET("project")
    suspend fun getAllProjects(): Response<List<Project>>

    /**
     * Creates a new project in the DB.
     *
     * 'Prefer' - ensures the created object is returned in the response.
     *
     * @param project Project object to be inserted.
     * @return List containing the inserted project.
     */
    @Headers("Prefer: return=representation")
    @POST("project")
    suspend fun createProject(
        @Body project: Project
    ): Response<List<Project>>

    /**
     * Updates an existing project using a PATCH request.
     *
     * @param url Resource URL ("project?id=eq.<id>").
     * @param updatedProject Object with updated fields.
     * @return List containing the updated project.
     */
    @PATCH
    @Headers("Prefer: return=representation")
    suspend fun updateProject(
        @Url url: String,
        @Body updatedProject: ProjectUpdate
    ): Response<List<Project>>

    /**
     * Deletes a project by its resource URL.
     *
     * @param url Resource URL ("project?id=eq.<id>").
     * @return List containing the deleted project.
     */
    @DELETE
    @Headers("Prefer: return=representation")
    suspend fun deleteProject(
        @Url url: String
    ): Response<List<Project>>

    /**
     * All projects managed by a specific user.
     *
     * @param idManager ID of the manager.
     */
    @GET("project")
    suspend fun getProjectsByManager(
        @Query("id_manager") idManager: String
    ): Response<List<Project>>

    /**
     * Project by its ID using a URL.
     *
     * @param url Resource URL ("project?id=eq.<id>").
     */
    @GET
    suspend fun getProjectById(
        @Url url: String
    ): Response<List<Project>>
}

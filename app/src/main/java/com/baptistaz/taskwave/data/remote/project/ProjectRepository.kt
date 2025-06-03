package com.baptistaz.taskwave.data.remote.project

import android.util.Log
import com.baptistaz.taskwave.data.model.Project
import com.baptistaz.taskwave.data.model.ProjectUpdate

class ProjectRepository(private val service: ProjectService) {

    suspend fun getAllProjects(): List<Project> {
        val response = service.getAllProjects()
        Log.d("GET_PROJECTS", "Status: ${response.code()}, Body: ${response.body()}")
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Erro ao obter projetos: ${response.code()}")
        }
    }

    suspend fun createProject(project: Project): Project {
        val response = service.createProject(project)
        if (response.isSuccessful) {
            return response.body()?.firstOrNull()
                ?: throw Exception("Erro: resposta vazia ao criar projeto")
        } else {
            throw Exception("Erro ao criar projeto: ${response.code()}")
        }

    }

    suspend fun updateProject(id: String, updatedProject: ProjectUpdate): Project {
        val url = "project?id_project=eq.$id"
        val response = service.updateProject(url, updatedProject)
        if (response.isSuccessful) {
            return response.body()?.firstOrNull() ?: throw Exception("Erro: resposta vazia ao atualizar projeto")
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e("UPDATE_ERROR", "Erro: $errorBody")
            throw Exception("Erro ao atualizar projeto: ${response.code()} - $errorBody")
        }
    }

    suspend fun deleteProject(id: String) {
        val url = "project?id_project=eq.$id"
        Log.d("DELETE_PROJECT", "A eliminar projeto com URL: $url")
        val response = service.deleteProject(url)
        Log.d("DELETE_PROJECT", "Código resposta: ${response.code()}")
        Log.d("DELETE_PROJECT", "Corpo resposta: ${response.errorBody()?.string()}")
        if (!response.isSuccessful) {
            throw Exception("Erro ao eliminar projeto: ${response.code()}")
        }
    }
}

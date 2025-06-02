package com.baptistaz.taskwave.data.remote.project

import com.baptistaz.taskwave.data.model.Project

class ProjectRepository(private val service: ProjectService) {

    suspend fun getAllProjects(): List<Project> {
        val response = service.getAllProjects()
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

    suspend fun updateProject(id: String, updatedProject: Project): Project {
        val response = service.updateProject(id, updatedProject)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Erro: resposta vazia ao atualizar projeto")
        } else {
            throw Exception("Erro ao atualizar projeto: ${response.code()}")
        }
    }

    suspend fun deleteProject(id: String) {
        val response = service.deleteProject(id)
        if (!response.isSuccessful) {
            throw Exception("Erro ao eliminar projeto: ${response.code()}")
        }
    }
}

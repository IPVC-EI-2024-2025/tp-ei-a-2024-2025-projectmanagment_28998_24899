package com.baptistaz.taskwave.data.remote

import com.baptistaz.taskwave.data.model.User
import retrofit2.Response

class UserRepository {

    suspend fun getAllUsers(): Response<List<User>> {
        return RetrofitInstance.api.getAllUsers(
            apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndkZWlkdXF3ZmhvZXBiaWFqdHBqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDgzNTc4MzIsImV4cCI6MjA2MzkzMzgzMn0.abxfTIR7QHgLGMODbiEgGwHgzF3aBntc0-Utq-BAAHA",
            auth = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndkZWlkdXF3ZmhvZXBiaWFqdHBqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDgzNTc4MzIsImV4cCI6MjA2MzkzMzgzMn0.abxfTIR7QHgLGMODbiEgGwHgzF3aBntc0-Utq-BAAHA"
        )
    }
}

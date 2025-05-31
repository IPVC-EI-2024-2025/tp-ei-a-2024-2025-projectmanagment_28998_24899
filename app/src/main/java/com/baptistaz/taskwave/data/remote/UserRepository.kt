package com.baptistaz.taskwave.data.remote

import com.baptistaz.taskwave.BuildConfig
import com.baptistaz.taskwave.data.model.User
import retrofit2.Response

class UserRepository {

    suspend fun getAllUsers(): Response<List<User>> {
        return RetrofitInstance.api.getAllUsers(
            apiKey = BuildConfig.SUPABASE_KEY,
            auth = "Bearer ${BuildConfig.SUPABASE_KEY}"
        )
    }
}

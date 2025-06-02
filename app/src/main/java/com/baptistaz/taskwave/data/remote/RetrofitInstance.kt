package com.baptistaz.taskwave.data.remote

import com.baptistaz.taskwave.BuildConfig
import com.baptistaz.taskwave.data.remote.auth.AuthService
import com.baptistaz.taskwave.data.remote.project.ProjectService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_KEY}")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    fun createRestClient(token: String): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    val auth: AuthService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL + "/auth/v1/")
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthService::class.java)
    }

    val projectService: ProjectService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL + "/rest/v1/")
            .client(createRestClient(BuildConfig.SUPABASE_KEY))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProjectService::class.java)
    }

    fun getApiService(token: String): ApiService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL + "/rest/v1/")
            .client(createRestClient(token))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun getProjectService(token: String): ProjectService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL + "/rest/v1/")
            .client(createRestClient(token))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProjectService::class.java)
    }

}
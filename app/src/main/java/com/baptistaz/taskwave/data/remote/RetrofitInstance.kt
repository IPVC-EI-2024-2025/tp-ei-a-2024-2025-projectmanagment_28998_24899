package com.baptistaz.taskwave.data.remote

import com.baptistaz.taskwave.BuildConfig
import com.baptistaz.taskwave.data.remote.auth.AuthService
import com.baptistaz.taskwave.data.remote.project.ProjectService
import com.baptistaz.taskwave.data.remote.project.TaskService
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object RetrofitInstance {

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { src, _, _ ->
            src?.let { com.google.gson.JsonPrimitive(it.format(DateTimeFormatter.ISO_LOCAL_DATE)) }
        })
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ ->
            LocalDate.parse(json.asJsonPrimitive.asString, DateTimeFormatter.ISO_LOCAL_DATE)
        })
        .create()

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
                    .addHeader("Prefer", "return=representation")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    val auth: AuthService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL + "/auth/v1/")
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthService::class.java)
    }

    val projectService: ProjectService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL + "/rest/v1/")
            .client(createRestClient(BuildConfig.SUPABASE_KEY))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ProjectService::class.java)
    }

    // NOVO: Service para tarefas, idêntico ao projectService!
    val taskService: TaskService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL + "/rest/v1/")
            .client(createRestClient(BuildConfig.SUPABASE_KEY))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(TaskService::class.java)
    }

    fun getApiService(token: String): ApiService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL + "/rest/v1/")
            .client(createRestClient(token))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    fun getProjectService(token: String): ProjectService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL + "/rest/v1/")
            .client(createRestClient(token))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ProjectService::class.java)
    }

    // (Se precisares de getTaskService(token: String) para usar um token dinâmico, podes adicionar esta função)
    fun getTaskService(token: String): TaskService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL + "/rest/v1/")
            .client(createRestClient(token))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(TaskService::class.java)
    }
}

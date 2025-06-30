package com.baptistaz.taskwave.data.remote

import com.baptistaz.taskwave.BuildConfig
import com.baptistaz.taskwave.data.remote.auth.AuthService
import com.baptistaz.taskwave.data.remote.project.ProjectService
import com.baptistaz.taskwave.data.remote.project.TaskService
import com.baptistaz.taskwave.data.remote.project.TaskUpdateService
import com.baptistaz.taskwave.data.remote.project.UserTaskService
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

    /* ---------- Gson ---------- */
    private val gson = GsonBuilder()
        .registerTypeAdapter(
            LocalDate::class.java,
            JsonSerializer<LocalDate> { src, _, _ ->
                src?.let { com.google.gson.JsonPrimitive(it.format(DateTimeFormatter.ISO_LOCAL_DATE)) }
            })
        .registerTypeAdapter(
            LocalDate::class.java,
            JsonDeserializer { json, _, _ ->
                LocalDate.parse(json.asJsonPrimitive.asString, DateTimeFormatter.ISO_LOCAL_DATE)
            })
        .create()

    /* ---------- OkHttp ---------- */
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                    .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .build()
            )
        }
        .build()

    /** Cliente com token de sessão (Bearer <token-jwt>) */
    private fun restClient(token: String) = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .build()
            )
        }
        .build()

    /* ---------- Helpers ---------- */
    private fun <T> buildRest(token: String, svc: Class<T>): T =
        Retrofit.Builder()
            .baseUrl("${BuildConfig.SUPABASE_URL}/rest/v1/")
            .client(restClient(token))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(svc)

    private fun <T> buildRestWithServiceKey(svc: Class<T>): T =
        Retrofit.Builder()
            .baseUrl("${BuildConfig.SUPABASE_URL}/rest/v1/")
            .client(restClient(BuildConfig.SUPABASE_KEY))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(svc)

    /* ---------- AUTH ---------- */
    val auth: AuthService by lazy {
        Retrofit.Builder()
            .baseUrl("${BuildConfig.SUPABASE_URL}/auth/v1/")
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthService::class.java)
    }

    /* ---------- SINGLETONS com service-key ---------- */
    val projectService: ProjectService        by lazy { buildRestWithServiceKey(ProjectService::class.java) }
    val taskService: TaskService              by lazy { buildRestWithServiceKey(TaskService::class.java) }
    val userTaskService: UserTaskService      by lazy { buildRestWithServiceKey(UserTaskService::class.java) }
    val taskUpdateService: TaskUpdateService  by lazy { buildRestWithServiceKey(TaskUpdateService::class.java) }

    /* ---------- Dinâmicos (jwt) ---------- */
    fun getApiService(token: String)        = buildRest(token, ApiService::class.java)
    fun getProjectService(token: String)    = buildRest(token, ProjectService::class.java)
    fun getTaskService(token: String)       = buildRest(token, TaskService::class.java)
    fun getUserTaskService(token: String)   = buildRest(token, UserTaskService::class.java)
    fun getTaskUpdateService(token: String) = buildRest(token, TaskUpdateService::class.java)
}

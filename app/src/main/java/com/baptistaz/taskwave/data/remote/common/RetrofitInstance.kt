package com.baptistaz.taskwave.data.remote.common

import com.baptistaz.taskwave.BuildConfig
import com.baptistaz.taskwave.data.remote.auth.AuthService
import com.baptistaz.taskwave.data.remote.manager.service.EvaluationService
import com.baptistaz.taskwave.data.remote.manager.service.ProjectTeamService
import com.baptistaz.taskwave.data.remote.project.service.ProjectService
import com.baptistaz.taskwave.data.remote.project.service.TaskService
import com.baptistaz.taskwave.data.remote.project.service.TaskUpdateService
import com.baptistaz.taskwave.data.remote.project.service.UserTaskService
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Provides configured Retrofit service instances.
 */
object RetrofitInstance {

    /* ---------- Gson config with LocalDate adapters ---------- */
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

    /* ---------- OkHttp logging interceptor ---------- */
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Client used for Supabase Auth API (uses service key only).
     */
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

    /**
     * Builds an OkHttpClient with JWT session token for authenticated requests.
     */
    private fun restClient(token: String): OkHttpClient = OkHttpClient.Builder()
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

    /* ---------- Generic Retrofit builders ---------- */

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

    /* ---------- Auth API (public) ---------- */
    val auth: AuthService by lazy {
        Retrofit.Builder()
            .baseUrl("${BuildConfig.SUPABASE_URL}/auth/v1/")
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthService::class.java)
    }

    /* ---------- REST APIs using service key (admin-level) ---------- */
    val projectService: ProjectService by lazy { buildRestWithServiceKey(ProjectService::class.java) }
    val taskService: TaskService by lazy { buildRestWithServiceKey(TaskService::class.java) }
    val userTaskService: UserTaskService by lazy { buildRestWithServiceKey(UserTaskService::class.java) }
    val taskUpdateService: TaskUpdateService by lazy { buildRestWithServiceKey(TaskUpdateService::class.java) }

    /* ---------- REST APIs using dynamic JWT token (per session) ---------- */
    fun getApiService(token: String) = buildRest(token, ApiService::class.java)
    fun getProjectService(token: String) = buildRest(token, ProjectService::class.java)
    fun getTaskService(token: String) = buildRest(token, TaskService::class.java)
    fun getUserTaskService(token: String) = buildRest(token, UserTaskService::class.java)
    fun getTaskUpdateService(token: String) = buildRest(token, TaskUpdateService::class.java)
    fun getEvaluationService(token: String): EvaluationService = buildRest(token, EvaluationService::class.java)
    fun getProjectTeamService(token: String): ProjectTeamService = buildRest(token, ProjectTeamService::class.java)
}

package com.baptistaz.taskwave.data.remote

import com.baptistaz.taskwave.BuildConfig
import com.baptistaz.taskwave.data.remote.auth.AuthService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val BASE_URL = "https://wdeiduqwfhoepbiajtpj.supabase.co/rest/v1/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private val authClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    val auth: AuthService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL + "/")
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthService::class.java)
    }
}

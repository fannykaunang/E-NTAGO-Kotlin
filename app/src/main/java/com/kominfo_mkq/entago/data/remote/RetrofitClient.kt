package com.kominfo_mkq.entago.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.utils.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import androidx.core.content.edit

object RetrofitClient {
    //const val BASE_URL = "https://192.168.110.236:7113/" // Real Device (ganti IP)
    const val BASE_URL = "https://entago.merauke.go.id/" // Real Device (ganti IP)
    private const val API_KEY = "f26d27b0b8a01f0390767155e17745e2"
    private var retrofit: Retrofit? = null

    fun getClient(sharedPreferences: SharedPreferences): Retrofit {
        if (retrofit == null) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                // --- INTERCEPTOR UTAMA ---
                .addInterceptor { chain ->
                    val token = sharedPreferences.getString("token", "")
                    val originalRequest = chain.request()

                    val requestBuilder = originalRequest.newBuilder()
                        .addHeader("X-Api-Key", API_KEY)
                        .addHeader("Content-Type", "application/json")

                    if (!token.isNullOrEmpty()) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }

                    // 1. Eksekusi Request
                    val response = chain.proceed(requestBuilder.build())

                    // 2. CEK APAKAH ERROR 401?
                    val isLoginRequest = originalRequest.url.toString().contains("login")

                    if (response.code == 401 && !isLoginRequest) {
                        // Jika 401 terjadi dan ini bukan saat sedang login,
                        // berarti token expired atau tidak valid.

                        android.util.Log.e("AUTH_DEBUG", "Sesi Expired (401). Memulai Auto-Logout...")

                        // Hapus data token di memori agar state app bersih
                        sharedPreferences.edit { remove("token") }

                        // Pemicu navigasi global (SessionManager)
                        runBlocking {
                            SessionManager.triggerUnauthorized()
                        }
                    }

                    response
                }
                .addInterceptor(logging)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }
        return retrofit!!
    }

//    fun getClient(sharedPrefs: SharedPreferences): Retrofit {
//
//        // 1. Logging Interceptor (Untuk debugging di terminal)
//        val logging = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        }
//
//        val headerInterceptor = Interceptor { chain ->
//            val token = sharedPrefs.getString("token", "")
//            val request = chain.request().newBuilder()
//                .addHeader("X-Api-Key", API_KEY)
//                .addHeader("Content-Type", "application/json")
//                .addHeader("Accept", "application/json")
//                .apply {
//                    // Hanya tambahkan Authorization jika token ada
//                    if (!token.isNullOrEmpty()) {
//                        addHeader("Authorization", "Bearer $token")
//                    }
//                }
//                .build()
//            chain.proceed(request)
//        }
//
//        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor(logging)
//            .addInterceptor(headerInterceptor)
//            .build()
//
//        return Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(okHttpClient)
//            .build()
//    }

    fun getInstance(context: Context): ApiService {
        val prefManager = PrefManager(context)
        return getClient(prefManager.sharedPrefs).create(ApiService::class.java)
    }
}
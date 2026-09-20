package com.the5watermelons.motionpulse.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MotionPulseApiService {

    // Using real public test endpoint: jsonplaceholder.typicode.com/posts
    // This allows us to perform a real REST POST request and get a real status code back.
    @POST("posts")
    suspend fun logMood(@Body payload: MoodLogPayload)

    @GET("posts/1")
    suspend fun getMoodStreak(@Query("userId") userId: String): MoodStreakResponse

    companion object {
        private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

        fun create(): MotionPulseApiService {
            // Add real-time network logging so you can see the data flow in Logcat
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MotionPulseApiService::class.java)
        }
    }
}
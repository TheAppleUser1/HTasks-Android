package com.gdaniel.htasks

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiService {
    private const val API_KEY = "AIzaSyDHXGA4eOKAw6PYS83RCdwveTWLg7_BHEQ"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val ENDPOINT = "v1beta/models/gemini-2.0-flash-lite:generateContent?key=$API_KEY"

    private val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }

    suspend fun sendMessage(message: String): String = withContext(Dispatchers.IO) {
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = message))
                )
            ),
            generationConfig = GeminiGenerationConfig()
        )
        val response = api.sendMessage(ENDPOINT, request)
        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Sorry, no response."
    }

    interface GeminiApi {
        @Headers("Content-Type: application/json")
        @POST
        suspend fun sendMessage(
            @retrofit2.http.Url url: String,
            @Body body: GeminiRequest
        ): GeminiResponse
    }

    data class GeminiRequest(
        val contents: List<GeminiContent>,
        @SerializedName("generationConfig") val generationConfig: GeminiGenerationConfig
    )
    data class GeminiContent(val parts: List<GeminiPart>)
    data class GeminiPart(val text: String)
    data class GeminiGenerationConfig(
        val temperature: Double = 0.7,
        val topK: Int = 40,
        val topP: Double = 0.95,
        val maxOutputTokens: Int = 1024
    )
    data class GeminiResponse(
        val candidates: List<GeminiCandidate>?
    )
    data class GeminiCandidate(
        val content: GeminiContentResponse?
    )
    data class GeminiContentResponse(
        val parts: List<GeminiPartResponse>
    )
    data class GeminiPartResponse(
        val text: String?
    )
} 
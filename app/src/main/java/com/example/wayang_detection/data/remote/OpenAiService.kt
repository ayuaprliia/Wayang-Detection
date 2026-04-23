package com.example.wayang_detection.data.remote

import com.example.wayang_detection.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Lightweight OpenAI ChatCompletions client for wayang character elaboration.
 * Uses HttpURLConnection (no extra dependencies needed).
 *
 * Model: gpt-4o-mini (cost-effective, fast)
 */
object OpenAiService {

    private const val SYSTEM_PROMPT = """Kamu adalah seorang ahli pewayangan Bali (wayang kulit) yang sangat berpengetahuan.
Kamu memahami sejarah, filosofi, mitologi, dan tradisi pewayangan Bali secara mendalam.
Jawab pertanyaan dengan bahasa Indonesia yang baik, informatif, dan menarik.
Berikan penjelasan yang kaya akan detail budaya dan spiritual.
Gunakan format yang mudah dibaca dengan paragraf yang terstruktur.
Jangan gunakan format markdown seperti ** atau ##, cukup teks biasa dengan paragraf."""

    /**
     * Ask the AI to elaborate on a wayang character.
     *
     * @param characterName Name of the wayang character
     * @param context Background info (description, category, traits, philosophy)
     * @param question User's question or "elaborate" for general elaboration
     * @return AI response text, or error message if request fails
     */
    suspend fun askAboutWayang(
        characterName: String,
        context: String,
        question: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userMessage = buildString {
                append("Karakter wayang: $characterName\n\n")
                append("Informasi yang sudah diketahui:\n$context\n\n")
                append("Pertanyaan: $question")
            }

            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", SYSTEM_PROMPT)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                })
            }

            val requestBody = JSONObject().apply {
                put("model", Constants.OPENAI_MODEL)
                put("messages", messagesArray)
                put("max_tokens", 800)
                put("temperature", 0.7)
            }

            val url = URL(Constants.OPENAI_API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer ${Constants.OPENAI_API_KEY}")
                doOutput = true
                connectTimeout = 30_000
                readTimeout = 60_000
            }

            // Send request
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(
                    InputStreamReader(connection.inputStream, Charsets.UTF_8)
                ).use { it.readText() }

                val jsonResponse = JSONObject(response)
                val content = jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()

                Result.success(content)
            } else {
                val errorStream = connection.errorStream
                val errorBody = if (errorStream != null) {
                    BufferedReader(InputStreamReader(errorStream, Charsets.UTF_8))
                        .use { it.readText() }
                } else {
                    "Unknown error"
                }
                Result.failure(Exception("API Error ($responseCode): $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke AI: ${e.localizedMessage}"))
        }
    }

    /**
     * Build a context string from character data for the AI prompt.
     */
    fun buildCharacterContext(
        name: String,
        category: String,
        group: String,
        traits: List<String>,
        description: String,
        philosophy: String
    ): String = buildString {
        append("Nama: $name\n")
        append("Kategori: $category\n")
        append("Kelompok: $group\n")
        if (traits.isNotEmpty()) {
            append("Sifat: ${traits.joinToString(", ")}\n")
        }
        if (description.isNotEmpty()) {
            append("Deskripsi: $description\n")
        }
        if (philosophy.isNotEmpty()) {
            append("Filosofi: $philosophy\n")
        }
    }
}

package mr.talkingtoy.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.ResponseStoppedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ChatData {
    val api_key = "AIzaSyD7MzMbYK9N3esZF7cckzSPivXluN3ZKNA"

    suspend fun getResponse(prompt: String): String {
        val generativeModel = GenerativeModel(
            modelName = "gemini-pro", apiKey = api_key
        )
        try {
            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(prompt)
            }
                return response.text ?: "error"
        } catch (e: ResponseStoppedException) {
            return "error"
        }
    }
}
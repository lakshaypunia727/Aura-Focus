package com.example.ai

import com.example.BuildConfig
import com.example.data.model.DailyStatEntity
import com.example.data.model.TaskItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiProductivityCoach {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateCoachingInsights(
        userPrompt: String,
        customApiKey: String?,
        totalFocusMinutes: Int,
        completedSessions: Int,
        tasks: List<TaskItemEntity>,
        recentStats: List<DailyStatEntity>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = when {
            !customApiKey.isNullOrBlank() -> customApiKey.trim()
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        // Construct productivity context
        val completedTaskCount = tasks.count { it.isCompleted }
        val pendingTaskCount = tasks.count { !it.isCompleted }
        val pastWeekMinutes = recentStats.take(7).sumOf { it.totalFocusMinutes }
        val totalDistractions = recentStats.take(7).sumOf { it.distractionAttemptsResisted }

        val systemContext = """
            You are AuraFocus AI, an elite mindful productivity coach and cognitive performance mentor.
            User's Current Metrics:
            - Lifetime Focus Time: $totalFocusMinutes minutes ($completedSessions completed pomodoro sessions)
            - Past 7 Days Focus: $pastWeekMinutes minutes
            - Tasks: $completedTaskCount completed, $pendingTaskCount pending
            - Distraction temptations shielded: $totalDistractions times
            
            Give concise, inspiring, actionable, aesthetic advice formatted with clear bullet points and gentle wisdom. 
            Keep it under 250 words. Focus on sustainable flow state, healthy break recovery, and mindful discipline.
        """.trimIndent()

        if (apiKey.isBlank()) {
            return@withContext generateOfflineHeuristicInsights(
                userPrompt,
                totalFocusMinutes,
                pastWeekMinutes,
                completedTaskCount,
                pendingTaskCount,
                totalDistractions
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val jsonPayload = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", "$systemContext\n\nUser Question/Request: $userPrompt"))
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 800)
                })
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Network error"
                return@withContext "AI Coach is currently operating in offline mode.\n\n" +
                    generateOfflineHeuristicInsights(userPrompt, totalFocusMinutes, pastWeekMinutes, completedTaskCount, pendingTaskCount, totalDistractions)
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                text.trim()
            } else {
                generateOfflineHeuristicInsights(userPrompt, totalFocusMinutes, pastWeekMinutes, completedTaskCount, pendingTaskCount, totalDistractions)
            }
        } catch (e: Exception) {
            "⚡ Offline Mode Active\n\n" +
                generateOfflineHeuristicInsights(userPrompt, totalFocusMinutes, pastWeekMinutes, completedTaskCount, pendingTaskCount, totalDistractions)
        }
    }

    private fun generateOfflineHeuristicInsights(
        prompt: String,
        totalFocusMinutes: Int,
        pastWeekMinutes: Int,
        completedTasks: Int,
        pendingTasks: Int,
        distractions: Int
    ): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("week") || lower.contains("report") || lower.contains("audit") -> {
                """
                📊 **AuraFocus Weekly Productivity Audit**
                
                • **Weekly Focus Volume:** $pastWeekMinutes mins (${pastWeekMinutes / 25} pomodoro cycles logged).
                • **Execution Rate:** $completedTasks tasks closed, with $pendingTasks key tasks currently queued.
                • **Distraction Defense:** Resisted $distractions distraction urges with active App Shield.
                
                ✨ **Coach's Key Finding:**
                Your momentum is steady. You achieve peak cognitive clarity in 25-minute intervals. To elevate performance next week, batch your high-friction creative tasks into the morning block before checking communications.
                """.trimIndent()
            }
            lower.contains("goal") || lower.contains("plan") || lower.contains("target") -> {
                """
                🎯 **Smart Goal & Pacing Architecture**
                
                • **Daily Rhythm:** Aim for 4 Focus Pomodoros (100 min) daily.
                • **Deep Work Block:** 2 consecutive pomodoros on your highest priority task, followed by 1 full 15-minute restorative walk or stretch.
                • **Cognitive Pacing:** Never skip the 5-minute break; looking at distant objects relaxes the ciliary eye muscles and resets dopamine baseline.
                """.trimIndent()
            }
            lower.contains("distract") || lower.contains("block") || lower.contains("social") -> {
                """
                🛡️ **Cognitive Shield Protocol**
                
                • **Dopamine Friction:** Keeping WhatsApp, Instagram, and games locked behind the AuraFocus Blocker saves an average of 47 minutes of fragmented attention daily.
                • **The 10-Second Rule:** When an impulse strikes to check notifications, take three deep belly breaths. The micro-craving peak subsides in 8 to 15 seconds.
                • **Flow State Protection:** Every external context switch costs up to 23 minutes of focus re-entry time. Your shield is doing the heavy lifting.
                """.trimIndent()
            }
            else -> {
                """
                🌿 **Mindful Focus Insight**
                
                "True productivity is not frantic velocity, but calm, unhurried presence in the task at hand."
                
                • **Current Progress:** $totalFocusMinutes lifetime minutes dedicated to deliberate practice.
                • **Next Step:** Select your top priority task, initiate a 25-minute session, and let the timer hold the container for your work.
                • **Late Night Tip:** In evening hours, ensure Dark Mode and the Eye Comfort filter are active to protect your melatonin synthesis.
                """.trimIndent()
            }
        }
    }
}

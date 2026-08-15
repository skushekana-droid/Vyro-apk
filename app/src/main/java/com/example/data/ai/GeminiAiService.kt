package com.example.data.ai

import com.example.BuildConfig
import com.example.model.AiTaskType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateCreatorAssistance(
        taskType: AiTaskType,
        userInput: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        val prompt = buildPromptForTask(taskType, userInput)

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Intelligent local fallback response when offline or key not yet set in Secrets
            return@withContext generateLocalFallback(taskType, userInput)
        }

        try {
            val jsonPayload = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext generateLocalFallback(taskType, userInput)
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                text.trim()
            } else {
                generateLocalFallback(taskType, userInput)
            }
        } catch (e: Exception) {
            generateLocalFallback(taskType, userInput)
        }
    }

    private fun buildPromptForTask(taskType: AiTaskType, input: String): String {
        return when (taskType) {
            AiTaskType.VIRAL_TITLES ->
                "You are an expert viral content growth strategist on the VYRO platform. Given this topic/input: '$input', generate 5 high-CTR, curiosity-inducing viral video titles with emoji hooks. Return as a clean numbered list."

            AiTaskType.DESCRIPTIONS_TAGS ->
                "You are a top-tier SEO copywriter for creators on VYRO. Given: '$input', write a compelling 3-paragraph video description, followed by 10 high-ranking searchable tags, and 3 call-to-actions linking to VYRO commerce store."

            AiTaskType.SHORTS_SCRIPT ->
                "You are an award-winning short-form video director. For: '$input', generate a 45-second high-retention vertical script with [0-3s Hook], [Visual Cue], [Spoken Narration], [30s Value Drop], and [40-45s Call to Action]."

            AiTaskType.CONTENT_IDEAS ->
                "You are an AI creator economy mentor on VYRO. For: '$input', give 5 innovative multi-part content series ideas, monetizable digital product concepts, and community poll angles."

            AiTaskType.THUMBNAIL_PROMPTS ->
                "You are a visual design director. For: '$input', create 3 high-contrast, clickable thumbnail visual compositions describing the foreground subject, color grading, facial expression, and 3-word text overlay."

            AiTaskType.CONTENT_CALENDAR ->
                "You are a creator production manager. For: '$input', design a strategic 7-day multi-format content release schedule (2 Long-form videos, 4 VYRO Shorts, 1 Community Poll)."
        }
    }

    private fun generateLocalFallback(taskType: AiTaskType, input: String): String {
        val topic = if (input.isNotBlank()) input else "Creator Economy & Digital Tech"
        return when (taskType) {
            AiTaskType.VIRAL_TITLES -> """
                🔥 1. The $topic Secret Nobody Is Talking About in 2026
                ⚡ 2. Why Everything You Know About $topic Is Changing Right Now
                🚀 3. How I Scaled My $topic System (Step-by-Step Blueprint)
                💡 4. Don't Make This $topic Mistake Before Watching This
                💎 5. The $topic Revolution: How Creators Are Earning 10x More
            """.trimIndent()

            AiTaskType.DESCRIPTIONS_TAGS -> """
                In this video, we break down the definitive blueprint for $topic. Discover practical tactics, creator workflows, and how to turn viewer attention into sustainable economic value on VYRO.
                
                📌 Timestamps:
                0:00 - The Paradigm Shift in $topic
                2:15 - Core Framework & Breakdown
                6:40 - Monetization & Commerce Integration
                9:30 - Future Outlook & Community Takeaways

                🏷️ Recommended Tags:
                #VYRO #$topic #CreatorEconomy #FutureTech #DigitalCreators #Innovation #Commerce #Productivity #Mastery #Viral2026
            """.trimIndent()

            AiTaskType.SHORTS_SCRIPT -> """
                🎬 [0:00 - 0:03] HOOK: "Stop scrolling if you care about $topic — because everything just shifted."
                👁️ [VISUAL]: Fast zoom-in with neon glitch overlay and bold dynamic subtitle.
                
                🎙️ [0:04 - 0:20] THE PROBLEM: "Most creators approach $topic the old way and miss 90% of their potential engagement."
                
                ⚡ [0:21 - 0:38] THE SOLUTION: "Here is the exact 3-step engine: 1. Target micro-moments. 2. Embed direct digital store links. 3. Build a closed community loop."
                
                🚀 [0:39 - 0:45] CTA: "Drop a 🔥 in the comments if you want the complete playbook, and subscribe for part two!"
            """.trimIndent()

            AiTaskType.CONTENT_IDEAS -> """
                💡 1. The 30-Day $topic Challenge (Daily Shorts + Weekly Deep Dive)
                📊 2. Breaking Down the Top 1% in $topic: What They Do Differently
                🛠️ 3. Live Tool Walkthrough: My Everyday Setup for $topic
                💰 4. Turning $topic Knowledge into a Digital Mini-Course on VYRO Store
                🎙️ 5. Unfiltered Q&A: Answering Your Burning Questions on $topic
            """.trimIndent()

            AiTaskType.THUMBNAIL_PROMPTS -> """
                🎨 Concept 1: High-Contrast Split Screen
                - Left: Dark desaturated problem view with red warning icon.
                - Right: Glowing cyan/violet cyberpunk studio with smiling creator.
                - Overlay Text: "THE 2026 SHIFT"

                🎨 Concept 2: Shock & Data Hologram
                - Subject: Expressive creator pointing upwards toward floating 3D revenue chart.
                - Lighting: Electric purple rim light and deep obsidian backdrop.
                - Overlay Text: "10X RESULTS"

                🎨 Concept 3: Minimalist Golden Ratio
                - Subject: Centered sleek hardware/concept piece with radial gold particle bloom.
                - Overlay Text: "DON'T MISS THIS"
            """.trimIndent()

            AiTaskType.CONTENT_CALENDAR -> """
                📅 Monday: Long-form Deep Dive on "$topic: The Core Strategy"
                📅 Tuesday: VYRO Short — "Top 3 Tools for $topic"
                📅 Wednesday: Community Discussion & Interactive Poll on Audience Hurdles
                📅 Thursday: VYRO Short — "Avoid This Costly $topic Mistake"
                📅 Friday: Long-form Case Study + Digital Product Drop on VYRO Store
                📅 Saturday: VYRO Short — "Behind the Scenes Workflow"
                📅 Sunday: Creator Studio Livestream Q&A & Top Supporter Shoutouts
            """.trimIndent()
        }
    }
}

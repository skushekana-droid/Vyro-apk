package com.example.infrastructure.ai

import com.example.model.AiTaskType

enum class AiProviderType(val displayName: String, val isSelfHosted: Boolean) {
    GEMINI("Google Gemini 3.5 Flash / Pro (Cloud Adapter)", false),
    OPENAI("OpenAI GPT-4o / Sora (Cloud Adapter)", false),
    LOCAL_LLAMA("VYRO Neural Engine (Self-Hosted Llama 3.3 / vLLM Cluster)", true),
    ANTHROPIC("Anthropic Claude 3.5 Sonnet (Cloud Adapter)", false)
}

data class AiUsageMetric(
    val provider: AiProviderType,
    val promptTokens: Int,
    val completionTokens: Int,
    val latencyMs: Long,
    val costUsd: Double
)

interface VyroAiEngine {
    val activeProvider: AiProviderType

    suspend fun generateText(
        taskType: AiTaskType,
        userInput: String,
        systemPrompt: String? = null
    ): String

    suspend fun analyzeVideo(
        videoUrl: String,
        query: String
    ): String

    suspend fun transcribeAndTranslate(
        audioUrl: String,
        targetLanguage: String = "en"
    ): String

    suspend fun assistModeration(
        content: String,
        authorContext: String
    ): Pair<Double, String> // Toxicity score (0-1), explanation
}

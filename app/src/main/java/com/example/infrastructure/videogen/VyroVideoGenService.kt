package com.example.infrastructure.videogen

import com.example.infrastructure.events.VyroEventBus
import com.example.infrastructure.events.VyroEventType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class GeneratedVideoProject(
    val id: String = "ai_vid_${UUID.randomUUID().toString().take(8)}",
    val prompt: String,
    val style: String,
    val durationSeconds: Int = 10,
    val fps: Int = 30,
    val resolution: String = "1080p Full HD",
    val progressPercent: Int = 0,
    val status: String = "QUEUED",
    val videoUrl: String? = null,
    val previewGifUrl: String? = null,
    val isPublishedToVyro: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

object VyroVideoGenService {
    private val _projects = MutableStateFlow<List<GeneratedVideoProject>>(createSampleProjects())
    val projects: StateFlow<List<GeneratedVideoProject>> = _projects.asStateFlow()

    private val _isRendering = MutableStateFlow(false)
    val isRendering: StateFlow<Boolean> = _isRendering.asStateFlow()

    private fun createSampleProjects(): List<GeneratedVideoProject> {
        return listOf(
            GeneratedVideoProject(
                id = "ai_vid_city_10s",
                prompt = "A 10-second ultra-photorealistic flythrough of a futuristic neon cyber-city at dusk with flying transit vehicles and purple ambient lighting",
                style = "Hyper-Realistic Sci-Fi",
                durationSeconds = 10,
                progressPercent = 100,
                status = "READY",
                videoUrl = "https://media.vyro.internal/ai-gen/cyber_city.mp4",
                isPublishedToVyro = true
            )
        )
    }

    suspend fun submitVideoPrompt(
        prompt: String,
        style: String = "Cinematic Photorealism",
        durationSeconds: Int = 10,
        onProgress: ((Int) -> Unit)? = null
    ): GeneratedVideoProject {
        _isRendering.value = true

        val project = GeneratedVideoProject(
            prompt = prompt,
            style = style,
            durationSeconds = durationSeconds,
            status = "DIFFUSION_RENDERING"
        )
        _projects.value = listOf(project) + _projects.value

        VyroEventBus.emit(
            VyroEventType.AI_GENERATION_QUEUED,
            mapOf("type" to "VIDEO_DIFFUSION", "prompt" to prompt.take(60)),
            sourceService = "vyro-video-gen"
        )

        // Progress rendering simulation
        for (progress in listOf(15, 35, 60, 85, 100)) {
            delay(300)
            onProgress?.invoke(progress)
            _projects.value = _projects.value.map {
                if (it.id == project.id) it.copy(progressPercent = progress) else it
            }
        }

        val completed = project.copy(
            progressPercent = 100,
            status = "READY",
            videoUrl = "https://media.vyro.internal/ai-gen/${project.id}.mp4"
        )

        _projects.value = _projects.value.map {
            if (it.id == completed.id) completed else it
        }
        _isRendering.value = false

        VyroEventBus.emit(
            VyroEventType.AI_GENERATION_COMPLETED,
            mapOf("type" to "VIDEO_DIFFUSION", "projectId" to completed.id),
            sourceService = "vyro-video-gen"
        )

        return completed
    }

    fun markAsPublished(projectId: String) {
        _projects.value = _projects.value.map {
            if (it.id == projectId) it.copy(isPublishedToVyro = true) else it
        }
    }
}

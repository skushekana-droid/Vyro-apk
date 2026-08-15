package com.example.infrastructure.imagegen

import com.example.infrastructure.events.VyroEventBus
import com.example.infrastructure.events.VyroEventType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class ImageAspectRatio(val label: String, val width: Int, val height: Int) {
    THUMBNAIL_16_9("16:9 (YouTube/Feed Thumbnail)", 1280, 720),
    SHORTS_9_16("9:16 (Shorts Cover / Story)", 720, 1280),
    SQUARE_1_1("1:1 (Profile Avatar)", 512, 512),
    BANNER_3_1("3:1 (Channel Header Banner)", 1500, 500)
}

data class GeneratedImageItem(
    val id: String = "img_${UUID.randomUUID().toString().take(8)}",
    val prompt: String,
    val style: String,
    val aspectRatio: ImageAspectRatio,
    val imageUrl: String,
    val status: String = "COMPLETED",
    val createdAt: Long = System.currentTimeMillis()
)

object VyroImageGenService {
    private val _history = MutableStateFlow<List<GeneratedImageItem>>(createSampleImages())
    val history: StateFlow<List<GeneratedImageItem>> = _history.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private fun createSampleImages(): List<GeneratedImageItem> {
        return listOf(
            GeneratedImageItem(
                id = "img_thumb_1",
                prompt = "Cyberpunk content creator studio with glowing purple holographic interface and 4K displays",
                style = "Cinematic Cyberpunk",
                aspectRatio = ImageAspectRatio.THUMBNAIL_16_9,
                imageUrl = "vyro_hero_banner"
            ),
            GeneratedImageItem(
                id = "img_thumb_2",
                prompt = "Futuristic decentralized creator economy token matrix neon violet",
                style = "3D Octane Render",
                aspectRatio = ImageAspectRatio.THUMBNAIL_16_9,
                imageUrl = "vyro_thumb_cyber"
            )
        )
    }

    suspend fun generateImage(
        prompt: String,
        style: String = "Cinematic 8K",
        aspectRatio: ImageAspectRatio = ImageAspectRatio.THUMBNAIL_16_9
    ): GeneratedImageItem {
        _isGenerating.value = true

        VyroEventBus.emit(
            VyroEventType.AI_GENERATION_QUEUED,
            mapOf("type" to "IMAGE_GEN", "prompt" to prompt.take(50), "aspectRatio" to aspectRatio.label),
            sourceService = "vyro-image-gen"
        )

        delay(800) // Processing simulation

        val newItem = GeneratedImageItem(
            prompt = prompt,
            style = style,
            aspectRatio = aspectRatio,
            imageUrl = "vyro_hero_banner"
        )

        _history.value = listOf(newItem) + _history.value
        _isGenerating.value = false

        VyroEventBus.emit(
            VyroEventType.AI_GENERATION_COMPLETED,
            mapOf("type" to "IMAGE_GEN", "id" to newItem.id),
            sourceService = "vyro-image-gen"
        )

        return newItem
    }
}

package com.example.infrastructure.video

import com.example.infrastructure.events.VyroEventBus
import com.example.infrastructure.events.VyroEventType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class TranscodingStep(val stepNumber: Int, val label: String) {
    UPLOAD_VALIDATION(1, "Validating container format, codecs, and bitrate"),
    SECURITY_VIRUS_SCAN(2, "Running ClamAV / Sandbox security scanning"),
    RAW_ORIGINAL_ARCHIVE(3, "Storing immutable raw master to S3 storage"),
    FFMPEG_TRANSCODING_LADDER(4, "FFmpeg adaptive ladder encoding (360p - 4K)"),
    AUDIO_NORMALIZATION(5, "EBU R128 audio loudness normalization (-14 LUFS)"),
    SMART_THUMBNAIL_SPRITES(6, "Generating AI keyframes & seekable scrub sprite sheet"),
    HLS_DASH_PACKAGING(7, "Packaging HLS (.m3u8) & MPEG-DASH (.mpd) manifests"),
    METADATA_EXTRACTION(8, "Extracting color primaries, HDR metadata & frame counts"),
    DATABASE_REGISTRATION(9, "Registering media asset in PostgreSQL relational catalog"),
    EDGE_CACHE_WARMING(10, "Warming VYRO Edge CDN nodes & publishing live stream")
}

enum class VideoResolution(val label: String, val width: Int, val height: Int, val targetBitrateKbps: Int) {
    RES_360P("360p (Mobile Low)", 640, 360, 800),
    RES_480P("480p (SD)", 854, 480, 1400),
    RES_720P("720p (HD 60fps)", 1280, 720, 3200),
    RES_1080P("1080p (Full HD 60fps)", 1920, 1080, 6000),
    RES_1440P("1440p (2K Quad HD)", 2560, 1440, 12000),
    RES_4K("4K (Ultra HD HDR)", 3840, 2160, 24000)
}

data class TranscodingJob(
    val jobId: String = "job_${UUID.randomUUID().toString().take(8)}",
    val videoTitle: String,
    val creatorId: String,
    val rawFileSizeMb: Double,
    val durationSeconds: Int,
    val currentStep: TranscodingStep = TranscodingStep.UPLOAD_VALIDATION,
    val progressPercent: Int = 0,
    val generatedResolutions: List<VideoResolution> = emptyList(),
    val masterManifestUrl: String? = null,
    val thumbnailUrl: String? = null,
    val status: String = "QUEUED",
    val logs: List<String> = emptyList(),
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

object VyroVideoEngine {
    private val _activeJobs = MutableStateFlow<List<TranscodingJob>>(emptyList())
    val activeJobs: StateFlow<List<TranscodingJob>> = _activeJobs.asStateFlow()

    private val _completedJobs = MutableStateFlow<List<TranscodingJob>>(createSampleJobs())
    val completedJobs: StateFlow<List<TranscodingJob>> = _completedJobs.asStateFlow()

    private fun createSampleJobs(): List<TranscodingJob> {
        return listOf(
            TranscodingJob(
                jobId = "job_v8921a",
                videoTitle = "Building Decentralized AI Clusters",
                creatorId = "creator_me",
                rawFileSizeMb = 1420.5,
                durationSeconds = 840,
                currentStep = TranscodingStep.EDGE_CACHE_WARMING,
                progressPercent = 100,
                generatedResolutions = listOf(
                    VideoResolution.RES_360P,
                    VideoResolution.RES_720P,
                    VideoResolution.RES_1080P,
                    VideoResolution.RES_4K
                ),
                masterManifestUrl = "https://media.vyro.internal/hls/v8921a/master.m3u8",
                thumbnailUrl = "https://media.vyro.internal/thumbs/v8921a_hero.webp",
                status = "PUBLISHED",
                logs = listOf(
                    "[00:01] Container validated: MP4 (H.264 / AAC 48kHz)",
                    "[00:03] Security scan PASSED (0 threats detected)",
                    "[00:08] Raw master saved to vyro-storage-raw/v8921a.mov",
                    "[00:15] Transcoding complete: 4 resolutions rendered with libx264/nvenc",
                    "[00:20] HLS manifest generated: /hls/v8921a/master.m3u8",
                    "[00:22] Deployed to global edge caches."
                ),
                completedAt = System.currentTimeMillis() - 3600000
            )
        )
    }

    suspend fun submitVideoJob(
        title: String,
        creatorId: String,
        fileSizeMb: Double,
        durationSeconds: Int,
        isShort: Boolean = false,
        onProgressUpdate: ((TranscodingJob) -> Unit)? = null
    ): TranscodingJob {
        val job = TranscodingJob(
            videoTitle = title,
            creatorId = creatorId,
            rawFileSizeMb = fileSizeMb,
            durationSeconds = durationSeconds,
            status = "PROCESSING"
        )

        _activeJobs.value = _activeJobs.value + job

        VyroEventBus.emit(
            VyroEventType.VIDEO_UPLOADED,
            mapOf("jobId" to job.jobId, "title" to title, "creatorId" to creatorId),
            actorId = creatorId,
            sourceService = "vyro-video-engine"
        )

        var currentJob = job
        val logList = mutableListOf<String>()

        // Simulate 10-step independent video processing pipeline
        for (step in TranscodingStep.values()) {
            val stepPercent = (step.stepNumber * 10)
            val logMessage = "[Step ${step.stepNumber}/10] ${step.label}"
            logList.add(logMessage)

            val resolutions = if (step.stepNumber >= 4) {
                if (isShort) {
                    listOf(VideoResolution.RES_720P, VideoResolution.RES_1080P)
                } else {
                    listOf(VideoResolution.RES_360P, VideoResolution.RES_720P, VideoResolution.RES_1080P, VideoResolution.RES_4K)
                }
            } else emptyList()

            currentJob = currentJob.copy(
                currentStep = step,
                progressPercent = stepPercent,
                generatedResolutions = resolutions,
                logs = logList.toList(),
                masterManifestUrl = if (step.stepNumber >= 7) "https://media.vyro.internal/hls/${currentJob.jobId}/master.m3u8" else null,
                thumbnailUrl = if (step.stepNumber >= 6) "https://media.vyro.internal/thumbs/${currentJob.jobId}_thumb.webp" else null
            )

            _activeJobs.value = _activeJobs.value.map { if (it.jobId == currentJob.jobId) currentJob else it }
            onProgressUpdate?.invoke(currentJob)

            VyroEventBus.emit(
                VyroEventType.VIDEO_TRANSCODING_PROGRESS,
                mapOf("jobId" to currentJob.jobId, "step" to step.name, "progress" to "$stepPercent%"),
                actorId = creatorId,
                sourceService = "vyro-video-engine"
            )

            delay(250) // Non-blocking simulation step
        }

        val completedJob = currentJob.copy(
            status = "PUBLISHED",
            progressPercent = 100,
            completedAt = System.currentTimeMillis()
        )

        _activeJobs.value = _activeJobs.value.filter { it.jobId != completedJob.jobId }
        _completedJobs.value = listOf(completedJob) + _completedJobs.value

        VyroEventBus.emit(
            VyroEventType.VIDEO_PUBLISHED,
            mapOf("jobId" to completedJob.jobId, "title" to title, "manifestUrl" to (completedJob.masterManifestUrl ?: "")),
            actorId = creatorId,
            sourceService = "vyro-video-engine"
        )

        return completedJob
    }
}

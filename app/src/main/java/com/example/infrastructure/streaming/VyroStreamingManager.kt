package com.example.infrastructure.streaming

import com.example.infrastructure.video.VideoResolution

data class VideoChapter(
    val title: String,
    val startSeconds: Int,
    val endSeconds: Int
)

data class AudioTrack(
    val id: String,
    val language: String,
    val label: String,
    val isDefault: Boolean = false
)

data class SubtitleTrack(
    val id: String,
    val language: String,
    val label: String,
    val vttUrl: String
)

data class PlaybackSession(
    val videoId: String,
    val currentPositionSeconds: Int,
    val durationSeconds: Int,
    val selectedResolution: VideoResolution?,
    val isAutoQuality: Boolean = true,
    val activeAudioTrack: AudioTrack,
    val activeSubtitleTrack: SubtitleTrack?,
    val bufferHealthSeconds: Double = 18.5,
    val currentBitrateKbps: Int = 5400,
    val playbackHistoryTimestamp: Long = System.currentTimeMillis()
)

object VyroStreamingManager {
    fun getAdaptiveManifestForVideo(videoId: String): String {
        return """
            #EXTM3U
            #EXT-X-VERSION:6
            #EXT-X-INDEPENDENT-SEGMENTS
            
            #EXT-X-STREAM-INF:BANDWIDTH=800000,RESOLUTION=640x360,FRAME-RATE=30.000,CODECS="avc1.4d401e,mp4a.40.2"
            https://edge.vyro.network/hls/$videoId/360p/index.m3u8
            
            #EXT-X-STREAM-INF:BANDWIDTH=3200000,RESOLUTION=1280x720,FRAME-RATE=60.000,CODECS="avc1.640020,mp4a.40.2"
            https://edge.vyro.network/hls/$videoId/720p/index.m3u8
            
            #EXT-X-STREAM-INF:BANDWIDTH=6000000,RESOLUTION=1920x1080,FRAME-RATE=60.000,CODECS="avc1.64002a,mp4a.40.2"
            https://edge.vyro.network/hls/$videoId/1080p/index.m3u8
            
            #EXT-X-STREAM-INF:BANDWIDTH=24000000,RESOLUTION=3840x2160,FRAME-RATE=60.000,CODECS="hev1.1.6.L153.B0,mp4a.40.2"
            https://edge.vyro.network/hls/$videoId/4k/index.m3u8
        """.trimIndent()
    }

    fun getSampleChapters(): List<VideoChapter> {
        return listOf(
            VideoChapter("Introduction & Architecture Overview", 0, 120),
            VideoChapter("Independent Transcoding Ladder (FFmpeg)", 120, 360),
            VideoChapter("PostgreSQL Relational Ledger & Scalability", 360, 600),
            VideoChapter("Self-Hosted AI & Edge Streaming", 600, 840)
        )
    }

    fun getAvailableAudioTracks(): List<AudioTrack> {
        return listOf(
            AudioTrack("en_orig", "en", "English (Original Audio - Stereo 48kHz)", isDefault = true),
            AudioTrack("es_dub", "es", "Spanish (AI Dubbed - Studio High)"),
            AudioTrack("ja_dub", "ja", "Japanese (AI Dubbed - Clean Vocal)")
        )
    }

    fun getAvailableSubtitles(): List<SubtitleTrack> {
        return listOf(
            SubtitleTrack("sub_en", "en", "English (Auto-Transcribed)", "https://edge.vyro.network/subs/en.vtt"),
            SubtitleTrack("sub_es", "es", "Spanish (Translated)", "https://edge.vyro.network/subs/es.vtt"),
            SubtitleTrack("sub_fr", "fr", "French (Translated)", "https://edge.vyro.network/subs/fr.vtt"),
            SubtitleTrack("sub_de", "de", "German (Translated)", "https://edge.vyro.network/subs/de.vtt")
        )
    }
}

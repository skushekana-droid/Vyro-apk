package com.example.infrastructure.cdn

interface MediaDeliveryService {
    val providerName: String
    val isIndependent: Boolean

    fun getStreamUrl(fileKey: String, requestedResolution: String): String
    fun getThumbnailUrl(fileKey: String): String
    fun purgeCache(fileKey: String): Boolean
    fun getCacheHitRatio(): Double
}

class VyroEdgeNetworkAdapter : MediaDeliveryService {
    override val providerName: String = "VYRO Global Anycast Edge Network (Self-Hosted PoPs)"
    override val isIndependent: Boolean = true

    override fun getStreamUrl(fileKey: String, requestedResolution: String): String {
        return "https://edge.vyro.network/hls/$fileKey/$requestedResolution/index.m3u8?token=vyro_sig_${System.currentTimeMillis()}"
    }

    override fun getThumbnailUrl(fileKey: String): String {
        return "https://edge.vyro.network/cdn/thumbs/$fileKey.webp"
    }

    override fun purgeCache(fileKey: String): Boolean = true
    override fun getCacheHitRatio(): Double = 98.4
}

class CloudflareCdnAdapter : MediaDeliveryService {
    override val providerName: String = "Cloudflare Enterprise CDN Adapter"
    override val isIndependent: Boolean = false

    override fun getStreamUrl(fileKey: String, requestedResolution: String): String {
        return "https://cdn.cloudflare.net/vyro-stream/$fileKey/$requestedResolution.m3u8"
    }

    override fun getThumbnailUrl(fileKey: String): String {
        return "https://cdn.cloudflare.net/vyro-thumbs/$fileKey.webp"
    }

    override fun purgeCache(fileKey: String): Boolean = true
    override fun getCacheHitRatio(): Double = 96.1
}

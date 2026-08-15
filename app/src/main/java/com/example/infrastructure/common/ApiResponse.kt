package com.example.infrastructure.common

import java.util.UUID

data class ApiResponse<T>(
    val success: Boolean,
    val statusCode: Int = 200,
    val message: String = "OK",
    val data: T? = null,
    val error: ApiError? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val requestId: String = "req_${UUID.randomUUID().toString().take(8)}"
) {
    companion object {
        fun <T> ok(data: T, message: String = "OK"): ApiResponse<T> =
            ApiResponse(success = true, statusCode = 200, message = message, data = data)

        fun <T> created(data: T, message: String = "Resource created"): ApiResponse<T> =
            ApiResponse(success = true, statusCode = 201, message = message, data = data)

        fun <T> error(code: Int, message: String, details: String? = null): ApiResponse<T> =
            ApiResponse(
                success = false,
                statusCode = code,
                message = message,
                error = ApiError(code = code, message = message, details = details)
            )
    }
}

data class ApiError(
    val code: Int,
    val message: String,
    val details: String? = null
)

enum class EnvironmentType(val displayName: String) {
    DEVELOPMENT("Development (Local)"),
    TESTING("Testing (Automated CI/CD)"),
    STAGING("Staging (Pre-Production Cluster)"),
    PRODUCTION("Production (Global Independent Grid)")
}

data class VyroEnvironmentConfig(
    val environment: EnvironmentType = EnvironmentType.PRODUCTION,
    val apiBaseUrl: String = "https://api.vyro.internal/v1",
    val mediaServerUrl: String = "https://media.vyro.internal",
    val cdnEdgeUrl: String = "https://edge.vyro.network",
    val postgresConnectionString: String = "postgresql://vyro_admin:***@db-cluster.internal:5432/vyro_production",
    val s3EndpointUrl: String = "https://storage.vyro.internal",
    val rateLimitPerMinute: Int = 120,
    val isAuditLogEnabled: Boolean = true,
    val isAiProviderProxySecure: Boolean = true
)

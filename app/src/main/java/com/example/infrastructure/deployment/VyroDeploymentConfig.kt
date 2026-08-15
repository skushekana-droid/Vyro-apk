package com.example.infrastructure.deployment

data class ContainerServiceSpec(
    val serviceName: String,
    val containerImage: String,
    val replicaCount: Int,
    val cpuAllocation: String,
    val memoryAllocation: String,
    val ports: List<Int>,
    val healthCheckEndpoint: String
)

object VyroDeploymentTopology {
    val DOCKER_COMPOSE_SERVICES: List<ContainerServiceSpec> = listOf(
        ContainerServiceSpec("vyro-api-gateway", "vyro/api-gateway:v2.4", 4, "2.0 vCPU", "4 GB RAM", listOf(8080, 443), "/healthz"),
        ContainerServiceSpec("vyro-auth-service", "vyro/auth-engine:v2.4", 2, "1.0 vCPU", "2 GB RAM", listOf(8081), "/auth/health"),
        ContainerServiceSpec("vyro-video-transcoder", "vyro/transcoder-ffmpeg:v2.4", 8, "8.0 vCPU + NVENC GPU", "16 GB RAM", listOf(8082), "/transcoder/status"),
        ContainerServiceSpec("vyro-media-streamer", "vyro/hls-origin:v2.4", 6, "4.0 vCPU", "8 GB RAM", listOf(8083, 1935), "/stream/ping"),
        ContainerServiceSpec("vyro-ai-neural-cluster", "vyro/vllm-llama:v3.3", 4, "A100-80GB GPU Tensor Core", "64 GB RAM", listOf(8000), "/v1/health"),
        ContainerServiceSpec("vyro-postgres-cluster", "postgres:16-alpine", 3, "8.0 vCPU", "32 GB RAM", listOf(5432), "/pg_isready"),
        ContainerServiceSpec("vyro-s3-minio-storage", "minio/minio:RELEASE", 4, "4.0 vCPU", "16 GB RAM", listOf(9000, 9001), "/minio/health/live"),
        ContainerServiceSpec("vyro-redis-event-bus", "redis:7-alpine", 3, "2.0 vCPU", "8 GB RAM", listOf(6379), "/ping")
    )

    const val DOCKER_COMPOSE_YML = """
version: '3.9'
services:
  postgres-primary:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: vyro_production
      POSTGRES_USER: vyro_admin
      POSTGRES_PASSWORD_FILE: /run/secrets/db_password
    volumes:
      - pgdata:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  minio-s3-storage:
    image: minio/minio:RELEASE.2024-08
    command: server /data --console-address ":9001"
    volumes:
      - s3data:/data
    ports:
      - "9000:9000"
      - "9001:9001"

  vyro-api-gateway:
    image: vyro/api-gateway:latest
    depends_on:
      - postgres-primary
      - minio-s3-storage
    ports:
      - "8080:8080"
    environment:
      - VYRO_ENV=production
      - DB_URL=postgresql://vyro_admin@postgres-primary:5432/vyro_production
      - S3_ENDPOINT=http://minio-s3-storage:9000
"""
}

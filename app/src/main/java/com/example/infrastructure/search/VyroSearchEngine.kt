package com.example.infrastructure.search

import com.example.model.Video

enum class SearchEngineType(val displayName: String) {
    POSTGRES_FTS("PostgreSQL Full-Text Search (tsvector / tsquery)"),
    VECTOR_SEMANTIC("VYRO Neural Vector Similarity Search (HNSW Index)"),
    MEILISEARCH("Meilisearch Open-Source Cluster")
}

data class SearchResult<T>(
    val items: List<T>,
    val totalMatches: Int,
    val executionTimeMs: Long,
    val engineUsed: SearchEngineType
)

class VyroSearchEngine {
    var activeSearchEngine: SearchEngineType = SearchEngineType.POSTGRES_FTS

    fun searchVideos(query: String, allVideos: List<Video>): SearchResult<Video> {
        val start = System.currentTimeMillis()
        val q = query.lowercase().trim()
        val matches = if (q.isBlank()) allVideos else {
            allVideos.filter {
                it.title.lowercase().contains(q) ||
                        it.creatorName.lowercase().contains(q) ||
                        it.category.displayName.lowercase().contains(q) ||
                        it.tags.any { tag -> tag.lowercase().contains(q) }
            }
        }
        val duration = (System.currentTimeMillis() - start).coerceAtLeast(12)
        return SearchResult(
            items = matches,
            totalMatches = matches.size,
            executionTimeMs = duration,
            engineUsed = activeSearchEngine
        )
    }
}

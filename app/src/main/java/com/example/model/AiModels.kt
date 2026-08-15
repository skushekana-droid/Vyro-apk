package com.example.model

enum class AiTaskType(val label: String, val iconName: String) {
    VIRAL_TITLES("Viral Video Titles", "title"),
    DESCRIPTIONS_TAGS("SEO Descriptions & Tags", "description"),
    SHORTS_SCRIPT("Shorts Video Script", "script"),
    CONTENT_IDEAS("Content Ideation & Hooks", "lightbulb"),
    THUMBNAIL_PROMPTS("Thumbnail Art Concepts", "image"),
    CONTENT_CALENDAR("Creator Content Calendar", "calendar")
}

data class AiGenerationResult(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

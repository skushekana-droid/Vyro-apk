package com.example.model

enum class HouseArea(
    val title: String,
    val subtitle: String,
    val description: String,
    val drawableResName: String,
    val iconName: String
) {
    LIVING_ROOM(
        title = "Living Room",
        subtitle = "Casual & Content",
        description = "Casual conversations, watching videos together, and relaxed brainstorming.",
        drawableResName = "img_stassen_house_1786868204218",
        iconName = "Weekend"
    ),
    OFFICE(
        title = "Office & Workstation",
        subtitle = "Coding & Analysis",
        description = "Deep research, system architecture, programming, and technical problem solving.",
        drawableResName = "img_stassen_office_1786868216401",
        iconName = "Computer"
    ),
    STUDY(
        title = "Study & Library",
        subtitle = "Documents & Learning",
        description = "Analyzing documents, reading books, educational homework, and synthesis.",
        drawableResName = "img_stassen_study_1786868226097",
        iconName = "AutoStories"
    ),
    PHONE_LOUNGE(
        title = "Phone & Comms",
        subtitle = "Live Web & Feeds",
        description = "Checking live web feeds, weather updates, notifications, and search browsing.",
        drawableResName = "img_stassen_phone_lounge_1786868275702",
        iconName = "Smartphone"
    ),
    ENTERTAINMENT(
        title = "Entertainment Hub",
        subtitle = "Music & Media",
        description = "Discovering tracks, sound design, creator ideas, and entertainment news.",
        drawableResName = "img_stassen_house_1786868204218",
        iconName = "Headphones"
    ),
    REST_AREA(
        title = "Rest & Reflection",
        subtitle = "Daily Planning",
        description = "Wellness check-ins, setting goals, scheduling reminders, and winding down.",
        drawableResName = "img_stassen_study_1786868226097",
        iconName = "Bedtime"
    )
}

enum class StassenActivityState(val displayStatus: String, val isBusy: Boolean) {
    IDLE("Stassen is ready to assist", false),
    THINKING("Stassen is thinking...", true),
    WALKING_TO_AREA("Stassen is heading to his workspace...", true),
    USING_PHONE("Stassen is checking his virtual phone...", true),
    USING_COMPUTER("Stassen is typing on his workstation...", true),
    READING_DOCUMENTS("Stassen is reading & analyzing documents...", true),
    SEARCHING_WEB("Stassen is searching live information...", true),
    WRITING_RESPONSE("Stassen is drafting your response...", true),
    SPEAKING("Stassen is speaking...", true),
    GENERATING_IMAGE("Stassen is generating visual assets...", true)
}

enum class StassenTone(val label: String, val description: String) {
    FRIENDLY("Friendly", "Warm, supportive, and approachable"),
    PROFESSIONAL("Professional", "Clear, concise, and structured"),
    CASUAL("Casual", "Relaxed, conversational, and direct"),
    FORMAL("Formal", "Thorough, polished, and academic"),
    SHORT("Concise", "High brevity, bullet points only"),
    DETAILED("Detailed", "Deep dive with full step-by-step reasoning")
}

data class StassenMessage(
    val id: String,
    val senderIsUser: Boolean,
    val text: String,
    val timestamp: String,
    val targetArea: HouseArea? = null,
    val activityState: StassenActivityState? = null,
    val attachedDocumentName: String? = null,
    val attachedImageUri: String? = null,
    val actionToolBadge: String? = null,
    val generatedImageUrl: String? = null
)

data class StassenMemoryItem(
    val id: String,
    val category: String, // e.g. "Preference", "Project", "Topic", "Style"
    val content: String,
    val createdAt: String
)

data class StassenVirtualPhoneApp(
    val id: String,
    val name: String,
    val category: String,
    val iconName: String,
    val activeSnippet: String
)

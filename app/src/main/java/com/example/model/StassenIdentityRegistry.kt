package com.example.model

import com.example.R

/**
 * Single Canonical Identity Registry for Stassen across the entire VYRO ecosystem.
 *
 * Enforces identity consistency so that all views, dialogues, avatars, scene headers,
 * message bubbles, and multimedia generators reference the exact same persistent digital human.
 */
object StassenIdentityRegistry {
    const val IDENTITY_NAME = "Stassen"
    const val IDENTITY_ROLE = "VYRO Canonical AI Companion"
    
    // The canonical high-resolution portrait reference of Stassen
    val PRIMARY_PORTRAIT_RES = R.drawable.img_stassen_avatar_1786868193397

    // Physical Identity Attributes defining Stassen's digital representation
    val PHYSICAL_IDENTITY = StassenPhysicalProfile(
        facialStructure = "Defined jawline, natural athletic build, warm approachable features",
        hairStyle = "Neat short tapered dark brown hair",
        eyeFeatures = "Focused dark expressive eyes with subtle smile lines",
        skinTone = "Natural warm olive tone matching reference photographs",
        attireStyles = mapOf(
            HouseArea.LIVING_ROOM to "Minimalist casual dark crewneck / smart lounge wear",
            HouseArea.OFFICE to "Modern fitted slate shirt with subtle VYRO engineering accents",
            HouseArea.STUDY to "Classic dark knit cardigan over structured collared shirt",
            HouseArea.PHONE_LOUNGE to "Relaxed obsidian tech hoodie",
            HouseArea.ENTERTAINMENT to "Casual graphite studio tee",
            HouseArea.REST_AREA to "Comfortable midnight dark lounge knit"
        )
    )

    /**
     * Checks if visual identity can be strictly preserved for a generation request.
     * If visual generation cannot guarantee the exact face match, returns clear limitation metadata.
     */
    fun checkIdentityPreservationGuarantee(hasExactFaceModel: Boolean): IdentityGuaranteeStatus {
        return if (hasExactFaceModel) {
            IdentityGuaranteeStatus.GUARANTEED
        } else {
            IdentityGuaranteeStatus.LIMITATION_INDICATED(
                reason = "Exact facial identity lock requires verified reference weights. Using canonical portrait reference to prevent visual distortion or unintended face alteration."
            )
        }
    }
}

data class StassenPhysicalProfile(
    val facialStructure: String,
    val hairStyle: String,
    val eyeFeatures: String,
    val skinTone: String,
    val attireStyles: Map<HouseArea, String>
)

sealed class IdentityGuaranteeStatus {
    object GUARANTEED : IdentityGuaranteeStatus()
    data class LIMITATION_INDICATED(val reason: String) : IdentityGuaranteeStatus()
}

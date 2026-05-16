package com.kaizen.khushu.data.model

/**
 * A single public reflection post from Quran Reflect (quranreflect.com).
 */
data class ReflectionPost(
    val id: Int,
    val verseKey: String,          // e.g. "1:1"
    val body: String,              // post text (may contain basic HTML)
    val bodyPlain: String,         // stripped plain text for preview
    val userName: String,
    val userHandle: String,
    val likeCount: Int,
    val reflectionCount: Int,
    val isVerified: Boolean = false,
    val publishedAt: String = "",  // ISO date string
)

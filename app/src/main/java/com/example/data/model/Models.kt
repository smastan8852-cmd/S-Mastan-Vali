package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val phoneNumber: String,
    val name: String,
    val isPro: Boolean = false,
    val proPlanExpiry: Long = 0L,
    val loginTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "creations")
data class CreationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompt: String,
    val modality: String, // "IMAGE", "VIDEO", "3D MODEL", "ANIMATION"
    val style: String,    // "Cinematic", "Animatic", "Cyberpunk", "Anime", "3D Render"
    val resultUrl: String,
    val ratio: String = "16:9",
    val resolution: String = "1080p",
    val soundEffects: String = "None",
    val timestamp: Long = System.currentTimeMillis(),
    val isProSpeed: Boolean = false,
    val isFavorite: Boolean = false
)

@Entity(tableName = "community_videos")
data class CommunityVideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val videoUrl: String,
    val thumbnailCoverUrl: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val isUploadedByUser: Boolean = false,
    val soundEffects: String = "Ambient",
    val style: String = "Cinematic",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "feedbacks")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userPhone: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

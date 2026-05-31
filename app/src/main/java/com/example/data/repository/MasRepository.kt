package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.ContentJson
import com.example.data.api.GeminiRequest
import com.example.data.api.GeminiRetrofitClient
import com.example.data.api.PartJson
import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MasRepository(
    private val userDao: UserDao,
    private val creationDao: CreationDao,
    private val communityVideoDao: CommunityVideoDao,
    private val chatMessageDao: ChatMessageDao,
    private val feedbackDao: FeedbackDao
) {
    val loggedInUser: Flow<UserEntity?> = userDao.getLoggedInUser()
    val creations: Flow<List<CreationEntity>> = creationDao.getAllCreations()
    val communityVideos: Flow<List<CommunityVideoEntity>> = communityVideoDao.getVideos()
    val chatMessages: Flow<List<ChatMessageEntity>> = chatMessageDao.getMessages()

    suspend fun seedInitialVideosIfEmpty() = withContext(Dispatchers.IO) {
        val count = communityVideoDao.getVideosCount()
        if (count == 0) {
            val initialList = listOf(
                CommunityVideoEntity(
                    title = "Cyberpunk Live Face-Swap",
                    author = "AI_Studio_Pro",
                    videoUrl = "https://example.com/videos/cyberpunk_swap.mp4",
                    thumbnailCoverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=500&auto=format&fit=crop&q=80",
                    likesCount = 2841,
                    isLiked = false,
                    isUploadedByUser = false,
                    soundEffects = "Cyberpunk Synth & Heavy Bass",
                    style = "Cyberpunk"
                ),
                CommunityVideoEntity(
                    title = "Astronaut Odyssey: Cinematic Voyage",
                    author = "Cosmic_Creator",
                    videoUrl = "https://example.com/videos/astronaut_odyssey.mp4",
                    thumbnailCoverUrl = "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=500&auto=format&fit=crop&q=80",
                    likesCount = 1943,
                    isLiked = true,
                    isUploadedByUser = false,
                    soundEffects = "Ambient Space Winds & Echoes",
                    style = "Cinematic"
                ),
                CommunityVideoEntity(
                    title = "Historical Renaissance Reimagined",
                    author = "ClassicAI_Swap",
                    videoUrl = "https://example.com/videos/renaissance_art.mp4",
                    thumbnailCoverUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500&auto=format&fit=crop&q=80",
                    likesCount = 1045,
                    isLiked = false,
                    isUploadedByUser = false,
                    soundEffects = "Classical Harpsichord & Reverb",
                    style = "Classic Painting"
                ),
                CommunityVideoEntity(
                    title = "Mecha Warrior 3D Animation Arena",
                    author = "SciFi_Render_Master",
                    videoUrl = "https://example.com/videos/mecha_warrior.mp4",
                    thumbnailCoverUrl = "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=500&auto=format&fit=crop&q=80",
                    likesCount = 3721,
                    isLiked = false,
                    isUploadedByUser = false,
                    soundEffects = "Heavy Metal Steps & Hydraulic Hiss",
                    style = "3D Render"
                ),
                CommunityVideoEntity(
                    title = "Serene Watercolor Anime Stream",
                    author = "Animatic_Dreamer",
                    videoUrl = "https://example.com/videos/anime_watercolor.mp4",
                    thumbnailCoverUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=500&auto=format&fit=crop&q=80",
                    likesCount = 1432,
                    isLiked = false,
                    isUploadedByUser = false,
                    soundEffects = "Piano Soft Lofi & Birds Chipping",
                    style = "Animatic"
                )
            )
            for (video in initialList) {
                communityVideoDao.insertVideo(video)
            }
        }
    }

    suspend fun loginOrRegister(phoneNumber: String, name: String) = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByPhone(phoneNumber)
        if (existing == null) {
            val newUser = UserEntity(
                phoneNumber = phoneNumber,
                name = name,
                isPro = false
            )
            userDao.insertUser(newUser)
        } else {
            val updated = existing.copy(loginTimestamp = System.currentTimeMillis())
            userDao.insertUser(updated)
        }
    }

    suspend fun upgradeToPro() = withContext(Dispatchers.IO) {
        val user = userDao.getLoggedInUserDirect()
        if (user != null) {
            val updated = user.copy(
                isPro = true,
                proPlanExpiry = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // 30 days
            )
            userDao.insertUser(updated)
        }
    }

    suspend fun downgradeToFree() = withContext(Dispatchers.IO) {
        val user = userDao.getLoggedInUserDirect()
        if (user != null) {
            val updated = user.copy(
                isPro = false,
                proPlanExpiry = 0L
            )
            userDao.insertUser(updated)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        userDao.clearUsers()
    }

    suspend fun addCreation(creation: CreationEntity) = withContext(Dispatchers.IO) {
        creationDao.insertCreation(creation)
    }

    suspend fun deleteCreation(id: Int) = withContext(Dispatchers.IO) {
        creationDao.deleteCreationById(id)
    }

    suspend fun uploadUserVideo(title: String, style: String, soundEffects: String) = withContext(Dispatchers.IO) {
        val user = userDao.getLoggedInUserDirect()
        val authorName = user?.name ?: "Guest AI Builder"
        // Select an Unsplash thumbnail matching style
        val thumbnail = when (style.lowercase()) {
            "cyberpunk" -> "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=500"
            "cinematic" -> "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=500"
            "3d render" -> "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=500"
            "animatic" -> "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=500"
            else -> "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=500" // abstract
        }
        val video = CommunityVideoEntity(
            title = title,
            author = authorName,
            videoUrl = "https://example.com/videos/user_uploaded_${System.currentTimeMillis()}.mp4",
            thumbnailCoverUrl = thumbnail,
            likesCount = 1,
            isLiked = true,
            isUploadedByUser = true,
            soundEffects = soundEffects,
            style = style
        )
        communityVideoDao.insertVideo(video)
    }

    suspend fun likeVideo(videoId: Int, isLiked: Boolean) = withContext(Dispatchers.IO) {
        val video = communityVideoDao.getVideoById(videoId)
        if (video != null) {
            val updated = video.copy(
                isLiked = isLiked,
                likesCount = if (isLiked) video.likesCount + 1 else video.likesCount - 1
            )
            communityVideoDao.updateVideo(updated)
        }
    }

    suspend fun submitFeedback(text: String) = withContext(Dispatchers.IO) {
        val user = userDao.getLoggedInUserDirect()
        val feedback = FeedbackEntity(
            userPhone = user?.phoneNumber ?: "Anonymous",
            text = text
        )
        feedbackDao.insertFeedback(feedback)
    }

    suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        chatMessageDao.clearHistory()
    }

    suspend fun analyzePromptKeywords(prompt: String): String = withContext(Dispatchers.IO) {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY" || key.contains("PLACEHOLDER") || key.contains("MY_")) {
            return@withContext extractBackupKeywords(prompt)
        }
        return@withContext try {
            val systemDoc = "Analyze the user's creative generation input prompt. Extract exactly 1 to 2 extremely precise, high-quality, English search keywords/tags representing the core subject. Return ONLY the comma-separated keywords (e.g., 'cyberpunk,car', 'golden-retriever', 'castle,forest', 'sunset,ocean'). Do not include formatting, periods, preamble, quotes, or notes."
            val request = GeminiRequest(
                contents = listOf(ContentJson(parts = listOf(PartJson(text = prompt)))),
                systemInstruction = ContentJson(parts = listOf(PartJson(text = systemDoc)))
            )
            val response = GeminiRetrofitClient.service.generateContent(key, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
            if (!text.isNullOrEmpty() && text.length < 50) {
                text.lowercase().replace(" ", "")
            } else {
                extractBackupKeywords(prompt)
            }
        } catch (e: Throwable) {
            Log.e("MasRepository", "Error analyzing prompt keywords: ${e.message}", e)
            extractBackupKeywords(prompt)
        }
    }

    fun extractBackupKeywords(prompt: String): String {
        return prompt.lowercase()
            .replace("[^a-zA-Z0-9 ]".toRegex(), "")
            .split("\\s+".toRegex())
            .filter { it.length > 2 && it != "create" && it != "generate" && it != "image" && it != "video" && it != "photo" && it != "picture" && it != "with" && it != "some" && it != "want" }
            .take(2)
            .joinToString(",")
    }

    suspend fun sendMessageToAI(userText: String) = withContext(Dispatchers.IO) {
        // First insert user message
        val userMsg = ChatMessageEntity(isUser = true, text = userText)
        chatMessageDao.insertMessage(userMsg)

        // Then get response
        val botText = getAIResponse(userText)
        val botMsg = ChatMessageEntity(isUser = false, text = botText)
        chatMessageDao.insertMessage(botMsg)
    }

    private suspend fun getAIResponse(prompt: String): String {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY" || key.contains("PLACEHOLDER") || key.contains("MY_")) {
            return generateOfflineResponse(prompt)
        }
        return try {
            val systemDoc = "You are Mas AI, a friendly creative assistant specializing in Image Creation, 3D Models, Video options, and cinematic Animatic styles. Welcome users, offer suggestions, and provide prompt improvements for generating high-quality works."
            val request = GeminiRequest(
                contents = listOf(ContentJson(parts = listOf(PartJson(text = prompt)))),
                systemInstruction = ContentJson(parts = listOf(PartJson(text = systemDoc)))
            )
            val response = GeminiRetrofitClient.service.generateContent(key, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "I apologize, but I could not formulate a response at this moment. Let's create some AI media together!"
        } catch (e: Throwable) {
            Log.e("MasRepository", "Gemini error: ${e.message}", e)
            generateOfflineResponse(prompt)
        }
    }

    private fun generateOfflineResponse(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            p.contains("video") || p.contains("watch") || p.contains("movie") -> {
                "🎬 *Mas AI Video Assistant:* Building videos is a Premium Pro feature of Mas AI. To generate videos, animations, and unlock 10x faster generation speeds, please click on the pro upgrade. Here's a tip: high-quality cinematic videos typically include specific details about camera angles, panning rates, and custom sound effects!"
            }
            p.contains("image") || p.contains("picture") || p.contains("photo") -> {
                "🎨 *Mas AI Image Creator:* You can create gorgeous, very high-quality images completely for free on Mas AI! Try using keywords like *'hyper-detailed 8k, cinematic key light, volumetric depth'* to get breathtaking results."
            }
            p.contains("3d") || p.contains("model") || p.contains("mesh") -> {
                "💎 *Mas AI 3D Mesh Engine:* Let's sculpt a 3D model. Simply prompt what you'd like and select '3D Model' above. For example: *'A futuristic hovercar sculpted out of titanium, high contrast cyberpunk mesh'*. Try compiling it!"
            }
            p.contains("swap") || p.contains("face") -> {
                "👥 *Face-Swap Mode:* Our show feed contains gorgeous ready-made AI creations demonstrating stunning face-swaps. You can browse them on the 'Showcase Feed' and press 'Swap My Face' to see the process! Additionally, you can upload your own works directly."
            }
            p.contains("hello") || p.contains("hi") || p.contains("hey") -> {
                "✨ Welcome to *Mas AI*! I am your AI creative guide. I can help you formulate the perfect prompt for Images, Cinematic Videos, 3D Models, or Animatic animations! What type of AI content are we creating today?"
            }
            p.contains("price") || p.contains("pro") || p.contains("fee") || p.contains("upgrade") -> {
                "💎 *Mas AI Pro Plan:* To upgrade to Pro and access rapid synthesis, longer cinematic video tools, and custom sound effects, please visit the Profile section and activate your plan. It starts at a simple flat fee and unlocks infinite potential!"
            }
            else -> {
                "💡 *Mas AI tip:* Try using exact description modifiers! For instance, if you want something in *Cinematic* or *Animatic* mode, specify the style tokens. I am here to help you draft prompt guidelines. Is there anything specific you would like to know?"
            }
        }
    }
}

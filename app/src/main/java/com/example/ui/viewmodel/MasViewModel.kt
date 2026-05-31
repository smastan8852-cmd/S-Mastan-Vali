package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CreationEntity
import com.example.data.model.UserEntity
import com.example.data.repository.MasRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MasViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MasRepository(
        userDao = database.userDao(),
        creationDao = database.creationDao(),
        communityVideoDao = database.communityVideoDao(),
        chatMessageDao = database.chatMessageDao(),
        feedbackDao = database.feedbackDao()
    )

    // Observables from Repo
    val loggedInUser: StateFlow<UserEntity?> = repository.loggedInUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userCreations = repository.creations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityVideos = repository.communityVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Core Inputs ---
    var creativePrompt = MutableStateFlow("")
    var selectedModality = MutableStateFlow("IMAGE") // IMAGE, VIDEO, 3D MODEL, ANIMATION
    var selectedStyle = MutableStateFlow("Cinematic")   // Cinematic, Animatic, Cyberpunk, Anime, 3D Render
    var selectedRatio = MutableStateFlow("16:9")     // 16:9, 21:9, 9:16, 1:1
    var selectedResolution = MutableStateFlow("1080p") // 1080p, 4K UHD, 2K QHD
    var includeSoundEffects = MutableStateFlow(false)
    var soundEffectsPrompt = MutableStateFlow("Ambient sci-fi breeze")

    // Auth screen inputs
    var loginPhone = MutableStateFlow("")
    var loginPassword = MutableStateFlow("")
    var registerName = MutableStateFlow("")
    var isLoginMode = MutableStateFlow(true)
    var authError = MutableStateFlow<String?>(null)

    // Chat Screen Inputs
    var chatInput = MutableStateFlow("")
    var isSendingChat = MutableStateFlow(false)

    // Profile & Support Inputs
    var feedbackInput = MutableStateFlow("")
    var showUpgradeDialog = MutableStateFlow(false)
    var showDemoFaceSwapMode = MutableStateFlow(false)

    // Generation State Engine
    var isGenerating = MutableStateFlow(false)
    var generationProgress = MutableStateFlow(0)
    var currentProgressText = MutableStateFlow("")
    var lastGeneratedCreation = MutableStateFlow<CreationEntity?>(null)

    init {
        // Seed initial community ai videos if DB is empty
        viewModelScope.launch {
            repository.seedInitialVideosIfEmpty()
        }
    }

    fun setModality(modality: String) {
        selectedModality.value = modality
    }

    fun setStyle(style: String) {
        selectedStyle.value = style
    }

    // --- Authentication Actions ---
    fun loginOrRegister(onSuccess: () -> Unit) {
        val phone = loginPhone.value.trim()
        val password = loginPassword.value.trim()
        val name = if (isLoginMode.value) "" else registerName.value.trim()

        if (phone.isEmpty() || password.isEmpty()) {
            authError.value = "Phone number and password are required."
            return
        }
        if (!isLoginMode.value && name.isEmpty()) {
            authError.value = "Name is required for registration."
            return
        }

        viewModelScope.launch {
            try {
                repository.loginOrRegister(phone, if (isLoginMode.value) "AI Developer" else name)
                authError.value = null
                onSuccess()
            } catch (e: Throwable) {
                authError.value = "Authentication error: ${e.message}"
            }
        }
    }

    fun logout(onCompleted: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            loginPhone.value = ""
            loginPassword.value = ""
            registerName.value = ""
            onCompleted()
        }
    }

    // --- Support & Feed Actions ---
    fun submitFeedback() {
        val text = feedbackInput.value.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            repository.submitFeedback(text)
            feedbackInput.value = ""
        }
    }

    fun uploadAIWork(title: String, style: String, soundEffects: String) {
        viewModelScope.launch {
            repository.uploadUserVideo(title, style, soundEffects)
        }
    }

    fun likeVideo(id: Int, isLiked: Boolean) {
        viewModelScope.launch {
            repository.likeVideo(id, isLiked)
        }
    }

    // --- AI Assistant Chat ---
    fun sendMessageToAI() {
        val prompt = chatInput.value.trim()
        if (prompt.isEmpty()) return
        chatInput.value = ""
        isSendingChat.value = true
        viewModelScope.launch {
            repository.sendMessageToAI(prompt)
            isSendingChat.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    // --- Pro Subscription Plan ---
    fun purchasePro() {
        viewModelScope.launch {
            repository.upgradeToPro()
            showUpgradeDialog.value = false
        }
    }

    fun testDowngradePro() {
        viewModelScope.launch {
            repository.downgradeToFree()
        }
    }

    fun deleteCreation(id: Int) {
        viewModelScope.launch {
            repository.deleteCreation(id)
        }
    }

    // --- Generator State Engine ---
    private suspend fun getDynamicUrlForPrompt(prompt: String, modality: String, style: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("cat") -> "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=800&auto=format&fit=crop&q=80"
            lower.contains("dog") || lower.contains("puppy") -> "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=800&auto=format&fit=crop&q=80"
            lower.contains("car") || lower.contains("vehicle") || lower.contains("auto") -> "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&auto=format&fit=crop&q=80"
            lower.contains("space") || lower.contains("mars") || lower.contains("galaxy") || lower.contains("astronaut") || lower.contains("star") -> "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80"
            lower.contains("city") || lower.contains("tokyo") || lower.contains("cyber") || lower.contains("cyberpunk") || lower.contains("street") -> "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=800&auto=format&fit=crop&q=80"
            lower.contains("mountain") || lower.contains("nature") || lower.contains("landscape") || lower.contains("scenery") -> "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&auto=format&fit=crop&q=80"
            lower.contains("ocean") || lower.contains("sea") || lower.contains("beach") || lower.contains("water") || lower.contains("river") -> "https://images.unsplash.com/photo-1505118380757-91f5f5632de0?w=800&auto=format&fit=crop&q=80"
            lower.contains("anime") || lower.contains("drawing") || lower.contains("illustration") || lower.contains("sketch") -> "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&auto=format&fit=crop&q=80"
            lower.contains("dragon") || lower.contains("monster") || lower.contains("fantasy") -> "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80"
            lower.contains("flower") || lower.contains("rose") || lower.contains("forest") || lower.contains("garden") || lower.contains("tree") -> "https://images.unsplash.com/photo-1465146344425-f00d5f5c8f07?w=800&auto=format&fit=crop&q=80"
            lower.contains("futuristic") || lower.contains("tech") || lower.contains("robot") || lower.contains("cyborg") -> "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=800&auto=format&fit=crop&q=80"
            else -> {
                val analyzed = repository.analyzePromptKeywords(prompt)
                if (analyzed.isNotEmpty()) {
                    "https://loremflickr.com/800/450/$analyzed"
                } else {
                    when (modality) {
                        "IMAGE" -> {
                            when (style) {
                                "Cinematic" -> "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&auto=format&fit=crop&q=80"
                                "Animatic" -> "https://images.unsplash.com/photo-1620641788421-7a1c342ea42e?w=800&auto=format&fit=crop&q=80"
                                "Cyberpunk" -> "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&auto=format&fit=crop&q=80"
                                "Anime" -> "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&auto=format&fit=crop&q=80"
                                else -> "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80"
                            }
                        }
                        else -> {
                            when (style) {
                                "Cinematic" -> "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=800&auto=format&fit=crop&q=80"
                                "Animatic" -> "https://images.unsplash.com/photo-1518156677180-95a2893f3e9f?w=800&auto=format&fit=crop&q=80"
                                "Cyberpunk" -> "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=800&auto=format&fit=crop&q=80"
                                "Anime" -> "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=800&auto=format&fit=crop&q=80"
                                else -> "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80"
                            }
                        }
                    }
                }
            }
        }
    }

    fun startAISynthesis() {
        val prompt = creativePrompt.value.trim()
        if (prompt.isEmpty()) return

        val user = loggedInUser.value
        val isUserPro = user?.isPro == true

        // Enforce either IMAGE or VIDEO based on what the user says
        val lowerPrompt = prompt.lowercase()
        var modality = selectedModality.value
        if (lowerPrompt.contains("video") || lowerPrompt.contains("clip") || lowerPrompt.contains("movie") || lowerPrompt.contains("film") || lowerPrompt.contains("recording") || lowerPrompt.contains("animation") || lowerPrompt.contains("loop")) {
            modality = "VIDEO"
            selectedModality.value = "VIDEO"
        } else if (lowerPrompt.contains("image") || lowerPrompt.contains("pic") || lowerPrompt.contains("picture") || lowerPrompt.contains("photo") || lowerPrompt.contains("drawing") || lowerPrompt.contains("painting") || lowerPrompt.contains("landscape") || lowerPrompt.contains("portrait")) {
            modality = "IMAGE"
            selectedModality.value = "IMAGE"
        } else {
            // Force it to be either IMAGE or VIDEO depending on current selection, defaulting to IMAGE
            if (modality != "IMAGE" && modality != "VIDEO") {
                modality = "IMAGE"
                selectedModality.value = "IMAGE"
            }
        }

        // Video must be pay/Pro to generate
        if (modality == "VIDEO" && !isUserPro) {
            showUpgradeDialog.value = true
            return
        }

        viewModelScope.launch {
            isGenerating.value = true
            generationProgress.value = 0
            currentProgressText.value = "Establishing contact with synthesis servers..."

            val steps = listOf(
                "Initializing latent noise models..." to 15,
                "Parsing visual instructions vectors..." to 35,
                "Baking style maps [${selectedStyle.value}]..." to 55,
                "Synthesizing high fidelity tensor matrices..." to 75,
                if (modality == "VIDEO") "Encoding custom audio sound signatures..." to 85 else "Optimizing texture scale ratios..." to 85,
                "Compiling creative layers..." to 95,
                "Denoising completed..." to 100
            )

            // Speed factor: 10x faster for Pro users!
            val delayMultiplier = if (isUserPro) 40L else 300L

            for ((text, pct) in steps) {
                currentProgressText.value = text
                while (generationProgress.value < pct) {
                    delay(delayMultiplier)
                    generationProgress.value += 1
                }
            }

            // High-quality content dynamically determined
            val finalUrl = getDynamicUrlForPrompt(prompt, modality, selectedStyle.value)

            val creation = CreationEntity(
                prompt = prompt,
                modality = modality,
                style = selectedStyle.value,
                resultUrl = finalUrl,
                ratio = selectedRatio.value,
                resolution = selectedResolution.value,
                soundEffects = if (includeSoundEffects.value) soundEffectsPrompt.value else "None",
                isProSpeed = isUserPro
            )

            repository.addCreation(creation)
            lastGeneratedCreation.value = creation
            isGenerating.value = false
            creativePrompt.value = "" // clear prompt
        }
    }
}

package com.example

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

// Data models
data class FamilyMember(
    val id: String,
    val name: String,
    val role: String,
    val avatarIndex: Int,
    val mood: String,
    val colorHex: String
)

data class PostComment(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val avatarIndex: Int,
    val text: String,
    val timestamp: String = "Just now"
)

data class FamilyPost(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val avatarIndex: Int,
    val content: String,
    val imageUrl: String? = null,
    val likes: Int = 0,
    val comments: List<PostComment> = emptyList(),
    val timestamp: String = "Just now"
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val avatarIndex: Int,
    val text: String,
    val timestamp: String = "Just now"
)

class MyFamilyViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("myfamily_prefs", Context.MODE_PRIVATE)

    // Dark Mode Local state
    private val _isDarkMode = MutableStateFlow(true) // Default to dark for premium look
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Onboarding Locked States
    private val _isOnboarded = MutableStateFlow(prefs.getBoolean("onboarded", false))
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    private val _familyId = MutableStateFlow(prefs.getString("family_id", "") ?: "")
    val familyId: StateFlow<String> = _familyId.asStateFlow()

    private val _nickname = MutableStateFlow(prefs.getString("nickname", "") ?: "")
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    private val _avatarIndex = MutableStateFlow(prefs.getInt("avatar_index", 0))
    val avatarIndex: StateFlow<Int> = _avatarIndex.asStateFlow()

    // Real-time Social Feed
    private val _posts = MutableStateFlow<List<FamilyPost>>(emptyList())
    val posts: StateFlow<List<FamilyPost>> = _posts.asStateFlow()

    // Collaborative Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Family Members
    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMember>> = _familyMembers.asStateFlow()

    init {
        initializeData()
    }

    private fun initializeData() {
        _familyMembers.value = listOf(
            FamilyMember("1", "Mummy", "Queen", 1, "🍳 Making Dinner", "#F43F5E"),
            FamilyMember("2", "Papa", "Captain", 0, "💼 At Office", "#6366F1"),
            FamilyMember("3", "Chintu", "Ninja", 2, "🎮 Level Up", "#22D3EE")
        )

        _posts.value = listOf(
            FamilyPost(
                sender = "Mummy",
                avatarIndex = 1,
                content = "Just baked some fresh chocolate chip cookies for everyone! 🍪✨",
                likes = 3,
                comments = listOf(
                    PostComment(sender = "Chintu", avatarIndex = 2, text = "Save some for me!! 🤤")
                )
            ),
            FamilyPost(
                sender = "Papa",
                avatarIndex = 0,
                content = "Found this old photo from our last trip. Miss those mountains! 🏔️",
                likes = 5
            )
        )

        _chatMessages.value = listOf(
            ChatMessage(sender = "System", avatarIndex = 5, text = "Welcome to your private family room! 🏡")
        )
    }

    fun loginAndEnter(fid: String, nick: String, avatarIdx: Int) {
        prefs.edit().apply {
            putString("family_id", fid)
            putString("nickname", nick)
            putInt("avatar_index", avatarIdx)
            putBoolean("onboarded", true)
        }.apply()

        _familyId.value = fid
        _nickname.value = nick
        _avatarIndex.value = avatarIdx
        _isOnboarded.value = true
    }

    fun logOutAndReset() {
        prefs.edit().clear().apply()
        _isOnboarded.value = false
    }

    fun createPost(content: String, imageUrl: String? = null) {
        val newPost = FamilyPost(
            sender = _nickname.value,
            avatarIndex = _avatarIndex.value,
            content = content,
            imageUrl = imageUrl
        )
        _posts.value = listOf(newPost) + _posts.value
    }

    fun addComment(postId: String, text: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newComment = PostComment(
                    sender = _nickname.value,
                    avatarIndex = _avatarIndex.value,
                    text = text
                )
                post.copy(comments = post.comments + newComment)
            } else post
        }
    }

    fun likePost(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) post.copy(likes = post.likes + 1) else post
        }
    }

    fun sendChatMessage(text: String) {
        val newMsg = ChatMessage(
            sender = _nickname.value,
            avatarIndex = _avatarIndex.value,
            text = text
        )
        _chatMessages.value = _chatMessages.value + newMsg
    }
}

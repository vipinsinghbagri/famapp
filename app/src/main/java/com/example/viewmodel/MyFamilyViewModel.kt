package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.models.*
import com.example.data.repository.LocalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class MyFamilyViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "family-db"
    ).build()

    private val repository = LocalRepository(db.familyDao(), db.socialDao())
    private val prefs = application.getSharedPreferences("family_prefs", Context.MODE_PRIVATE)

    // Current User Logic
    private val _currentUserProfile = MutableStateFlow<Profile?>(null)
    val currentUserProfile = _currentUserProfile.asStateFlow()

    private val _isOnboarded = MutableStateFlow(false)
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    // Social Data
    private val _posts = MutableStateFlow<List<FamilyPost>>(emptyList())
    val posts: StateFlow<List<FamilyPost>> = _posts.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMember>> = _familyMembers.asStateFlow()

    // Dark Mode
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        loadSession()
    }

    private fun loadSession() {
        val userId = prefs.getString("user_id", null)
        if (userId != null) {
            viewModelScope.launch {
                val profile = repository.getProfile(userId)
                if (profile != null) {
                    _currentUserProfile.value = profile
                    _isOnboarded.value = true
                    if (profile.familyId != null) {
                        observeData(profile.familyId)
                    }
                }
            }
        }
    }

    private fun observeData(familyId: String) {
        viewModelScope.launch {
            repository.getPosts(familyId).collect { _posts.value = it }
        }
        viewModelScope.launch {
            repository.getMessages(familyId).collect { _chatMessages.value = it }
        }
        // Initialize dummy members for visual feel in family tab
        _familyMembers.value = listOf(
            FamilyMember("1", "Mummy", "Queen", 1, "🍳 Making Dinner", "#F43F5E"),
            FamilyMember("2", "Papa", "Captain", 0, "💼 At Office", "#6366F1")
        )
    }

    fun signUp(nickname: String, avatarIndex: Int) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val profile = Profile(id, null, nickname, avatarIndex)
            repository.saveProfile(profile)
            prefs.edit().putString("user_id", id).apply()
            _currentUserProfile.value = profile
            _isOnboarded.value = true
        }
    }

    fun createFamily(name: String) {
        val user = _currentUserProfile.value ?: return
        viewModelScope.launch {
            val family = repository.createFamily(name, user.id)
            val updatedProfile = repository.getProfile(user.id)
            _currentUserProfile.value = updatedProfile
            if (updatedProfile?.familyId != null) {
                observeData(updatedProfile.familyId)
            }
        }
    }

    fun joinFamily(inviteCode: String) {
        val user = _currentUserProfile.value ?: return
        viewModelScope.launch {
            val success = repository.joinFamily(inviteCode, user.id)
            if (success) {
                val updatedProfile = repository.getProfile(user.id)
                _currentUserProfile.value = updatedProfile
                if (updatedProfile?.familyId != null) {
                    observeData(updatedProfile.familyId)
                }
            }
        }
    }

    fun createPost(content: String) {
        val user = _currentUserProfile.value ?: return
        val familyId = user.familyId ?: return
        viewModelScope.launch {
            val post = FamilyPost(
                familyId = familyId,
                authorId = user.id,
                sender = user.nickname,
                avatarIndex = user.avatarIndex,
                content = content
            )
            repository.createPost(post)
        }
    }

    fun sendChatMessage(text: String) {
        val user = _currentUserProfile.value ?: return
        val familyId = user.familyId ?: return
        viewModelScope.launch {
            val msg = ChatMessage(
                familyId = familyId,
                senderId = user.id,
                sender = user.nickname,
                avatarIndex = user.avatarIndex,
                text = text,
                timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            )
            repository.sendMessage(msg)
        }
    }

    fun logOutAndReset() {
        prefs.edit().clear().apply()
        _isOnboarded.value = false
        _currentUserProfile.value = null
        _posts.value = emptyList()
        _chatMessages.value = emptyList()
    }
}

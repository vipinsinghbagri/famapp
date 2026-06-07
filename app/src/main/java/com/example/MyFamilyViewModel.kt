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

data class FamilyTask(
    val id: String,
    val title: String,
    val assignee: String,
    val dueDate: String,
    val isCompleted: Boolean = false,
    val createdBy: String = "AI Assistant"
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val avatarIndex: Int,
    val text: String,
    val isPhoto: Boolean = false,
    val photoType: Int = 0, // For drawing different canvas pictures in the Polaroid
    val photoLabel: String = "",
    val timestamp: String = "Just now"
)

class MyFamilyViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("myfamily_prefs", Context.MODE_PRIVATE)

    // Dark Mode Local state
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
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

    // Active User Mood state
    private val _userMood = MutableStateFlow("😀 Feeling Wonderful")
    val userMood: StateFlow<String> = _userMood.asStateFlow()

    // Live Family Members Grid (Mock Members)
    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMember>> = _familyMembers.asStateFlow()

    // Tasks list
    private val _tasks = MutableStateFlow<List<FamilyTask>>(emptyList())
    val tasks: StateFlow<List<FamilyTask>> = _tasks.asStateFlow()

    // Chat Feed
    private val _chatFeed = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatFeed: StateFlow<List<ChatMessage>> = _chatFeed.asStateFlow()

    // AI digest and parser feedback message text
    private val _aiDigest = MutableStateFlow("")
    val aiDigest: StateFlow<String> = _aiDigest.asStateFlow()

    private val _aiFeedbackMessage = MutableStateFlow<String?>(null)
    val aiFeedbackMessage: StateFlow<String?> = _aiFeedbackMessage.asStateFlow()

    init {
        // Load initial mock data
        initializeMockData()
        updateAiDigest()
    }

    private fun initializeMockData() {
        _familyMembers.value = listOf(
            FamilyMember("1", "Papa", "Provider", 0, "💼 At Work", "#DCFD8B"), // Lime
            FamilyMember("2", "Mummy", "Planner", 1, "🍳 Baking Cake", "#FDD5BD"), // Peach
            FamilyMember("3", "Chintu", "Explorer", 2, "🎮 Playing FIFA", "#BC84ee"), // Lavender
            FamilyMember("4", "Dadi", "Guide", 3, "📖 Reading Book", "#FFD6E8") // Pink
        )

        _tasks.value = listOf(
            FamilyTask("t1", "Buy organic milk", "Mummy", "Tonight"),
            FamilyTask("t2", "Fix the living room shelf", "Papa", "This Weekend"),
            FamilyTask("t3", "Complete homework assignment", "Chintu", "Tomorrow"),
            FamilyTask("t4", "Drink lukewarm tea", "Dadi", "Tonight", true)
        )

        _chatFeed.value = listOf(
            ChatMessage("c1", "Mummy", 1, "Dinner plan: Home-baked Pasta & Garlic Bread tonight! Who's in? 🍝"),
            ChatMessage("c2", "Papa", 0, "I shared a photo of the sky from the office garden", true, 1, "Sunny Garden View", "2 hours ago"),
            ChatMessage("c3", "Chintu", 2, "Can Papa help me with science project tomorrow? 🚀"),
            ChatMessage("c4", "Dadi", 3, "God bless our beautiful home. Chintu, please bring me warm tea.", false, 0, "", "3 hours ago")
        )
    }

    fun toggleDarkMode() {
        val newVal = !_isDarkMode.value
        _isDarkMode.value = newVal
        prefs.edit().putBoolean("dark_mode", newVal).apply()
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
        
        updateAiDigest()
    }

    fun logOutAndReset() {
        prefs.edit().clear().apply()
        _familyId.value = ""
        _nickname.value = ""
        _avatarIndex.value = 0
        _isOnboarded.value = false
        _userMood.value = "😀 Feeling Wonderful"
        initializeMockData()
        updateAiDigest()
    }

    fun updateOwnMood(newMood: String) {
        _userMood.value = newMood
        updateAiDigest()
    }

    fun updateFamilyMemberMood(memberId: String, newMood: String) {
        _familyMembers.value = _familyMembers.value.map {
            if (it.id == memberId) it.copy(mood = newMood) else it
        }
        updateAiDigest()
    }

    fun toggleTaskCompletion(taskId: String) {
        _tasks.value = _tasks.value.map {
            if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
        }
        updateAiDigest()
    }

    fun deleteTask(taskId: String) {
        _tasks.value = _tasks.value.filter { it.id != taskId }
        updateAiDigest()
    }

    fun addChatMessage(text: String, isPhoto: Boolean = false, photoType: Int = 0, photoLabel: String = "") {
        val nick = _nickname.value.ifEmpty { "You" }
        val avatarIdx = _avatarIndex.value
        val newMsg = ChatMessage(
            sender = nick,
            avatarIndex = avatarIdx,
            text = text,
            isPhoto = isPhoto,
            photoType = photoType,
            photoLabel = photoLabel,
            timestamp = "Just now"
        )
        // Add to top of feed
        _chatFeed.value = listOf(newMsg) + _chatFeed.value
        updateAiDigest()
    }

    fun parseVoiceCommand(commandText: String) {
        val lower = commandText.lowercase()
        var assignee = "Everyone"
        var cleanActivity = commandText
        var due = "Tonight"

        // Rule-based parsing
        if (lower.contains("papa") || lower.contains("dad") || lower.contains("father")) {
            assignee = "Papa"
        } else if (lower.contains("mummy") || lower.contains("mom") || lower.contains("mother")) {
            assignee = "Mummy"
        } else if (lower.contains("chintu") || lower.contains("bro") || lower.contains("junior")) {
            assignee = "Chintu"
        } else if (lower.contains("dadi") || lower.contains("grandma") || lower.contains("nari")) {
            assignee = "Dadi"
        } else if (lower.contains("me") || lower.contains("self") || lower.contains("i ")) {
            assignee = _nickname.value.ifEmpty { "You" }
        }

        // Clean command prefix
        val prefixes = listOf(
            "remind papa to", "remind mummy to", "remind chintu to", "remind dadi to", "remind me to",
            "tell papa to", "tell mummy to", "tell chintu to", "tell dadi to", "please remind",
            "ask papa to", "ask mummy to", "ask chintu to", "ask dadi to", "create task for",
            "remind to", "add task", "remind"
        )

        for (prefix in prefixes) {
            if (cleanActivity.lowercase().startsWith(prefix)) {
                cleanActivity = cleanActivity.substring(prefix.length).trim()
                break
            }
        }

        // Parse due date
        if (lower.contains("tonight")) {
            due = "Tonight"
            cleanActivity = cleanActivity.replace("tonight", "", ignoreCase = true).trim()
        } else if (lower.contains("tomorrow")) {
            due = "Tomorrow"
            cleanActivity = cleanActivity.replace("tomorrow", "", ignoreCase = true).trim()
        } else if (lower.contains("this weekend")) {
            due = "This Weekend"
            cleanActivity = cleanActivity.replace("this weekend", "", ignoreCase = true).trim()
        } else if (lower.contains("next week")) {
            due = "Next Week"
            cleanActivity = cleanActivity.replace("next week", "", ignoreCase = true).trim()
        }

        // Clean lingering punctuation
        cleanActivity = cleanActivity.trim(',', '.', '!', '?')
        if (cleanActivity.isEmpty()) {
            cleanActivity = "Do helper chores"
        }

        // Capitalize action
        val taskTitle = cleanActivity.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        // Create new task
        val newTask = FamilyTask(
            id = UUID.randomUUID().toString(),
            title = taskTitle,
            assignee = assignee,
            dueDate = due,
            isCompleted = false,
            createdBy = nickname.value.ifEmpty { "User" }
        )

        _tasks.value = listOf(newTask) + _tasks.value
        _aiFeedbackMessage.value = "🤖 AI Parsed:\nAssigned to **$assignee**\nTask: **$taskTitle**\nDue: **$due**"
        
        updateAiDigest()
    }

    fun clearFeedback() {
        _aiFeedbackMessage.value = null
    }

    fun updateAiDigest() {
        val activeTasks = _tasks.value.filter { !it.isCompleted }
        val completedCount = _tasks.value.count { it.isCompleted }
        val totalCount = _tasks.value.size
        
        // Find if someone has custom moods
        val currentMoodCount = _familyMembers.value.size
        val latestMsg = _chatFeed.value.firstOrNull()
        
        val chatText = when {
            latestMsg == null -> "A quiet peaceful day."
            latestMsg.isPhoto -> "${latestMsg.sender} shared a Polaroid: \"${latestMsg.photoLabel}\""
            else -> "${latestMsg.sender} shared: \"${latestMsg.text.take(30)}...\""
        }

        val myMood = _userMood.value
        val listSize = activeTasks.size
        
        _aiDigest.value = "🤖 AI Daily Digest: Currently $listSize pending items. Mummy shared dinner food ideas. Papa is at work. You updated mood to \"$myMood\". Latest updates: $chatText"
    }
}

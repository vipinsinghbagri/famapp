package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MyFamilyViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyFamilyApp(viewModel = viewModel)
                }
            }
        }
    }
}

// Available 6 Avatars (emojis, labels, and beautiful pastel backdrop colors)
data class AvatarPreset(val emoji: String, val label: String, val color: Color)

val avatarPresets = listOf(
    AvatarPreset("👨", "Papa", Color(0xFFE0F2FE)), // soft blue
    AvatarPreset("👩", "Mummy", Color(0xFFFEE2E2)), // soft peach/orange
    AvatarPreset("👶", "Chintu", Color(0xFFF3E8FF)), // soft lavender
    AvatarPreset("👵", "Dadi", Color(0xFFFCE7F3)), // soft pink
    AvatarPreset("🐹", "🐹", Color(0xFFFEF9C3)), // soft yellow
    AvatarPreset("✨", "Me", Color(0xFFDCFCE7)) // soft green
)

// List of popular quick mood options
val moodOptions = listOf(
    "😊 Feeling Wonderful",
    "💼 At Work",
    "🍳 Baking Cake",
    "🎮 Playing Games",
    "📚 Studying hard",
    "😴 Sleepy Zzz",
    "🧘 Meditating",
    "🏋 Exercising",
    "🍕 Ordering Pizza"
)

// List of simulated photo options for attachments
data class MockPhotoPreset(val type: Int, val label: String, val text: String)
val photoPresets = listOf(
    MockPhotoPreset(0, "Delicious Pasta 🍝", "Cooked warm dinner for everyone!"),
    MockPhotoPreset(1, "Sunny Sky View 🌅", "Golden office garden sunrise!"),
    MockPhotoPreset(2, "New Toys 🧸", "Look what I gathered for Chintu!"),
    MockPhotoPreset(3, "Lazy Cat 🐱", "Snuck into Dadi's shawl wool!")
)

@Composable
fun MyFamilyApp(viewModel: MyFamilyViewModel) {
    val isOnboarded by viewModel.isOnboarded.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (!isOnboarded) {
            OnboardingScreen(
                onJoinSpace = { fid, nick, avatarIndex ->
                    viewModel.loginAndEnter(fid, nick, avatarIndex)
                }
            )
        } else {
            DashboardScreen(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onJoinSpace: (String, String, Int) -> Unit) {
    var familyId by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var selectedAvatarIndex by remember { mutableIntStateOf(0) }
    
    val context = LocalContext.current
    
    // Animate onboarding screen entrance
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        WarmCream,
                        Color(0xFFFFF2D8),
                        Color(0xFFF3E8FF)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = spring()) + slideInVertically(
                initialOffsetY = { 200 },
                animationSpec = spring()
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 6.dp, end = 6.dp)
                    .neobrutalistShadow(
                        color = CharcoalDark,
                        cornerRadius = 28.dp,
                        offset = 6.dp
                    )
                    .border(3.dp, CharcoalDark, RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header logo / title
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(PastelLime, CircleShape)
                            .border(3.dp, CharcoalDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Family logo",
                            tint = CharcoalDark,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "MyFamily Space 🏡",
                        style = MaterialTheme.typography.displayMedium,
                        color = CharcoalDark,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "Your private, beautiful digital living room",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Family ID Block
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Family Space ID (6 Characters)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = familyId,
                                onValueChange = { if (it.length <= 15) familyId = it.uppercase() },
                                placeholder = { Text("E.g. FAM-77") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SoftLavender,
                                    unfocusedBorderColor = CharcoalDark.copy(alpha = 0.5f),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("onboarding_family_id_input")
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = {
                                    val randomCode = ('A'..'Z').shuffled().take(3).joinToString("") +
                                            "-" + (100..999).random().toString()
                                    familyId = randomCode
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PastelLime,
                                    contentColor = CharcoalDark
                                ),
                                border = BorderStroke(2.dp, CharcoalDark),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("Auto", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // User Nickname Block
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Your Nickname",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            placeholder = { Text("Mummy, Papa, Chintu...") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SoftLavender,
                                unfocusedBorderColor = CharcoalDark.copy(alpha = 0.5f),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_nickname_input")
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Avatar Picker Block
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Choose Your Avatar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(avatarPresets) { idx, item ->
                                val isSelected = idx == selectedAvatarIndex
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1.3f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(item.color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) CharcoalDark else CharcoalDark.copy(
                                                alpha = 0.2f
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { selectedAvatarIndex = idx }
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(item.emoji, fontSize = 28.sp)
                                        Text(
                                            item.label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(30.dp))
                    
                    // Start Button
                    Button(
                        onClick = {
                            if (familyId.trim().length < 3) {
                                Toast.makeText(context, "Please enter or generate a Family ID!", Toast.LENGTH_SHORT).show()
                            } else if (nickname.trim().isEmpty()) {
                                Toast.makeText(context, "Please enter your Nickname!", Toast.LENGTH_SHORT).show()
                            } else {
                                onJoinSpace(familyId.trim(), nickname.trim(), selectedAvatarIndex)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SoftLavender,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(3.dp, CharcoalDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .testTag("lock_and_enter_button")
                    ) {
                        Text(
                            text = "Lock & Enter Space 🔑",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CharcoalDark
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(viewModel: MyFamilyViewModel) {
    val familyId by viewModel.familyId.collectAsState()
    val nickname by viewModel.nickname.collectAsState()
    val avatarIndex by viewModel.avatarIndex.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val chatFeed by viewModel.chatFeed.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()
    val userMood by viewModel.userMood.collectAsState()
    val aiDigest by viewModel.aiDigest.collectAsState()
    val aiFeedbackMessage by viewModel.aiFeedbackMessage.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    
    val myAvatar = avatarPresets[avatarIndex.coerceIn(0, avatarPresets.size - 1)]
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    // Dialog control states
    var showMoodDialog by remember { mutableStateOf(false) }
    var selectedMemberForMoodChange by remember { mutableStateOf<FamilyMember?>(null) }
    var showSimulatedPhotoDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDarkMode) DarkBg else WarmCream)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Profile Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showMoodDialog = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(myAvatar.color, RoundedCornerShape(14.dp))
                                .border(2.dp, if (isDarkMode) Color.White else CharcoalDark, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(myAvatar.emoji, fontSize = 24.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Column {
                            Text(
                                text = nickname,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else CharcoalDark
                            )
                            Text(
                                text = userMood,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDarkMode) SoftLavender else CharcoalMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    // Logo and Toggles
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dark mode toggle
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (isDarkMode) CharcoalDark else Color.White,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    2.dp,
                                    if (isDarkMode) Color.White.copy(0.4f) else CharcoalDark,
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                contentDescription = "Toggle Dark Mode",
                                tint = if (isDarkMode) Color.Yellow else CharcoalDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        // Log out/reset
                        IconButton(
                            onClick = {
                                viewModel.logOutAndReset()
                                Toast.makeText(context, "Space Reset Successful!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (isDarkMode) CharcoalDark else Color.White,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    2.dp,
                                    if (isDarkMode) Color.White.copy(0.4f) else CharcoalDark,
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Log Out",
                                tint = if (isDarkMode) Color.White else CharcoalDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // MyFamily Hub Title & Space Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "MyFamily Hub 🏡",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = if (isDarkMode) Color.White else CharcoalDark
                    )
                    
                    // Space Copyable Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PastelLime)
                            .border(2.dp, CharcoalDark, RoundedCornerShape(12.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(familyId))
                                Toast.makeText(context, "ID copied to Clipboard: $familyId", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share code",
                                tint = CharcoalDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ID: $familyId",
                                fontWeight = FontWeight.ExtraBold,
                                color = CharcoalDark,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Speech Task parsing AI assistant floating box
            VoiceTaskOverlaySimulated(
                viewModel = viewModel,
                isDarkMode = isDarkMode
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkMode) DarkBg else WarmCream)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Item 1: AI Generated Daily Brief Summary Box
            item {
                Spacer(modifier = Modifier.height(4.dp))
                AIDigestCardSection(aiDigest = aiDigest, isDarkMode = isDarkMode)
            }
            
            // Item 2: Live Family Cards Grid Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Realtime Family Core ✨",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDarkMode) Color.White else CharcoalDark
                    )
                    Text(
                        text = "(Tap cards to toggle status)",
                        fontSize = 11.sp,
                        color = if (isDarkMode) Color.White.copy(0.6f) else CharcoalMedium
                    )
                }
            }
            
            // Item 3: Live Family Cards Grid (Brutalist style cards)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val chunked = familyMembers.chunked(2)
                    chunked.forEach { rowMembers ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowMembers.forEach { member ->
                                FamilyCard(
                                    member = member,
                                    isDarkMode = isDarkMode,
                                    onClick = {
                                        selectedMemberForMoodChange = member
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowMembers.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            
            // Item 4: Tasks Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Immediate Handouts & Tasks ⚡",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDarkMode) Color.White else CharcoalDark
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                if (isDarkMode) SoftLavender.copy(0.2f) else SoftLavender,
                                RoundedCornerShape(10.dp)
                            )
                            .border(1.5.dp, CharcoalDark, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${tasks.count { !it.isCompleted }} pending",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else CharcoalDark
                        )
                    }
                }
            }
            
            // Item 5: Tasks Checklist List
            if (tasks.isEmpty()) {
                item {
                    CardEmptyState(
                        text = "All clean! No tasks assigned. Try adding one with the voice simulator below!",
                        imageVector = Icons.Default.AddTask,
                        isDarkMode = isDarkMode
                    )
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    TaskRowItem(
                        task = task,
                        isDarkMode = isDarkMode,
                        onCheckedChange = { viewModel.toggleTaskCompletion(task.id) },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }
            }
            
            // Item 6: Family Chat & Photo Wall Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Unified Chat & Food Wall 🥘",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDarkMode) Color.White else CharcoalDark
                    )
                    
                    Button(
                        onClick = { showSimulatedPhotoDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarmPeach,
                            contentColor = CharcoalDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.5.dp, CharcoalDark),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Post photo",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Polaroid", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            // Item 7: Chat Creator Quick text
            item {
                ChatInputBar(
                    isDarkMode = isDarkMode,
                    onMessageSend = { viewModel.addChatMessage(it) }
                )
            }
            
            // Item 8: Chat stream including dynamic layouts for photos
            items(chatFeed, key = { it.id }) { message ->
                ChatBubbleOrPolaroid(message = message, isDarkMode = isDarkMode)
            }
            
            // Bottom space buffer
            item { Spacer(modifier = Modifier.height(130.dp)) }
        }
    }
    
    // Dialog: Active User Mood dialog
    if (showMoodDialog) {
        AlertDialog(
            onDismissRequest = { showMoodDialog = false },
            title = {
                Text(
                    text = "Update Your Mood",
                    fontWeight = FontWeight.Bold,
                    color = CharcoalDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select what you are doing. Everyone in the family will be notified instantly ✨",
                        fontSize = 12.sp,
                        color = CharcoalMedium
                    )
                    LazyColumn(modifier = Modifier.height(240.dp)) {
                        items(moodOptions) { mood ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.updateOwnMood(mood)
                                        showMoodDialog = false
                                    }
                                    .border(1.dp, CharcoalDark.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAF9))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(mood, fontWeight = FontWeight.Bold, color = CharcoalDark)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoodDialog = false }) {
                    Text("Close", color = CharcoalDark)
                }
            },
            containerColor = Color.White
        )
    }
    
    // Dialog: Family member status toggle dialog
    if (selectedMemberForMoodChange != null) {
        val member = selectedMemberForMoodChange!!
        AlertDialog(
            onDismissRequest = { selectedMemberForMoodChange = null },
            title = {
                Text(
                    text = "Sync ${member.name}'s Status (Simulation)",
                    fontWeight = FontWeight.Bold,
                    color = CharcoalDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Quickly simulate a status update or action shift for ${member.name}:",
                        fontSize = 12.sp,
                        color = CharcoalMedium
                    )
                    Column {
                        listOf(
                            "At Work 💼",
                            "Baking Cake 🍳",
                            "Playing Games 🎮",
                            "Reading Book 📖",
                            "Going Home 🚗",
                            "Going to Sleep 😴",
                            "Extremely Happy 😊"
                        ).forEach { option ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.updateFamilyMemberMood(member.id, option)
                                        selectedMemberForMoodChange = null
                                    }
                                    .border(1.dp, CharcoalDark.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAF9))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(option, fontWeight = FontWeight.Bold, color = CharcoalDark)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMemberForMoodChange = null }) {
                    Text("Cancel", color = CharcoalDark)
                }
            },
            containerColor = Color.White
        )
    }
    
    // Dialog: Polaroid Photo Post Simulation Dialog
    if (showSimulatedPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showSimulatedPhotoDialog = false },
            title = {
                Text(
                    text = "Post Simulated Polaroid Photo",
                    fontWeight = FontWeight.Bold,
                    color = CharcoalDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select one of the beautiful simulated moments to render in the photo wall:",
                        fontSize = 12.sp,
                        color = CharcoalMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        photoPresets.forEach { preset ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addChatMessage(
                                            text = preset.text,
                                            isPhoto = true,
                                            photoType = preset.type,
                                            photoLabel = preset.label
                                        )
                                        showSimulatedPhotoDialog = false
                                        Toast.makeText(context, "Polaroid posted!", Toast.LENGTH_SHORT).show()
                                    }
                                    .border(1.5.dp, CharcoalDark, RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = PastelLime)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(preset.label, fontWeight = FontWeight.Bold, color = CharcoalDark)
                                        Text(preset.text, fontSize = 11.sp, color = CharcoalMedium)
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Select",
                                        tint = CharcoalDark
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSimulatedPhotoDialog = false }) {
                    Text("Cancel", color = CharcoalDark)
                }
            },
            containerColor = Color.White
        )
    }
}

// AI Generated Digest Box Composable
@Composable
fun AIDigestCardSection(aiDigest: String, isDarkMode: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isDarkMode) 2.5.dp else 1.5.dp,
                color = if (isDarkMode) SoftLavender else SoftLavender.copy(alpha = 0.40f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) CharcoalDark else SoftLavender.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PastelLime, CircleShape)
                    .border(2.dp, CharcoalDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🤖", fontSize = 22.sp)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "AI Space Digest & Summary",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDarkMode) SoftLavender else SoftLavender
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = aiDigest,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkMode) Color.White else CharcoalDark,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// Core Family Card Composable
@Composable
fun FamilyCard(
    member: FamilyMember,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = Color(android.graphics.Color.parseColor(member.colorHex))
    val avatarPreset = avatarPresets[member.avatarIndex.coerceIn(0, avatarPresets.size - 1)]
    
    Card(
        modifier = modifier
            .padding(bottom = 6.dp, end = 6.dp)
            .neobrutalistShadow(
                color = if (isDarkMode) Color.Black else CharcoalDark,
                cornerRadius = 24.dp,
                offset = 4.dp
            )
            .border(2.5.dp, if (isDarkMode) Color.White.copy(0.4f) else CharcoalDark, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) CharcoalDark else cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Member avatar badge
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(cardColor, CircleShape)
                    .border(2.dp, CharcoalDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(avatarPreset.emoji, fontSize = 32.sp)
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else CharcoalDark
            )
            
            Text(
                text = member.role,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) SoftLavender else CharcoalMedium.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Mood badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardColor.copy(alpha = 0.5f))
                    .border(1.dp, CharcoalDark.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.mood,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Task Checklist Item Row Composable
@Composable
fun TaskRowItem(
    task: FamilyTask,
    isDarkMode: Boolean,
    onCheckedChange: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp, end = 4.dp)
            .neobrutalistShadow(
                color = if (isDarkMode) Color.Black else CharcoalDark,
                cornerRadius = 16.dp,
                offset = 3.dp
            )
            .border(
                2.dp,
                if (isDarkMode) Color.White.copy(0.4f) else CharcoalDark,
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) CharcoalDark else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onCheckedChange() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SoftLavender,
                        uncheckedColor = CharcoalDark
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column {
                    Text(
                        text = task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else CharcoalDark,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Assignee badge
                        Box(
                            modifier = Modifier
                                .background(PastelLime, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                "To: ${task.assignee}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalDark
                            )
                        }
                        
                        // Due date badge
                        Box(
                            modifier = Modifier
                                .background(WarmPeach, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                "Due: ${task.dueDate}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalDark
                            )
                        }
                    }
                }
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete task",
                    tint = Color.Red.copy(0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Chat Creator Input Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(
    isDarkMode: Boolean,
    onMessageSend: (String) -> Unit
) {
    var txtVal by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp, end = 4.dp)
            .neobrutalistShadow(
                color = if (isDarkMode) Color.Black else CharcoalDark,
                cornerRadius = 16.dp,
                offset = 3.dp
            )
            .border(
                2.dp,
                if (isDarkMode) Color.White.copy(0.4f) else CharcoalDark,
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) CharcoalDark else Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = txtVal,
                onValueChange = { txtVal = it },
                placeholder = { Text("Write family message or recipe...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (txtVal.trim().isNotEmpty()) {
                        onMessageSend(txtVal.trim())
                        txtVal = ""
                        keyboardController?.hide()
                    }
                }),
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = {
                    if (txtVal.trim().isNotEmpty()) {
                        onMessageSend(txtVal.trim())
                        txtVal = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .background(SoftLavender, RoundedCornerShape(12.dp))
                    .border(1.5.dp, CharcoalDark, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Message",
                    tint = CharcoalDark,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Polaroid or Chat message renderer
@Composable
fun ChatBubbleOrPolaroid(message: ChatMessage, isDarkMode: Boolean) {
    if (message.isPhoto) {
        // Render beautiful Polaroid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .shadow(6.dp, shape = RoundedCornerShape(4.dp))
                    .rotate(if (message.photoType % 2 == 0) 1.5f else -1.5f)
                    .border(2.dp, CharcoalDark, RoundedCornerShape(4.dp)),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    // Simulated Polaroid viewport Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFFE2E8F0))
                            .border(1.5.dp, CharcoalDark)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            
                            when (message.photoType) {
                                0 -> { // Pasta Drawing (Pastel Lime plate, red tomato sauce circles, soft peach backdrop)
                                    drawRect(Color(0xFFFFF7ED))
                                    drawCircle(
                                        color = PastelLime,
                                        radius = canvasWidth * 0.35f,
                                        center = Offset(canvasWidth / 2, canvasHeight / 2)
                                    )
                                    drawCircle(
                                        color = Color(0xFFEF4444),
                                        radius = canvasWidth * 0.2f,
                                        center = Offset(canvasWidth * 0.52f, canvasHeight * 0.52f)
                                    )
                                    drawCircle(
                                        color = SoftLavender,
                                        radius = canvasWidth * 0.05f,
                                        center = Offset(canvasWidth * 0.45f, canvasHeight * 0.42f)
                                    )
                                    drawCircle(
                                        color = SoftLavender,
                                        radius = canvasWidth * 0.05f,
                                        center = Offset(canvasWidth * 0.58f, canvasHeight * 0.46f)
                                    )
                                }
                                1 -> { // Sky Sunset Drawing
                                    val grad = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFFF8A8A),
                                            Color(0xFFFFAE6B),
                                            Color(0xFFFFF078)
                                        )
                                    )
                                    drawRect(grad)
                                    drawCircle(
                                        color = Color(0xFFFFE59E),
                                        radius = canvasWidth * 0.16f,
                                        center = Offset(canvasWidth * 0.35f, canvasHeight * 0.5f)
                                    )
                                    // simple trees
                                    drawCircle(
                                        color = Color(0x3310B981),
                                        radius = canvasWidth * 0.22f,
                                        center = Offset(canvasWidth * 0.8f, canvasHeight * 0.9f)
                                    )
                                }
                                2 -> { // Teddy Bear/Toy face drawing
                                    drawRect(Color(0xFFEEF2F6))
                                    // Ears
                                    drawCircle(Color(0xFFBC84EE), canvasWidth * 0.1f, Offset(canvasWidth * 0.35f, canvasHeight * 0.42f))
                                    drawCircle(Color(0xFFBC84EE), canvasWidth * 0.1f, Offset(canvasWidth * 0.65f, canvasHeight * 0.42f))
                                    // Face center
                                    drawCircle(Color(0xFFBC84EE), canvasWidth * 0.22f, Offset(canvasWidth * 0.5f, canvasHeight * 0.58f))
                                    // Muzzle
                                    drawCircle(Color(0xFFFFF1F2), canvasWidth * 0.1f, Offset(canvasWidth * 0.5f, canvasHeight * 0.64f))
                                    drawCircle(CharcoalDark, canvasWidth * 0.02f, Offset(canvasWidth * 0.5f, canvasHeight * 0.62f))
                                    // eyes
                                    drawCircle(CharcoalDark, canvasWidth * 0.02f, Offset(canvasWidth * 0.44f, canvasHeight * 0.54f))
                                    drawCircle(CharcoalDark, canvasWidth * 0.02f, Offset(canvasWidth * 0.56f, canvasHeight * 0.54f))
                                }
                                3 -> { // Fluffy Cat
                                    drawRect(Color(0xFFFFFBEB))
                                    // Head
                                    drawCircle(Color(0xFFFBBF24), canvasWidth * 0.22f, Offset(canvasWidth * 0.5f, canvasHeight * 0.62f))
                                    // Ears
                                    val pathL = Path().apply {
                                        moveTo(canvasWidth * 0.3f, canvasHeight * 0.5f)
                                        lineTo(canvasWidth * 0.35f, canvasHeight * 0.35f)
                                        lineTo(canvasWidth * 0.45f, canvasHeight * 0.48f)
                                        close()
                                    }
                                    drawPath(pathL, Color(0xFFFBBF24))
                                    
                                    val pathR = Path().apply {
                                        moveTo(canvasWidth * 0.7f, canvasHeight * 0.5f)
                                        lineTo(canvasWidth * 0.65f, canvasHeight * 0.35f)
                                        lineTo(canvasWidth * 0.55f, canvasHeight * 0.48f)
                                        close()
                                    }
                                    drawPath(pathR, Color(0xFFFBBF24))
                                    
                                    // Eyes & Whiskers
                                    drawCircle(CharcoalDark, canvasWidth * 0.02f, Offset(canvasWidth * 0.44f, canvasHeight * 0.58f))
                                    drawCircle(CharcoalDark, canvasWidth * 0.02f, Offset(canvasWidth * 0.56f, canvasHeight * 0.58f))
                                    
                                    drawLine(CharcoalDark, Offset(canvasWidth * 0.35f, canvasHeight * 0.64f), Offset(canvasWidth * 0.25f, canvasHeight * 0.62f), 1.5f)
                                    drawLine(CharcoalDark, Offset(canvasWidth * 0.35f, canvasHeight * 0.66f), Offset(canvasWidth * 0.25f, canvasHeight * 0.66f), 1.5f)
                                    drawLine(CharcoalDark, Offset(canvasWidth * 0.65f, canvasHeight * 0.64f), Offset(canvasWidth * 0.75f, canvasHeight * 0.62f), 1.5f)
                                    drawLine(CharcoalDark, Offset(canvasWidth * 0.65f, canvasHeight * 0.66f), Offset(canvasWidth * 0.75f, canvasHeight * 0.66f), 1.5f)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = message.photoLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CharcoalDark
                    )
                    
                    Text(
                        text = message.text,
                        fontSize = 12.sp,
                        color = CharcoalMedium,
                        lineHeight = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📸 Posted by ${message.sender}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftLavender
                        )
                        Text(
                            text = message.timestamp,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    } else {
        // Standard elegant Chat bubble
        val senderAvatar = avatarPresets[message.avatarIndex.coerceIn(0, avatarPresets.size - 1)]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(senderAvatar.color, CircleShape)
                    .border(1.5.dp, CharcoalDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(senderAvatar.emoji, fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(
                        1.5.dp,
                        if (isDarkMode) Color.White.copy(0.3f) else CharcoalDark,
                        RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
                    ),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) CharcoalDark else Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message.sender,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDarkMode) SoftLavender else CharcoalMedium
                        )
                        Text(
                            text = message.timestamp,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.text,
                        fontSize = 13.sp,
                        color = if (isDarkMode) Color.White else CharcoalDark,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// Preset Quick-Voice-Action suggestions
val speechPrompts = listOf(
    "Remind Papa to pick up medicines tonight 💊",
    "Remind Mummy to bake pizza tomorrow 🍕",
    "Ask Chintu to complete science homework next week 🎒",
    "Tell Dadi to rest this weekend 👵"
)

// Simulated Speech Overlay
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VoiceTaskOverlaySimulated(
    viewModel: MyFamilyViewModel,
    isDarkMode: Boolean
) {
    var commandValue by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    
    val aiFeedbackMessage by viewModel.aiFeedbackMessage.collectAsState()
    
    // Wave pulse visual animations
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scalePulse1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "p1"
    )
    val alphaPulse1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "a1"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .border(
                BorderStroke(
                    3.dp,
                    if (isDarkMode) Color.White.copy(0.4f) else CharcoalDark
                ),
                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) DarkSurface else Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            
            // Header for parser
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice task",
                        tint = SoftLavender,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Shared Task Voice Creator",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDarkMode) Color.White else CharcoalDark
                    )
                }
                
                Box(
                    modifier = Modifier
                        .background(PastelLime, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "AI Copilot Mode",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalDark
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Loop suggestions
            Text(
                "Tap preset command or enter custom below:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkMode) Color.LightGray else CharcoalMedium
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Suggestions Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(speechPrompts) { prompt ->
                    Box(
                        modifier = Modifier
                            .background(
                                if (isDarkMode) CharcoalDark else WarmCream,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (isDarkMode) Color.White.copy(0.2f) else CharcoalDark.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { commandValue = prompt.substringBefore("💊").substringBefore("🍕").substringBefore("🎒").substringBefore("👵").trim() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else CharcoalDark
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Interactive Mic & Output row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large circular floating Microphone with waves animation when clicked
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Concentric Animated Wave Pulse behind microphone
                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(scalePulse1)
                                .background(SoftLavender.copy(alpha = alphaPulse1), CircleShape)
                        )
                    }
                    
                     FloatingActionButton(
                        onClick = {
                            isRecording = !isRecording
                            if (isRecording) {
                                // Simulate speech entry
                                if (commandValue.isEmpty()) {
                                    commandValue = "Remind Papa to pick up medicines tonight"
                                }
                            } else {
                                // Clear
                            }
                        },
                        containerColor = if (isRecording) PastelLime else SoftLavender,
                        contentColor = if (isRecording) CharcoalDark else Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(56.dp)
                            .border(3.5.dp, if (isDarkMode) CharcoalDark else Color.White, CircleShape)
                            .shadow(if (isDarkMode) 0.dp else 4.dp, CircleShape)
                            .testTag("floating_microphone_button")
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Pause else Icons.Default.Mic,
                            contentDescription = "Simulated record button",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Form input
                OutlinedTextField(
                    value = commandValue,
                    onValueChange = { commandValue = it },
                    placeholder = { Text("Speech is writing itself here...") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftLavender,
                        unfocusedBorderColor = CharcoalDark.copy(alpha = 0.3f),
                        focusedContainerColor = if (isDarkMode) CharcoalDark else Color(0xFFFAF9F6),
                        unfocusedContainerColor = if (isDarkMode) CharcoalDark else Color(0xFFFAF9F6)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("voice_command_input"),
                    trailingIcon = {
                        if (commandValue.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.parseVoiceCommand(commandValue)
                                    commandValue = ""
                                    isRecording = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Parse command",
                                    tint = CharcoalDark
                                )
                            }
                        }
                    }
                )
            }
            
            // Interactive UI popup displaying AI Parser feedback details instantly
            if (aiFeedbackMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, SoftLavender, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = PastelLime),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = aiFeedbackMessage ?: "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalDark,
                                lineHeight = 15.sp
                            )
                        }
                        
                        IconButton(
                            onClick = { viewModel.clearFeedback() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close feedback",
                                tint = CharcoalDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Visual placeholders for empty states (M3 standard layout guidelines compliant)
@Composable
fun CardEmptyState(
    text: String,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    isDarkMode: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isDarkMode) Color.White.copy(0.2f) else CharcoalDark.copy(alpha = 0.15f),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) CharcoalDark else Color(0x0A000000))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = SoftLavender,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = if (isDarkMode) Color.LightGray else CharcoalMedium,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// Extensions for neobrutalist styling
fun Modifier.neobrutalistShadow(
    color: Color,
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    offset: androidx.compose.ui.unit.Dp = 4.dp
) = this.drawBehind {
    val pxOffset = offset.toPx()
    val pxRadius = cornerRadius.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(pxOffset, pxOffset),
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(pxRadius, pxRadius)
    )
}


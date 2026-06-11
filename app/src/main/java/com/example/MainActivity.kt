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
    val keyboardController = LocalSoftwareKeyboardController.current

    // Entrance Animation State
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkCanvas,
                        DeepSpace,
                        ElectricIndigo.copy(alpha = 0.3f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Soft glowing background light
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(y = (-200).dp)
                .background(RoyalPurple.copy(alpha = 0.15f), CircleShape)
                .drawBehind { drawCircle(RoyalPurple.copy(alpha = 0.1f), radius = size.minDimension) }
        )

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(
                initialOffsetY = { 100 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(GlassBackground)
                    .border(1.5.dp, GlassBorder, RoundedCornerShape(32.dp))
                    .padding(28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Branding
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SoftCyan, RoyalPurple)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "MyFamily Room",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Enter your private family portal",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Input Fields
                GlassInputField(
                    label = "Family ID",
                    value = familyId,
                    onValueChange = { familyId = it.uppercase() },
                    placeholder = "e.g. KHAN-2024",
                    icon = Icons.Default.VpnKey,
                    trailingAction = {
                        TextButton(
                            onClick = {
                                familyId = "FAM-" + (100..999).random().toString()
                            }
                        ) {
                            Text("Generate", color = SoftCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                GlassInputField(
                    label = "Your Nickname",
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = "How should we call you?",
                    icon = Icons.Default.Person
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Avatar Selection
                Text(
                    text = "Pick an Identity",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(avatarPresets) { idx, item ->
                        val isSelected = idx == selectedAvatarIndex
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) RoyalPurple else GlassBackground)
                                .border(
                                    2.dp,
                                    if (isSelected) SoftCyan else GlassBorder,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedAvatarIndex = idx },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(item.emoji, fontSize = 28.sp)
                                Text(
                                    item.label,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Submit Button
                Button(
                    onClick = {
                        if (familyId.length < 3 || nickname.isEmpty()) {
                            Toast.makeText(context, "Complete all fields first!", Toast.LENGTH_SHORT).show()
                        } else {
                            keyboardController?.hide()
                            onJoinSpace(familyId, nickname, selectedAvatarIndex)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalPurple
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Secure Login",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun GlassInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingAction: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.3f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(icon, contentDescription = null, tint = SoftCyan) },
            trailingIcon = trailingAction,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = GlassBackground,
                unfocusedContainerColor = GlassBackground,
                focusedBorderColor = SoftCyan,
                unfocusedBorderColor = GlassBorder,
                cursorColor = SoftCyan
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MyFamilyViewModel) {
    val familyId by viewModel.familyId.collectAsState()
    val nickname by viewModel.nickname.collectAsState()
    val avatarIndex by viewModel.avatarIndex.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    
    var currentTab by remember { mutableIntStateOf(0) } // 0: Feed, 1: Chat, 2: Family
    
    val myAvatar = avatarPresets[avatarIndex.coerceIn(0, avatarPresets.size - 1)]

    Scaffold(
        containerColor = DarkCanvas,
        topBar = {
            GlassTopBar(nickname, myAvatar, familyId, onLogout = { viewModel.logOutAndReset() })
        },
        bottomBar = {
            GlassBottomNav(currentTab, onTabSelected = { currentTab = it })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepSpace, DarkCanvas)
                    )
                )
        ) {
            when (currentTab) {
                0 -> SocialFeedTab(posts, viewModel)
                1 -> GroupChatTab(chatMessages, viewModel)
                2 -> FamilyMembersTab(viewModel)
            }
        }
    }
}

@Composable
fun GlassTopBar(nickname: String, avatar: AvatarPreset, familyId: String, onLogout: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GlassBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatar.color),
                contentAlignment = Alignment.Center
            ) {
                Text(avatar.emoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(nickname, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "ID: $familyId",
                    color = SoftCyan,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable {
                        clipboardManager.setText(AnnotatedString(familyId))
                        Toast.makeText(context, "ID Copied!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
        
        IconButton(onClick = onLogout) {
            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = RoseModern)
        }
    }
}

@Composable
fun GlassBottomNav(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp)
            .height(70.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GlassBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val items = listOf(
            Icons.Default.Dashboard to "Feed",
            Icons.Default.ChatBubble to "Chat",
            Icons.Default.People to "Family"
        )
        
        items.forEachIndexed { index, item ->
            val isSelected = selectedTab == index
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelected(index) }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = item.first,
                    contentDescription = item.second,
                    tint = if (isSelected) SoftCyan else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    item.second,
                    color = if (isSelected) SoftCyan else Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SocialFeedTab(posts: List<FamilyPost>, viewModel: MyFamilyViewModel) {
    var postText by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Create Post Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = GlassBackground),
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = postText,
                    onValueChange = { postText = it },
                    placeholder = { Text("What's happening in the family?", color = Color.White.copy(alpha = 0.3f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (postText.isNotBlank()) {
                                viewModel.createPost(postText)
                                postText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Post", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(posts) { post ->
                PostCard(post, viewModel)
            }
        }
    }
}

@Composable
fun PostCard(post: FamilyPost, viewModel: MyFamilyViewModel) {
    val avatar = avatarPresets[post.avatarIndex.coerceIn(0, avatarPresets.size - 1)]
    var showComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(avatar.color), contentAlignment = Alignment.Center) {
                    Text(avatar.emoji, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(post.sender, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(post.timestamp, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(post.content, color = Color.White, fontSize = 15.sp)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.likePost(post.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = RoseModern, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${post.likes}", color = Color.White, fontSize = 12.sp)
                    }
                }
                IconButton(onClick = { showComments = !showComments }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Comment, contentDescription = null, tint = SoftCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${post.comments.size}", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            if (showComments) {
                Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))
                post.comments.forEach { comment ->
                    val cAvatar = avatarPresets[comment.avatarIndex.coerceIn(0, avatarPresets.size - 1)]
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("${cAvatar.emoji} ", fontSize = 12.sp)
                        Text("${comment.sender}: ", color = SoftCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(comment.text, color = Color.White, fontSize = 12.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Add a comment...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.3f)) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(containerColor = Color.Transparent)
                    )
                    IconButton(onClick = {
                        if (commentText.isNotBlank()) {
                            viewModel.addComment(post.id, commentText)
                            commentText = ""
                        }
                    }) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = SoftCyan)
                    }
                }
            }
        }
    }
}

@Composable
fun GroupChatTab(messages: List<ChatMessage>, viewModel: MyFamilyViewModel) {
    var messageText by remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(GlassBackground)
                .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Message family...", color = Color.White.copy(alpha = 0.3f)) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            IconButton(onClick = {
                if (messageText.isNotBlank()) {
                    viewModel.sendChatMessage(messageText)
                    messageText = ""
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = null, tint = SoftCyan)
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val avatar = avatarPresets[msg.avatarIndex.coerceIn(0, avatarPresets.size - 1)]
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(avatar.color), contentAlignment = Alignment.Center) {
            Text(avatar.emoji, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                .background(GlassBackground)
                .padding(12.dp)
        ) {
            Text(msg.sender, color = SoftCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(msg.text, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun FamilyMembersTab(viewModel: MyFamilyViewModel) {
    val members by viewModel.familyMembers.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(members) { member ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(member.colorHex))), contentAlignment = Alignment.Center) {
                        Text(avatarPresets[member.avatarIndex].emoji, fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(member.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(member.mood, color = SoftCyan, fontSize = 12.sp)
                    }
                }
            }
        }
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


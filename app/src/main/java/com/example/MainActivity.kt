package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

// Available 6 Avatars
data class AvatarPreset(val emoji: String, val label: String, val color: Color)

val avatarPresets = listOf(
    AvatarPreset("👨", "Papa", Color(0xFFE0F2FE)),
    AvatarPreset("👩", "Mummy", Color(0xFFFEE2E2)),
    AvatarPreset("👶", "Chintu", Color(0xFFF3E8FF)),
    AvatarPreset("👵", "Dadi", Color(0xFFFCE7F3)),
    AvatarPreset("🐹", "🐹", Color(0xFFFEF9C3)),
    AvatarPreset("✨", "Me", Color(0xFFDCFCE7))
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

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkCanvas, DeepSpace, ElectricIndigo.copy(alpha = 0.3f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(y = (-200).dp)
                .background(RoyalPurple.copy(alpha = 0.15f), CircleShape)
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
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(colors = listOf(SoftCyan, RoyalPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(text = "MyFamily Room", style = MaterialTheme.typography.displayMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text(text = "Enter your private family portal", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.7f))
                
                Spacer(modifier = Modifier.height(32.dp))
                
                GlassInputField(
                    label = "Family ID",
                    value = familyId,
                    onValueChange = { familyId = it.uppercase() },
                    placeholder = "e.g. FAM-2024",
                    icon = Icons.Default.VpnKey,
                    trailingAction = {
                        TextButton(onClick = { familyId = "FAM-" + (100..999).random().toString() }) {
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
                
                Text(text = "Pick an Identity", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(avatarPresets) { idx, item ->
                        val isSelected = idx == selectedAvatarIndex
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) RoyalPurple else GlassBackground)
                                .border(2.dp, if (isSelected) SoftCyan else GlassBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedAvatarIndex = idx },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(item.emoji, fontSize = 28.sp)
                                Text(item.label, fontSize = 10.sp, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Button(
                    onClick = {
                        if (familyId.length < 3 || nickname.isEmpty()) {
                            Toast.makeText(context, "Complete all fields first!", Toast.LENGTH_SHORT).show()
                        } else {
                            keyboardController?.hide()
                            onJoinSpace(familyId, nickname, selectedAvatarIndex)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Secure Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun GlassInputField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, icon: androidx.compose.ui.graphics.vector.ImageVector, trailingAction: @Composable (() -> Unit)? = null) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MyFamilyViewModel) {
    val familyId by viewModel.familyId.collectAsState()
    val nickname by viewModel.nickname.collectAsState()
    val avatarIndex by viewModel.avatarIndex.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    
    var currentTab by remember { mutableIntStateOf(0) }
    val myAvatar = avatarPresets[avatarIndex.coerceIn(0, avatarPresets.size - 1)]

    Scaffold(
        containerColor = DarkCanvas,
        topBar = { GlassTopBar(nickname, myAvatar, familyId, onLogout = { viewModel.logOutAndReset() }) },
        bottomBar = { GlassBottomNav(currentTab, onTabSelected = { currentTab = it }) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Brush.verticalGradient(colors = listOf(DeepSpace, DarkCanvas)))) {
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
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp).clip(RoundedCornerShape(24.dp)).background(GlassBackground).border(1.dp, GlassBorder, RoundedCornerShape(24.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(avatar.color), contentAlignment = Alignment.Center) { Text(avatar.emoji, fontSize = 20.sp) }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(nickname, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("ID: $familyId", color = SoftCyan, fontSize = 11.sp, modifier = Modifier.clickable {
                    clipboardManager.setText(AnnotatedString(familyId))
                    Toast.makeText(context, "ID Copied!", Toast.LENGTH_SHORT).show()
                })
            }
        }
        IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, contentDescription = null, tint = RoseModern) }
    }
}

@Composable
fun GlassBottomNav(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).height(70.dp).clip(RoundedCornerShape(24.dp)).background(GlassBackground).border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val items = listOf(Icons.Default.Dashboard to "Feed", Icons.Default.ChatBubble to "Chat", Icons.Default.People to "Family")
        items.forEachIndexed { index, item ->
            val isSelected = selectedTab == index
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onTabSelected(index) }.padding(8.dp)) {
                Icon(imageVector = item.first, contentDescription = null, tint = if (isSelected) SoftCyan else Color.White.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
                Text(item.second, color = if (isSelected) SoftCyan else Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SocialFeedTab(posts: List<FamilyPost>, viewModel: MyFamilyViewModel) {
    var postText by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize()) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = GlassBackground), border = BorderStroke(1.dp, GlassBorder)) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(value = postText, onValueChange = { postText = it }, placeholder = { Text("What's happening?", color = Color.White.copy(alpha = 0.3f)) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { if (postText.isNotBlank()) { viewModel.createPost(postText); postText = "" } }, colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo), shape = RoundedCornerShape(12.dp)) { Text("Post") }
                }
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(posts) { post -> PostCard(post, viewModel) }
        }
    }
}

@Composable
fun PostCard(post: FamilyPost, viewModel: MyFamilyViewModel) {
    val avatar = avatarPresets[post.avatarIndex.coerceIn(0, avatarPresets.size - 1)]
    var showComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = GlassBackground), border = BorderStroke(1.dp, GlassBorder)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(avatar.color), contentAlignment = Alignment.Center) { Text(avatar.emoji, fontSize = 16.sp) }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(post.sender, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(post.timestamp, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp)); Text(post.content, color = Color.White, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.likePost(post.id) }) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Favorite, contentDescription = null, tint = RoseModern, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("${post.likes}", color = Color.White, fontSize = 12.sp) } }
                IconButton(onClick = { showComments = !showComments }) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Comment, contentDescription = null, tint = SoftCyan, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("${post.comments.size}", color = Color.White, fontSize = 12.sp) } }
            }
            if (showComments) {
                HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))
                post.comments.forEach { comment ->
                    val cAvatar = avatarPresets[comment.avatarIndex.coerceIn(0, avatarPresets.size - 1)]
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("${cAvatar.emoji} ", fontSize = 12.sp)
                        Text("${comment.sender}: ", color = SoftCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(comment.text, color = Color.White, fontSize = 12.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(value = commentText, onValueChange = { commentText = it }, placeholder = { Text("Comment...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.3f)) }, modifier = Modifier.weight(1f), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))
                    IconButton(onClick = { if (commentText.isNotBlank()) { viewModel.addComment(post.id, commentText); commentText = "" } }) { Icon(Icons.Default.Send, contentDescription = null, tint = SoftCyan) }
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
        LazyColumn(modifier = Modifier.weight(1f), state = scrollState, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages) { msg -> ChatBubble(msg) }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(24.dp)).background(GlassBackground).border(1.dp, GlassBorder, RoundedCornerShape(24.dp)).padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(value = messageText, onValueChange = { messageText = it }, placeholder = { Text("Message...", color = Color.White.copy(alpha = 0.3f)) }, modifier = Modifier.weight(1f), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
            IconButton(onClick = { if (messageText.isNotBlank()) { viewModel.sendChatMessage(messageText); messageText = "" } }) { Icon(Icons.Default.Send, contentDescription = null, tint = SoftCyan) }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val avatar = avatarPresets[msg.avatarIndex.coerceIn(0, avatarPresets.size - 1)]
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(avatar.color), contentAlignment = Alignment.Center) { Text(avatar.emoji, fontSize = 16.sp) }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)).background(GlassBackground).padding(12.dp)) {
            Text(msg.sender, color = SoftCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(msg.text, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun FamilyMembersTab(viewModel: MyFamilyViewModel) {
    val members by viewModel.familyMembers.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(members) { member ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = GlassBackground), border = BorderStroke(1.dp, GlassBorder)) {
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

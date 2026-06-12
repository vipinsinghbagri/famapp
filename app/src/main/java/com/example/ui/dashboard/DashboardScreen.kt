package com.example.ui.dashboard

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ChatMessage
import com.example.data.models.FamilyPost
import com.example.data.models.FamilyMember
import com.example.ui.components.avatarPresets
import com.example.ui.theme.*
import com.example.viewmodel.MyFamilyViewModel

@Composable
fun DashboardScreen(viewModel: MyFamilyViewModel) {
    val familyId by viewModel.currentUserProfile.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val members by viewModel.familyMembers.collectAsState()
    
    var currentTab by remember { mutableIntStateOf(0) }
    val profile = familyId ?: return

    Scaffold(
        containerColor = DarkCanvas,
        topBar = { GlassTopBar(profile.nickname, profile.avatarIndex, profile.familyId ?: "", onLogout = { viewModel.logOutAndReset() }) },
        bottomBar = { GlassBottomNav(currentTab, onTabSelected = { currentTab = it }) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Brush.verticalGradient(colors = listOf(DeepSpace, DarkCanvas)))) {
            when (currentTab) {
                0 -> SocialFeedTab(posts, viewModel)
                1 -> GroupChatTab(chatMessages, viewModel)
                2 -> FamilyMembersTab(members)
            }
        }
    }
}

@Composable
fun GlassTopBar(nickname: String, avatarIdx: Int, familyId: String, onLogout: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    val avatar = avatarPresets[avatarIdx.coerceIn(0, avatarPresets.size - 1)]
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
            items(posts) { post -> PostCard(post) }
        }
    }
}

@Composable
fun PostCard(post: FamilyPost) {
    val avatar = avatarPresets[post.avatarIndex.coerceIn(0, avatarPresets.size - 1)]
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
                Icon(Icons.Default.Favorite, contentDescription = null, tint = RoseModern, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${post.likes}", color = Color.White, fontSize = 12.sp)
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
fun FamilyMembersTab(members: List<FamilyMember>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(members) { member ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = GlassBackground), border = BorderStroke(1.dp, GlassBorder)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(member.colorHex))), contentAlignment = Alignment.Center) {
                        Text(avatarPresets[member.avatarIndex.coerceIn(0, avatarPresets.size - 1)].emoji, fontSize = 28.sp)
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

package com.example.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.avatarPresets
import com.example.ui.theme.*
import com.example.viewmodel.MyFamilyViewModel

enum class OnboardingMode {
    SIGNUP, FAMILY_SETUP
}

@Composable
fun OnboardingScreen(viewModel: MyFamilyViewModel) {
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    var mode by remember { mutableStateOf(if (currentUserProfile == null) OnboardingMode.SIGNUP else OnboardingMode.FAMILY_SETUP) }
    
    var nickname by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var selectedAvatarIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(DarkCanvas, DeepSpace, ElectricIndigo.copy(alpha = 0.3f)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp).clip(RoundedCornerShape(32.dp)).background(GlassBackground).border(1.5.dp, GlassBorder, RoundedCornerShape(32.dp)).padding(28.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (mode == OnboardingMode.SIGNUP) "Create Identity" else "Family Portal",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            if (mode == OnboardingMode.SIGNUP) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Your Nickname", color = Color.White.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = SoftCyan, unfocusedBorderColor = GlassBorder)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text("Pick an Avatar", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(avatarPresets) { idx, item ->
                        Box(
                            modifier = Modifier.size(60.dp).clip(CircleShape).background(if (idx == selectedAvatarIndex) RoyalPurple else GlassBackground).border(2.dp, if (idx == selectedAvatarIndex) SoftCyan else Color.Transparent, CircleShape).clickable { selectedAvatarIndex = idx },
                            contentAlignment = Alignment.Center
                        ) { Text(item.emoji, fontSize = 28.sp) }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = { if (nickname.isNotBlank()) viewModel.signUp(nickname, selectedAvatarIndex) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                    shape = RoundedCornerShape(20.dp)
                ) { Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            }

            if (mode == OnboardingMode.FAMILY_SETUP) {
                Text("Start a New Family", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                OutlinedTextField(
                    value = familyName,
                    onValueChange = { familyName = it },
                    placeholder = { Text("Family Name (e.g. The Khans)", color = Color.White.copy(alpha = 0.3f)) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = SoftCyan, unfocusedBorderColor = GlassBorder)
                )
                Button(onClick = { if (familyName.isNotBlank()) viewModel.createFamily(familyName) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)) {
                    Text("Create Room")
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Text("OR Join Existing", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it },
                    placeholder = { Text("Enter 6-digit Invite Code", color = Color.White.copy(alpha = 0.3f)) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = SoftCyan, unfocusedBorderColor = GlassBorder)
                )
                Button(onClick = { if (inviteCode.isNotBlank()) viewModel.joinFamily(inviteCode) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = SoftCyan, contentColor = Color.Black)) {
                    Text("Join Room", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    
    LaunchedEffect(currentUserProfile) {
        if (currentUserProfile != null) mode = OnboardingMode.FAMILY_SETUP
    }
}

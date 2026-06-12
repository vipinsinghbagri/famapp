package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.viewmodel.MyFamilyViewModel

@Composable
fun MyFamilyApp(viewModel: MyFamilyViewModel) {
    val isOnboarded by viewModel.isOnboarded.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (!isOnboarded || currentUserProfile?.familyId == null) {
            OnboardingScreen(viewModel = viewModel)
        } else {
            DashboardScreen(viewModel = viewModel)
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MasViewModel

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MasViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(
                darkTheme = true, // Force gorgeous dark mode suite for creative futuristic vibes
                dynamicColor = false // Force custom purple-orange branding colors
            ) {
                val loggedInUser by viewModel.loggedInUser.collectAsState()

                if (loggedInUser == null) {
                    LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = {
                            // User logged in successfully
                        }
                    )
                } else {
                    MainAppScaffold(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: MasViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(TechBg),
        bottomBar = {
            // Sleek Material Design 3 Bottom Navigation bar with safe navigation padding
            NavigationBar(
                containerColor = TechCardBg,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .background(TechCardBg)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Creative Studio Tab
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Hexagon else Icons.Outlined.Hexagon,
                            contentDescription = "Studio Hub"
                        )
                    },
                    label = { Text("Studio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonOrange,
                        selectedTextColor = NeonOrange,
                        indicatorColor = NeonPurple.copy(alpha = 0.25f),
                        unselectedIconColor = LightGreyText,
                        unselectedTextColor = LightGreyText
                    )
                )

                // Showcase Timeline
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Default.VideoLibrary else Icons.Outlined.VideoLibrary,
                            contentDescription = "AI Showcase Feed"
                        )
                    },
                    label = { Text("Showcase") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonOrange,
                        selectedTextColor = NeonOrange,
                        indicatorColor = NeonPurple.copy(alpha = 0.25f),
                        unselectedIconColor = LightGreyText,
                        unselectedTextColor = LightGreyText
                    )
                )

                // AI Chatbot Info assistant
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Default.Forum else Icons.Outlined.Forum,
                            contentDescription = "Mas AI Chat"
                        )
                    },
                    label = { Text("AI Assistant") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonOrange,
                        selectedTextColor = NeonOrange,
                        indicatorColor = NeonPurple.copy(alpha = 0.25f),
                        unselectedIconColor = LightGreyText,
                        unselectedTextColor = LightGreyText
                    )
                )

                // Portal Settings & Pro billing
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Default.ManageAccounts else Icons.Outlined.ManageAccounts,
                            contentDescription = "User Settings Portal"
                        )
                    },
                    label = { Text("Mas Portal") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonOrange,
                        selectedTextColor = NeonOrange,
                        indicatorColor = NeonPurple.copy(alpha = 0.25f),
                        unselectedIconColor = LightGreyText,
                        unselectedTextColor = LightGreyText
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TechBg)
        ) {
            when (selectedTab) {
                0 -> StudioScreen(
                    viewModel = viewModel,
                    onNavigateToPro = { selectedTab = 3 }
                )
                1 -> FeedScreen(
                    viewModel = viewModel,
                    onNavigateToPro = { selectedTab = 3 }
                )
                2 -> ChatScreen(
                    viewModel = viewModel
                )
                3 -> ProfileScreen(
                    viewModel = viewModel,
                    onLogoutCompleted = { selectedTab = 0 }
                )
            }
        }
    }
}

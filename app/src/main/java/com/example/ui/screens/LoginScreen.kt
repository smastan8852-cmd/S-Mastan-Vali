package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MasViewModel

// Theme colors inside screens for styling
val TechBg = Color(0xFF070B19)
val TechCardBg = Color(0xFF11172A)
val NeonPurple = Color(0xFF9E00FF)
val NeonOrange = Color(0xFFFF5E00)
val TechBorder = Color(0xFF1E293B)
val LightGreyText = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MasViewModel,
    onLoginSuccess: () -> Unit
) {
    val phone by viewModel.loginPhone.collectAsState()
    val password by viewModel.loginPassword.collectAsState()
    val name by viewModel.registerName.collectAsState()
    val isLoginMode by viewModel.isLoginMode.collectAsState()
    val authError by viewModel.authError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TechBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Subtle decorative ambient background circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = NeonPurple.copy(alpha = 0.15f),
                radius = size.width * 0.4f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.2f)
            )
            drawCircle(
                color = NeonOrange.copy(alpha = 0.1f),
                radius = size.width * 0.5f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.8f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Large Styled Logo
            MasLogo(size = 100.dp, showText = true)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Next-Generation Multimodal AI Suite",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LightGreyText,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Mode Selector Tab (Login vs Register)
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(TechCardBg, RoundedCornerShape(24.dp))
                    .padding(4.dp)
                    .border(1.dp, TechBorder, RoundedCornerShape(24.dp)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isLoginMode) Brush.horizontalGradient(listOf(NeonPurple, NeonOrange)) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.isLoginMode.value = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Login",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (!isLoginMode) Brush.horizontalGradient(listOf(NeonPurple, NeonOrange)) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.isLoginMode.value = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Register",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Error Message Card
            if (authError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = authError ?: "",
                        color = Color(0xFFFCA5A5),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Input Fields
            if (!isLoginMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.registerName.value = it },
                    label = { Text("Full Name", color = LightGreyText) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = TechBorder,
                        focusedContainerColor = TechCardBg,
                        unfocusedContainerColor = TechCardBg
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("register_name_input"),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonPurple) },
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { viewModel.loginPhone.value = it },
                label = { Text("Mobile Number", color = LightGreyText) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = TechBorder,
                    focusedContainerColor = TechCardBg,
                    unfocusedContainerColor = TechCardBg
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("login_phone_input"),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = NeonPurple) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.loginPassword.value = it },
                label = { Text("Password", color = LightGreyText) },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = TechBorder,
                    focusedContainerColor = TechCardBg,
                    unfocusedContainerColor = TechCardBg
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .testTag("login_password_input"),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonPurple) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            // Primary Action Button (Gradient)
            Button(
                onClick = { viewModel.loginOrRegister(onLoginSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("auth_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(listOf(NeonPurple, NeonOrange)))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLoginMode) "Log In Securely" else "Register & Connect",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "OR",
                color = LightGreyText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Google Sign In Button
            Button(
                onClick = {
                    viewModel.loginPhone.value = "+1 555-AI-STUDIO"
                    viewModel.loginPassword.value = "GoogleSsoUser"
                    viewModel.registerName.value = "Google Developer"
                    viewModel.isLoginMode.value = true
                    viewModel.loginOrRegister(onLoginSuccess)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .border(1.dp, TechBorder, RoundedCornerShape(14.dp))
                    .testTag("google_login_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TechCardBg),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            color = Color(0xFFEA4335),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign in with Google Account",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CreationEntity
import com.example.ui.viewmodel.MasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MasViewModel,
    onLogoutCompleted: () -> Unit
) {
    val user by viewModel.loggedInUser.collectAsState()
    val creations by viewModel.userCreations.collectAsState()
    val feedbackInput by viewModel.feedbackInput.collectAsState()

    val context = LocalContext.current
    val isPro = user?.isPro == true

    var showBuySuccessDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TechBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Mas Portal",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Manage subscription, view creations & suggest features",
                        color = LightGreyText,
                        fontSize = 11.sp
                    )
                }

                // Log out button
                IconButton(
                    onClick = { viewModel.logout(onLogoutCompleted) },
                    modifier = Modifier.testTag("logout_button")
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = "Log Out", tint = Color.Red.copy(alpha = 0.8f))
                }
            }

            // User Info Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechCardBg),
                border = BorderStroke(1.dp, TechBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(
                                Brush.horizontalGradient(listOf(NeonPurple, NeonOrange)),
                                RoundedCornerShape(27.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user?.name ?: "Guest").take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = user?.name ?: "Mas AI Member",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Phone: ${user?.phoneNumber ?: "N/A"}",
                            color = LightGreyText,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- PRO TRIAL CONFIGURATOR & PAYMENT GATEWAY PANEL ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPro) NeonOrange.copy(alpha = 0.1f) else TechCardBg
                ),
                border = BorderStroke(1.dp, if (isPro) NeonOrange else TechBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = NeonOrange,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPro) "PRO Plan Active" else "Mas AI Pro Membership",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        if (isPro) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Green.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.Green, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Active", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Unlock the ultimate AI rendering capabilities of Mas AI:",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pro Features checklist
                    val features = listOf(
                        "10x Faster Generation speeds (0.6s synthesis)",
                        "Unlocks premium Cinematic Video generation models",
                        "Unlocks dynamic Animation creative loop modals",
                        "Appends custom simulated background Sound Effects",
                        "Allows max 4K UHD cinematic texture resolution"
                    )

                    features.forEach { feat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(feat, color = LightGreyText, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!isPro) {
                        Text(
                            text = "Standard Membership Fee: $9.99/mo\nLimited-Time Promo: $4.99 one-time payment",
                            color = NeonOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.purchasePro()
                                showBuySuccessDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("purchase_pro_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.horizontalGradient(listOf(NeonPurple, NeonOrange))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Pay $4.99 & Get Pro", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Option for development testing to toggle back to free mode
                        OutlinedButton(
                            onClick = {
                                viewModel.testDowngradePro()
                                Toast.makeText(context, "Plan changed back to FREE for testing purposes", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("degrade_free_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                        ) {
                            Text("Switch back to FREE (Test Plan)", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SUGGESTIONS & FEEDBACK FORM ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechCardBg),
                border = BorderStroke(1.dp, TechBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Submit Feedback & Suggestions",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "We review suggestions daily to make Mas AI better!",
                        color = LightGreyText,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = feedbackInput,
                        onValueChange = { viewModel.feedbackInput.value = it },
                        placeholder = { Text("What tools or styles should we add next? Tell us your feedback...", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = TechBorder,
                            focusedContainerColor = TechBg,
                            unfocusedContainerColor = TechBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("feedback_text_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (feedbackInput.trim().isEmpty()) return@Button
                            viewModel.submitFeedback()
                            Toast.makeText(context, "Thank you! Suggestion recorded successfully.", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("submit_feedback_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Text("Submit Suggestion", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- USER HISTORIC GALLERY / CREATIVE LOGS ---
            Text(
                text = "My Synthesis History (${creations.size})",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            if (creations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No creations generated yet. Go to Studio tab!", color = LightGreyText, fontSize = 12.sp)
                }
            } else {
                // Horizontal scrollable cards gallery since Vertical scroll is outer container
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    creations.forEach { item ->
                        HistoryItemCard(
                            creation = item,
                            onDelete = { viewModel.deleteCreation(item.id) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // Pro Active Success Dialogue
        if (showBuySuccessDialog) {
            AlertDialog(
                onDismissRequest = { showBuySuccessDialog = false },
                title = { Text("Welcome to Mas AI Pro! 🎉", color = Color.White, fontWeight = FontWeight.Black) },
                text = {
                    Text(
                        text = "Your payment fee of $4.99 has been authorized. You have unlocked super-fast rendering speeds, Cinematic Video generation models, and customized sound effects integrations!",
                        color = Color.White
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showBuySuccessDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                    ) {
                        Text("Start Creating")
                    }
                },
                containerColor = TechCardBg
            )
        }
    }
}

@Composable
fun HistoryItemCard(
    creation: CreationEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_card_${creation.id}"),
        colors = CardDefaults.cardColors(containerColor = TechCardBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, TechBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left visual thumbnail
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TechBg)
            ) {
                AsyncImage(
                    model = creation.resultUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Small modality badge over thumbnail
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = creation.modality,
                        color = NeonOrange,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text parameters
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = creation.prompt,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Style: ${creation.style} | Info: ${creation.resolution} (${creation.ratio})",
                    color = LightGreyText,
                    fontSize = 10.sp
                )
                if (creation.soundEffects != "None") {
                    Text(
                        text = "🔊 Sound: ${creation.soundEffects}",
                        color = NeonPurple,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete action icon
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete from history log",
                    tint = Color.Red.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

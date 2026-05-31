package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.data.model.CommunityVideoEntity
import com.example.ui.viewmodel.MasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: MasViewModel,
    onNavigateToPro: () -> Unit
) {
    val videos by viewModel.communityVideos.collectAsState()
    val isUserPro = viewModel.loggedInUser.collectAsState().value?.isPro == true
    val context = LocalContext.current

    // Upload dialog state
    var showUploadDialog by remember { mutableStateOf(false) }
    var uploadTitle by remember { mutableStateOf("") }
    var uploadStyle by remember { mutableStateOf("Cinematic") }
    var uploadSoundFX by remember { mutableStateOf("Ambient orchestral") }

    // Face swap dialog state
    var activeFaceSwapVideo by remember { mutableStateOf<CommunityVideoEntity?>(null) }
    var targetFaceName by remember { mutableStateOf("") }
    var faceSwapProgress by remember { mutableStateOf(0) }
    var isSwappingFace by remember { mutableStateOf(false) }
    var swapProgressText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TechBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Content
            Row(
                modifier = Modifier
                    .fillQueryBar()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    MasLogo(size = 40.dp, showText = true)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Browse face swaps & upload your AI creations",
                        color = LightGreyText,
                        fontSize = 11.sp
                    )
                }

                // Add button to upload simulated video
                Button(
                    onClick = { showUploadDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(38.dp).testTag("open_upload_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(NeonPurple, NeonOrange)))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Timeline feed
            if (videos.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MovieCreation, contentDescription = null, modifier = Modifier.size(48.dp), tint = LightGreyText)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No video posts found. Create or upload one!", color = LightGreyText)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(videos) { item ->
                        CommunityVideoCard(
                            video = item,
                            onLike = { viewModel.likeVideo(item.id, !item.isLiked) },
                            onSwapFaceClick = { activeFaceSwapVideo = item }
                        )
                    }
                }
            }
        }

        // --- Simulated AI Upload Dialogue ---
        if (showUploadDialog) {
            AlertDialog(
                onDismissRequest = { showUploadDialog = false },
                title = { Text("Upload AI Masterpiece", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "Fill the metadata of the AI-created video that you would like to share with the Mas AI community.",
                            color = LightGreyText,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = uploadTitle,
                            onValueChange = { uploadTitle = it },
                            label = { Text("Video Title / Prompt", color = LightGreyText) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonPurple,
                                focusedContainerColor = TechBg,
                                unfocusedContainerColor = TechBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        // Style Dropdown Choice selection
                        Text("Style Coordinate:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                            val listStyles = listOf("Cinematic", "Cyberpunk", "Animatic", "3D Render")
                            listStyles.forEach { curStyle ->
                                val active = uploadStyle == curStyle
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) NeonPurple else TechCardBg)
                                        .border(1.dp, if (active) NeonOrange else TechBorder, RoundedCornerShape(8.dp))
                                        .clickable { uploadStyle = curStyle }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(curStyle, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uploadSoundFX,
                            onValueChange = { uploadSoundFX = it },
                            label = { Text("Sound Effects (e.g. Synth waves)", color = LightGreyText) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonPurple,
                                focusedContainerColor = TechBg,
                                unfocusedContainerColor = TechBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (uploadTitle.trim().isEmpty()) return@Button
                            viewModel.uploadAIWork(uploadTitle, uploadStyle, uploadSoundFX)
                            showUploadDialog = false
                            uploadTitle = ""
                            Toast.makeText(context, "AI Video uploaded to Feed successfully!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                    ) {
                        Text("Upload to Feed")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUploadDialog = false }) {
                        Text("Cancel", color = LightGreyText)
                    }
                },
                containerColor = TechCardBg
            )
        }

        // --- Interactive AI Face-Swap Wizard ---
        if (activeFaceSwapVideo != null) {
            val currentVid = activeFaceSwapVideo!!
            AlertDialog(
                onDismissRequest = {
                    if (!isSwappingFace) activeFaceSwapVideo = null
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Face, contentDescription = null, tint = NeonOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Interactive AI Face-Swap Wizard", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        if (!isSwappingFace) {
                            Text(
                                text = "Swap your face or any custom character into: '${currentVid.title}' using Mas AI coordinates.",
                                color = LightGreyText,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Media preview inside Dialog to feel extremely rich
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = currentVid.thumbnailCoverUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "[Template: ${currentVid.style}]",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = targetFaceName,
                                onValueChange = { targetFaceName = it },
                                label = { Text("Target Character Name (e.g. Elon Musk, Myself)", color = LightGreyText) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonOrange,
                                    focusedContainerColor = TechBg,
                                    unfocusedContainerColor = TechBg
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("face_swap_target_input"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "⚠️ Note: Generating face-swap integrations consumes GPU matrix steps. This will auto-save to your Creation Studio hub.",
                                color = NeonPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            // Swapping face progress visualizer
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(swapProgressText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("$faceSwapProgress%", color = NeonOrange, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { faceSwapProgress / 100f },
                                    color = NeonOrange,
                                    trackColor = TechBg,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    if (!isSwappingFace) {
                        Button(
                            onClick = {
                                if (targetFaceName.trim().isEmpty()) return@Button
                                isSwappingFace = true
                                faceSwapProgress = 0
                                swapProgressText = "Parsing source template elements..."

                                // Launch face swap simulation coroutine
                                val steps = listOf(
                                    "Parsing source face templates..." to 15,
                                    "Tracking facial landmarks & lighting mapping..." to 45,
                                    "Warping faces with target light coords [$targetFaceName]..." to 75,
                                    "Rendering deep neural layers with custom sound FX..." to 95,
                                    "Finalizing mechapixel frames..." to 100
                                )

                                val waitTime = if (isUserPro) 20L else 120L

                                // Kick off local thread simulation
                                val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
                                scope.launch {
                                    for ((text, pct) in steps) {
                                        swapProgressText = text
                                        while (faceSwapProgress < pct) {
                                            kotlinx.coroutines.delay(waitTime)
                                            faceSwapProgress += 1
                                        }
                                    }

                                    // Add to user creations
                                    viewModel.creativePrompt.value = "Face-swap of $targetFaceName into template '${currentVid.title}' with custom video options"
                                    viewModel.selectedModality.value = "VIDEO"
                                    viewModel.selectedStyle.value = currentVid.style
                                    viewModel.includeSoundEffects.value = true
                                    viewModel.soundEffectsPrompt.value = currentVid.soundEffects

                                    // Run actual synthesis to inject to Room creations list!
                                    viewModel.startAISynthesis()

                                    Toast.makeText(context, "Face swap simulation created inside Studio Screen!", Toast.LENGTH_LONG).show()

                                    // Clear dialogue
                                    isSwappingFace = false
                                    activeFaceSwapVideo = null
                                    targetFaceName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                        ) {
                            Text("Swap Faces")
                        }
                    }
                },
                dismissButton = {
                    if (!isSwappingFace) {
                        TextButton(onClick = { activeFaceSwapVideo = null }) {
                            Text("Dismiss", color = LightGreyText)
                        }
                    }
                },
                containerColor = TechCardBg
            )
        }
    }
}

// Custom internal row modifier as helper
private fun Modifier.fillQueryBar(): Modifier = this.fillMaxWidth()

// Single item card inside feed
@Composable
fun CommunityVideoCard(
    video: CommunityVideoEntity,
    onLike: () -> Unit,
    onSwapFaceClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("video_post_card_${video.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TechCardBg),
        border = BorderStroke(1.dp, TechBorder)
    ) {
        Column {
            // Media Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = video.thumbnailCoverUrl,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Bottom gradient scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                // Style Badge
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                        .background(NeonPurple, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = video.style,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Play Button overlays
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Simulate Video Stream",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Author block
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(NeonOrange, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = video.author.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = video.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "by @${video.author}",
                            color = LightGreyText,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Bottom interactivity actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sound info and uploads
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = LightGreyText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = video.soundEffects,
                        color = LightGreyText,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Like action
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onLike() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (video.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (video.isLiked) Color.Red else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = video.likesCount.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Swap Face Action Button!
                    Button(
                        onClick = { onSwapFaceClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple.copy(alpha = 0.25f)),
                        border = BorderStroke(1.dp, NeonPurple),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp).testTag("swap_face_action_${video.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Face, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Swap Face", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

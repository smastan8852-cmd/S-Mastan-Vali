package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

data class ModalityItem(
    val id: String,
    val desc: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    viewModel: MasViewModel,
    onNavigateToPro: () -> Unit
) {
    val prompt by viewModel.creativePrompt.collectAsState()
    val modality by viewModel.selectedModality.collectAsState()
    val style by viewModel.selectedStyle.collectAsState()
    val ratio by viewModel.selectedRatio.collectAsState()
    val resolution by viewModel.selectedResolution.collectAsState()
    val soundEffectsEnabled by viewModel.includeSoundEffects.collectAsState()
    val soundEffectsPrompt by viewModel.soundEffectsPrompt.collectAsState()

    val isGenerating by viewModel.isGenerating.collectAsState()
    val progress by viewModel.generationProgress.collectAsState()
    val progressText by viewModel.currentProgressText.collectAsState()
    val lastCreation by viewModel.lastGeneratedCreation.collectAsState()
    val user by viewModel.loggedInUser.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberScrollState()

    // Interactive 3D Model Rotations (Stored as angles in Radian for our wireframe demo)
    var yawAngle by remember { mutableStateOf(0.5f) }
    var pitchAngle by remember { mutableStateOf(0.4f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TechBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(listState)
                .padding(bottom = 24.dp)
        ) {
            // Header Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MasLogo(size = 46.dp, showText = true)

                // User speed and Pro marker
                Card(
                    modifier = Modifier.clickable { onNavigateToPro() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (user?.isPro == true) NeonOrange.copy(alpha = 0.25f) else TechCardBg
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (user?.isPro == true) NeonOrange else TechBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (user?.isPro == true) Icons.Default.Bolt else Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = if (user?.isPro == true) NeonOrange else Color.Yellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (user?.isPro == true) "PRO (Super Fast)" else "FREE (Standard)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Creative Multimodal Selection Grid Row
            Text(
                text = "Select Generation Modal",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            val modalities = listOf(
                ModalityItem("IMAGE", "Free creation mode", Icons.Default.Photo),
                ModalityItem("VIDEO", "PRO required", Icons.Default.Videocam)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("modality_row")
            ) {
                items(modalities) { item ->
                    val isSelected = item.id == modality
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .width(135.dp)
                            .clickable { viewModel.setModality(item.id) }
                            .testTag("modality_chip_${item.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) TechCardBg else TechCardBg.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(
                            2.dp,
                            if (isSelected) Brush.horizontalGradient(listOf(NeonPurple, NeonOrange))
                            else Brush.linearGradient(listOf(TechBorder, TechBorder))
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = if (isSelected) NeonOrange else LightGreyText,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.id,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = item.desc,
                                color = LightGreyText,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Text prompt panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechCardBg),
                border = BorderStroke(1.dp, TechBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Describe your vision",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        // Smart Expand Suggestion Button using offline helper
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonPurple.copy(alpha = 0.2f))
                                .clickable {
                                    val currentText = prompt
                                    if (currentText.isBlank()) {
                                        viewModel.creativePrompt.value = "An astronaut standing on Mars gazing at a glowing city, oil-painting style, highly detailed"
                                    } else {
                                        // Auto-expand prompt beautifully
                                        viewModel.creativePrompt.value = "Highly detailed masterwork of ${currentText.trim()}, spectacular depth, 8k cinematic resolution, dramatic key lights, gorgeous volumetric fog"
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonOrange,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI Expand",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = prompt,
                            onValueChange = { viewModel.creativePrompt.value = it },
                            placeholder = {
                                Text(
                                    text = "Enter a detailed prompt (e.g. 'Cinematic landscape of Neo-Tokyo under neon purple thunderstorm, Unreal Render 5')...",
                                    color = LightGreyText,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                                .testTag("creative_prompt_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = TechBorder,
                                focusedContainerColor = TechBg,
                                unfocusedContainerColor = TechBg
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Brush.horizontalGradient(listOf(NeonPurple, NeonOrange)))
                                .clickable {
                                    if (prompt.isNotBlank()) {
                                        viewModel.startAISynthesis()
                                    }
                                }
                                .testTag("send_prompt_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Prompt",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset suggestions tags row
                    Text(
                        text = "Suggested presets:",
                        color = LightGreyText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val suggestionsList = listOf(
                        "Space Odyssey on Mars",
                        "Steampunk dragon flying through sunset",
                        "Renaissance futuristic mech robot",
                        "Sleek hypercar gliding in Neo-Tokyo"
                    )

                    LazyRow(modifier = Modifier.fillMaxWidth()) {
                        items(suggestionsList) { word ->
                            Text(
                                text = "+ $word",
                                color = NeonOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(end = 12.dp, top = 2.dp, bottom = 2.dp)
                                    .clickable { viewModel.creativePrompt.value = word }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI style modifiers
            Text(
                text = "Creative Style Presets",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            val styles = listOf(
                "Cinematic" to "Film cameras & warm lighting",
                "Animatic" to "Handdrawn frame sequence",
                "Cyberpunk" to "Fluorescent neon city vibes",
                "Anime" to "Vibrant fantasy illustrations",
                "3D Render" to "Hyper-detailed Octane project"
            )

            LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                items(styles) { coreStyle ->
                    val isStyleSelected = coreStyle.first == style
                    Card(
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { viewModel.setStyle(coreStyle.first) }
                            .testTag("style_chip_${coreStyle.first}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isStyleSelected) NeonPurple.copy(alpha = 0.35f) else TechCardBg
                        ),
                        border = BorderStroke(1.dp, if (isStyleSelected) NeonPurple else TechBorder)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                            Text(
                                text = coreStyle.first,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = coreStyle.second,
                                color = LightGreyText,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Options panel for VIDEO and ANIMATION
            AnimatedVisibility(visible = modality == "VIDEO" || modality == "ANIMATION") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = TechCardBg),
                    border = BorderStroke(1.dp, TechBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Premium Video Custom Options",
                            color = NeonOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sound Effects Trigger
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Synthesis Custom Sound Effects",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Adds fitting AI-synthesized audio effects",
                                    color = LightGreyText,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = soundEffectsEnabled,
                                onCheckedChange = { viewModel.includeSoundEffects.value = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonOrange,
                                    checkedTrackColor = NeonPurple.copy(alpha = 0.5f)
                                )
                            )
                        }

                        if (soundEffectsEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = soundEffectsPrompt,
                                onValueChange = { viewModel.soundEffectsPrompt.value = it },
                                label = { Text("What sound theme components?", color = LightGreyText) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonOrange,
                                    unfocusedBorderColor = TechBorder,
                                    focusedContainerColor = TechBg,
                                    unfocusedContainerColor = TechBg
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Aspect Ratio selector
                        Text(
                            text = "Render Aspect Ratio",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val ratios = listOf("16:9", "21:9", "9:16", "1:1")
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ratios.forEach { item ->
                                val active = item == ratio
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) NeonPurple else TechBg)
                                        .border(1.dp, if (active) NeonOrange else TechBorder, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.selectedRatio.value = item }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Video Resolution Selector
                        Text(
                            text = "Render Resolution Quality",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val resolutions = listOf("1080p", "4K UHD", "2K QHD")
                        Row(modifier = Modifier.fillMaxWidth()) {
                            resolutions.forEach { item ->
                                val active = item == resolution
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) NeonPurple else TechBg)
                                        .border(1.dp, if (active) NeonOrange else TechBorder, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.selectedResolution.value = item }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Generate Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                if (!isGenerating) {
                    val label = when (modality) {
                        "VIDEO" -> "Generate Cinema Video (PRO)"
                        "ANIMATION" -> "Generate Animatic Loop"
                        "3D MODEL" -> "Assemble 3D Mesh"
                        else -> "Synthesize Ultra-High Image"
                    }
                    Button(
                        onClick = { viewModel.startAISynthesis() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("generate_action_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(NeonPurple, NeonOrange))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                } else {
                    // Progress generator display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = TechCardBg),
                        border = BorderStroke(1.dp, NeonOrange)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = progressText,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$progress%",
                                    color = NeonOrange,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .testTag("generation_progress_bar"),
                                color = NeonOrange,
                                trackColor = TechBg
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Creation outputs layout
            AnimatedVisibility(visible = lastCreation != null) {
                CreationResultCard(
                    creation = lastCreation,
                    yawAngle = yawAngle,
                    pitchAngle = pitchAngle,
                    onYawChanged = { yawAngle = it },
                    onPitchChanged = { pitchAngle = it }
                )
            }
        }
    }
}

// Sub-component card to render the customized creations
@Composable
fun CreationResultCard(
    creation: CreationEntity?,
    yawAngle: Float,
    pitchAngle: Float,
    onYawChanged: (Float) -> Unit,
    onPitchChanged: (Float) -> Unit
) {
    if (creation == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("creation_result_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TechCardBg),
        border = BorderStroke(1.dp, TechBorder)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Synthesized [${creation.modality}] Complete",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${creation.style} Model",
                    color = LightGreyText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Central Media Render container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(TechBg),
                contentAlignment = Alignment.Center
            ) {
                when (creation.modality) {
                    "IMAGE" -> {
                        AsyncImage(
                            model = creation.resultUrl,
                            contentDescription = "Created AI Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    "3D MODEL" -> {
                        // EXQUISITE INTERACTIVE 3D WIREFRAME EASTER EGG
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        onYawChanged(yawAngle + dragAmount.x * 0.01f)
                                        onPitchChanged(pitchAngle - dragAmount.y * 0.01f)
                                    }
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cx = size.width / 2
                                val cy = size.height / 2
                                val baseScale = size.minDimension * 0.35f

                                // Standard 3D coordinates for a cube centered in Space
                                val points = listOf(
                                    listOf(-1f, -1f, -1f),
                                    listOf(1f, -1f, -1f),
                                    listOf(1f, 1f, -1f),
                                    listOf(-1f, 1f, -1f),
                                    listOf(-1f, -1f, 1f),
                                    listOf(1f, -1f, 1f),
                                    listOf(1f, 1f, 1f),
                                    listOf(-1f, 1f, 1f)
                                )

                                // Transform & Rotation logic around Yaw (Y) and Pitch (X)
                                val rotatedPoints = points.map { pt ->
                                    val x = pt[0]
                                    val y = pt[1]
                                    val z = pt[2]

                                    // Rotate around Y Axis (Yaw yawAngle)
                                    val x1 = x * cos(yawAngle) - z * sin(yawAngle)
                                    val z1 = x * sin(yawAngle) + z * cos(yawAngle)

                                    // Rotate around X Axis (Pitch pitchAngle)
                                    val y2 = y * cos(pitchAngle) - z1 * sin(pitchAngle)
                                    val z2 = y * sin(pitchAngle) + z1 * cos(pitchAngle)

                                    // Projection coordinate factoring perspective z-depth
                                    val dist = 3.0f
                                    val factor = dist / (dist + z2)
                                    val projX = cx + x1 * baseScale * factor
                                    val projY = cy + y2 * baseScale * factor

                                    Offset(projX, projY)
                                }

                                // Wireframe links/edges between corners
                                val edges = listOf(
                                    0 to 1, 1 to 2, 2 to 3, 3 to 0, // front
                                    4 to 5, 5 to 6, 6 to 7, 7 to 4, // back
                                    0 to 4, 1 to 5, 2 to 6, 3 to 7  // sides
                                )

                                // Draw connections in colorful neon orange/cyan
                                val brushLines = Brush.linearGradient(listOf(NeonOrange, NeonPurple))
                                edges.forEach { edge ->
                                    drawLine(
                                        brush = brushLines,
                                        start = rotatedPoints[edge.first],
                                        end = rotatedPoints[edge.second],
                                        strokeWidth = 3.dp.toPx()
                                    )
                                }

                                // Render nodes/points on vertex points
                                rotatedPoints.forEach { node ->
                                    drawCircle(
                                        color = Color.White,
                                        radius = 6.dp.toPx(),
                                        center = node
                                    )
                                    drawCircle(
                                        color = NeonOrange,
                                        radius = 4.dp.toPx(),
                                        center = node
                                    )
                                }
                            }

                            // Interactive instructional tag
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Gesture,
                                    contentDescription = null,
                                    tint = NeonOrange,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Drag finger to rotate 3D Mesh in Space",
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    "VIDEO", "ANIMATION" -> {
                        // Premium Video simulation container (animated canvas backdrop with simulated controls)
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = creation.resultUrl,
                                contentDescription = "Video Thumbnail",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Glassmorphism ambient dark grid mask representing audio sound synthesis
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.55f))
                            )

                            // Sound Waves simulation visualization is sound is enabled
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(NeonOrange.copy(alpha = 0.9f), RoundedCornerShape(27.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Playback Action",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Premium AI Video File Synthesis Output",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )

                                if (creation.soundEffects != "None") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(NeonPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = null,
                                            tint = NeonOrange,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = creation.soundEffects,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Resolution label
                            Box(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${creation.resolution} | ${creation.ratio}",
                                    color = NeonOrange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // Info details and prompt info
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Prompt Input Coordinates:",
                    color = NeonOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = creation.prompt,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pro Synthesis Speed: " + if (creation.isProSpeed) "Enabled (0.6s)" else "Disabled (4.2s)",
                        color = LightGreyText,
                        fontSize = 11.sp
                    )

                    Row {
                        IconButton(onClick = {}) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }
                        IconButton(onClick = {}) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = "Download File", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

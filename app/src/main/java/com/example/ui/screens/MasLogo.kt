package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Reusable gradients based on the Mas AI Logo
val BlueToPurple = Brush.linearGradient(
    colors = listOf(Color(0xFF0055FF), Color(0xFF6700FF), Color(0xFF9E00FF))
)
val PurpleToOrange = Brush.linearGradient(
    colors = listOf(Color(0xFF7B00FF), Color(0xFFFF007F), Color(0xFFFF5E00))
)
val FullLogoGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF0055FF),
        Color(0xFF6B00FE),
        Color(0xFFD400FF),
        Color(0xFFFF007F),
        Color(0xFFFF5E00)
    )
)

@Composable
fun MasLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    showText: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Draw the stylized 3D faceted "M" with PCB circuits logo
        Canvas(
            modifier = Modifier
                .size(size)
                .padding(4.dp)
        ) {
            val w = size.toPx()
            val h = size.toPx()

            // 1. Blue Prism (Left 3D Wing of M)
            // Left facet of left wing (dark blue gradient)
            val leftFacetPath = Path().apply {
                moveTo(w * 0.20f, h * 0.72f)           // bottom-left corner
                lineTo(w * 0.29f, h * 0.38f)           // top apex of left wing
                lineTo(w * 0.31f, h * 0.58f)           // middle ridge divide
                close()
            }
            // Right facet of left wing (light sky blue/cyan gradient)
            val rightFacetPath = Path().apply {
                moveTo(w * 0.31f, h * 0.58f)           // middle ridge divide
                lineTo(w * 0.29f, h * 0.38f)           // top apex of left wing
                lineTo(w * 0.38f, h * 0.73f)           // bottom-right corner of blue leg
                close()
            }

            // Draw left facet
            drawPath(
                path = leftFacetPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF003FB4), Color(0xFF015DED)),
                    start = Offset(w * 0.20f, h * 0.72f),
                    end = Offset(w * 0.31f, h * 0.58f)
                )
            )

            // Draw right facet
            drawPath(
                path = rightFacetPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0077FF), Color(0xFF00E2FF)),
                    start = Offset(w * 0.31f, h * 0.58f),
                    end = Offset(w * 0.38f, h * 0.73f)
                )
            )

            // 2. Purple to Orange Right Wing (valley and right leg ribbon)
            val rightWingPath = Path().apply {
                moveTo(w * 0.38f, h * 0.73f)           // starting where left wing ends
                lineTo(w * 0.47f, h * 0.58f)           // the central valley dip
                lineTo(w * 0.62f, h * 0.38f)           // right apex/peak of M
                lineTo(w * 0.81f, h * 0.75f)           // bottom-right outer tip of M
                lineTo(w * 0.71f, h * 0.75f)           // bottom-right inner corner of M
                lineTo(w * 0.58f, h * 0.50f)           // inner right peak slope
                lineTo(w * 0.48f, h * 0.69f)           // inner bottom V-crotch
                lineTo(w * 0.38f, h * 0.73f)
                close()
            }

            // Draw the right wing with a stunning rich color gradient matching the uploaded logo
            drawPath(
                path = rightWingPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF5D00E2), // dark violet
                        Color(0xFFB500C0), // purple-magenta
                        Color(0xFFFF007F), // bright hot pink
                        Color(0xFFFF5E00)  // neon orange
                    ),
                    start = Offset(w * 0.38f, h * 0.73f),
                    end = Offset(w * 0.81f, h * 0.75f)
                )
            )

            // 3. Electronic circuits (three parallel traces with endpoint nodes slanted up-right)
            val circ1Start = Offset(w * 0.36f, h * 0.50f)
            val circ1End = Offset(w * 0.44f, h * 0.35f)
            
            val circ2Start = Offset(w * 0.43f, h * 0.54f)
            val circ2End = Offset(w * 0.51f, h * 0.39f)

            val circ3Start = Offset(w * 0.50f, h * 0.48f)
            val circ3End = Offset(w * 0.58f, h * 0.33f)

            val tonePurple = Color(0xFF9E00FF)
            val tonePink = Color(0xFFFF007F)
            val toneOrange = Color(0xFFFF5E00)

            // Circuit 1 (Purple / Magenta)
            drawLine(
                brush = Brush.linearGradient(listOf(tonePurple, tonePink)),
                start = circ1Start,
                end = circ1End,
                strokeWidth = w * 0.035f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = tonePurple,
                radius = w * 0.045f,
                center = circ1End
            )

            // Circuit 2 (Pink)
            drawLine(
                brush = Brush.linearGradient(listOf(tonePink, tonePink)),
                start = circ2Start,
                end = circ2End,
                strokeWidth = w * 0.035f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = tonePink,
                radius = w * 0.045f,
                center = circ2End
            )

            // Circuit 3 (Orange / Red)
            drawLine(
                brush = Brush.linearGradient(listOf(tonePink, toneOrange)),
                start = circ3Start,
                end = circ3End,
                strokeWidth = w * 0.035f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = toneOrange,
                radius = w * 0.045f,
                center = circ3End
            )
        }

        if (showText) {
            Spacer(modifier = Modifier.width(8.dp))
            Row {
                Text(
                    text = "Mas ",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal,
                        fontSize = (size.value * 0.45f).sp,
                        color = Color.White
                    )
                )
                Text(
                    text = "AI",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        fontSize = (size.value * 0.45f).sp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6700FF), // vibrant indigo/purple
                                Color(0xFFFF007F), // bright hot pink
                                Color(0xFFFF5E00)  // neon orange
                            )
                        )
                    )
                )
            }
        }
    }
}

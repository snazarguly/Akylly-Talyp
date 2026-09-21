package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppRadius
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PrimaryBrand

@Composable
fun isAppDarkTheme(): Boolean {
    return MaterialTheme.colorScheme.surface == DarkSurface ||
        MaterialTheme.colorScheme.background == DarkBg ||
        MaterialTheme.colorScheme.surface.luminance() < 0.5f
}

/**
 * Returns a glassmorphism frosted gradient brush based on the active theme.
 */
@Composable
fun glassBackgroundBrush(
    isDark: Boolean = isAppDarkTheme(),
    alphaTop: Float = if (isDark) 0.76f else 0.94f,
    alphaBottom: Float = if (isDark) 0.64f else 0.86f
): Brush {
    return if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF1E252D).copy(alpha = alphaTop),
                Color(0xFF13171C).copy(alpha = alphaBottom)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFFFFFFF).copy(alpha = alphaTop),
                Color(0xFFF8FAFC).copy(alpha = alphaBottom)
            )
        )
    }
}

/**
 * Returns a specular glass refraction border stroke based on the active theme.
 */
@Composable
fun glassBorderStroke(
    isDark: Boolean = isAppDarkTheme(),
    width: Dp = 1.dp
): BorderStroke {
    return if (isDark) {
        BorderStroke(
            width,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.08f),
                    Color(0xFF38BDF8).copy(alpha = 0.15f)
                )
            )
        )
    } else {
        BorderStroke(
            width,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color(0xFFCBD5E1).copy(alpha = 0.60f),
                    PrimaryBrand.copy(alpha = 0.15f)
                )
            )
        )
    }
}

/**
 * Modifier to apply glassmorphism style (shadow, clip, background gradient, and specular border)
 */
@Composable
fun Modifier.glassEffect(
    shape: Shape = RoundedCornerShape(AppRadius.card),
    elevation: Dp = 4.dp,
    borderWidth: Dp = 1.dp,
    alphaTop: Float = if (isAppDarkTheme()) 0.76f else 0.94f,
    alphaBottom: Float = if (isAppDarkTheme()) 0.64f else 0.86f
): Modifier {
    val isDark = isAppDarkTheme()
    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) Color.Black.copy(alpha = 0.35f) else Color(0x0A0F172A),
            spotColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color(0x140F172A)
        )
        .clip(shape)
        .background(glassBackgroundBrush(isDark = isDark, alphaTop = alphaTop, alphaBottom = alphaBottom))
        .border(glassBorderStroke(isDark = isDark, width = borderWidth), shape)
}

/**
 * Reusable Glassmorphism Card
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(AppRadius.card),
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    testTag: String = "glass_card",
    content: @Composable () -> Unit
) {
    val isDark = isAppDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.35f) else Color(0x0A0F172A),
                spotColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color(0x140F172A)
            )
            .clip(shape)
            .background(glassBackgroundBrush(isDark = isDark))
            .border(glassBorderStroke(isDark = isDark), shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = PrimaryBrand.copy(alpha = 0.2f)),
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .testTag(testTag)
    ) {
        content()
    }
}

package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppComponentSize
import com.example.ui.theme.AppIconSize
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppTypography
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PrimaryBrand

data class GlassNavTabItem(
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val testTag: String
)

/**
 * Modern iOS-inspired Glassmorphism Bottom Navigation Bar.
 *
 * Implements:
 * - Ultra-sleek reduced height (60.dp)
 * - 2px enlarged navigation icons (26.dp)
 * - High-contrast, crystal-clear optics in BOTH Dark and Light modes
 * - Frosted liquid glass multi-stop translucent gradients
 * - Specular border highlight mimicking light refraction
 * - Inner specular highlight shimmer along the top rim
 * - Soft ambient & colored glow drop shadow
 * - Floating rounded pill layout with safe-area spacing
 * - Tactile active state with soft translucent cyan glow and smooth spring animation
 */
@Composable
fun GlassBottomNavigation(
    tabs: List<GlassNavTabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface == DarkSurface ||
        MaterialTheme.colorScheme.background == DarkBg ||
        MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val navBarInsets = WindowInsets.navigationBars.asPaddingValues()
    val bottomSafePadding = navBarInsets.calculateBottomPadding()

    // Authentic translucent frosted glass background
    val glassBgGradient = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF1E252D).copy(alpha = 0.72f),
                Color(0xFF13171C).copy(alpha = 0.65f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFFFFFFF).copy(alpha = 0.78f),
                Color(0xFFF1F5F9).copy(alpha = 0.65f)
            )
        )
    }

    // Specular glass refraction rim border
    val glassBorder = if (isDark) {
        BorderStroke(
            1.dp,
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
            1.dp,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.90f),
                    Color.White.copy(alpha = 0.45f),
                    PrimaryBrand.copy(alpha = 0.20f)
                )
            )
        )
    }

    val shadowSpotColor = if (isDark) {
        Color.Black.copy(alpha = 0.45f)
    } else {
        Color(0x18000000)
    }

    val shadowAmbientColor = if (isDark) {
        Color.Black.copy(alpha = 0.30f)
    } else {
        Color(0x0A000000)
    }

    // Outer alignment wrapper with feather gradient backdrop and safe-area aware positioning
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Feather gradient backdrop: softly dissolves scrolling content behind the floating glass nav bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppComponentSize.bottomNavHeight + bottomSafePadding + AppComponentSize.bottomNavBottomSpacing + 28.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = AppComponentSize.bottomNavHorizontalMargin,
                    end = AppComponentSize.bottomNavHorizontalMargin,
                    bottom = bottomSafePadding + AppComponentSize.bottomNavBottomSpacing
                ),
            contentAlignment = Alignment.Center
        ) {
            // Floating pill container with shadow and maxContentWidth responsiveness
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = AppComponentSize.maxContentWidth)
                    .height(AppComponentSize.bottomNavHeight)
                .shadow(
                    elevation = if (isDark) 12.dp else 10.dp,
                    shape = RoundedCornerShape(AppRadius.glassNav),
                    spotColor = shadowSpotColor,
                    ambientColor = shadowAmbientColor,
                    clip = false
                )
                .clip(RoundedCornerShape(AppRadius.glassNav))
                .background(glassBgGradient)
                .border(glassBorder, RoundedCornerShape(AppRadius.glassNav))
                .testTag("glass_bottom_navigation")
        ) {
            // Inner glass highlight - top specular reflection line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 20.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = if (isDark) 0.18f else 0.95f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Navigation items container
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tabItem ->
                    val isSelected = selectedTabIndex == index

                    GlassNavItemCell(
                        tabItem = tabItem,
                        isSelected = isSelected,
                        onClick = { onTabSelected(index) },
                        isDark = isDark,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}
}

@Composable
private fun GlassNavItemCell(
    tabItem: GlassNavTabItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    // Smooth spring scale animation on tap & active state
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_tab_scale"
    )

    // Clear, high-contrast unselected and selected content colors for both modes
    val unselectedColor = if (isDark) Color(0xFF9AA5B1) else Color(0xFF4B5563)
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryBrand else unselectedColor,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "nav_content_color"
    )

    // Soft active pill glow background
    val activePillBg = if (isSelected) {
        PrimaryBrand.copy(alpha = if (isDark) 0.22f else 0.14f)
    } else {
        Color.Transparent
    }

    val activePillBorder = if (isSelected) {
        BorderStroke(
            0.5.dp,
            PrimaryBrand.copy(alpha = if (isDark) 0.38f else 0.30f)
        )
    } else {
        BorderStroke(0.dp, Color.Transparent)
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.large))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 32.dp, color = PrimaryBrand.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .testTag(tabItem.testTag),
        contentAlignment = Alignment.Center
    ) {
        // Active indicator capsule
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(activePillBg)
                .border(activePillBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isSelected) tabItem.filledIcon else tabItem.outlinedIcon,
                    contentDescription = tabItem.label,
                    tint = contentColor,
                    modifier = Modifier.size(AppIconSize.navBarIcon) // 26.dp (enlarged by 2px)
                )

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = tabItem.label,
                    style = AppTypography.Caption.copy(
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = contentColor,
                    maxLines = 1
                )
            }
        }
    }
}

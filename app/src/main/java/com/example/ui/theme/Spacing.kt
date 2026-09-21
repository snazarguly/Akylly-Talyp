package com.example.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Centralized Spacing System - 8pt grid with 4pt half-steps
 * No magic numbers allowed in layout spacing!
 */
object AppSpacing {
    val none = 0.dp
    val xxs = 4.dp
    val xs = 8.dp
    val s = 12.dp
    val m = 16.dp
    val l = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 40.dp
    val huge = 48.dp

    // Standardized Structural Layout Spacing
    val screenHorizontal = 16.dp
    val cardInternal = 16.dp
    val sectionSpacing = 20.dp
    val itemSpacing = 12.dp
    val cardGap = 12.dp
    val rowGap = 8.dp
}

/**
 * Standardized Icon Sizes
 */
object AppIconSize {
    val micro = 14.dp
    val small = 18.dp
    val medium = 24.dp
    val navBarIcon = 26.dp // 2px enlarged for bottom navigation
    val large = 32.dp
    val xl = 40.dp
    val huge = 48.dp
}

/**
 * Standardized Component Heights & Dimensions
 */
object AppComponentSize {
    val minTouchTarget = 48.dp
    val buttonHeight = 48.dp
    val buttonHeightSmall = 40.dp
    val touchTargetCompact = 40.dp
    val inputHeight = 52.dp
    val segmentedHeight = 44.dp
    val bottomNavHeight = 60.dp // Reduced from 72.dp for compact elegance
    val bottomNavHorizontalMargin = 16.dp
    val bottomNavBottomSpacing = 8.dp
    val bottomNavContentPadding = 88.dp
    val avatarSize = 64.dp
    val avatarLarge = 64.dp
    val avatarSizeLarge = 80.dp
    val iconBoxSmall = 32.dp
    val iconBoxMedium = 40.dp
    val optionLetterBox = 32.dp
    val maxContentWidth = 640.dp
}


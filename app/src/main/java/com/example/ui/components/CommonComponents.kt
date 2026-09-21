package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.ui.theme.AppComponentSize
import com.example.ui.theme.AppIconSize
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppTypography
import com.example.ui.theme.PrimaryBrand
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

/**
 * Standard Centralized Primary Button
 */
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    testTag: String = "app_primary_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(AppRadius.medium),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        modifier = modifier
            .heightIn(min = AppComponentSize.buttonHeight)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(AppIconSize.small)
                )
                Spacer(modifier = Modifier.width(AppSpacing.xs))
            }
            Text(
                text = text,
                style = AppTypography.Headline.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Standard Centralized Secondary Outlined Button
 */
@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    testTag: String = "app_secondary_button"
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(AppRadius.medium),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .heightIn(min = AppComponentSize.buttonHeight)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(AppIconSize.small)
                )
                Spacer(modifier = Modifier.width(AppSpacing.xs))
            }
            Text(
                text = text,
                style = AppTypography.Headline.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Standard Centralized Card Component with Glassmorphism
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    testTag: String = "app_card",
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        testTag = testTag,
        content = content
    )
}

/**
 * Apple/Modern iOS-Inspired Glass Segmented Control
 */
@Composable
fun AppSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "segmented_control"
) {
    val isDark = isAppDarkTheme()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppComponentSize.segmentedHeight)
            .glassEffect(shape = RoundedCornerShape(AppRadius.medium), elevation = 2.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .padding(AppSpacing.xxs)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, title ->
                val isSelected = index == selectedIndex
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        if (isDark) Color.White else PrimaryBrand
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    animationSpec = tween(150),
                    label = "segmented_text"
                )

                val itemModifier = if (isSelected) {
                    Modifier
                        .shadow(2.dp, RoundedCornerShape(AppRadius.small))
                        .clip(RoundedCornerShape(AppRadius.small))
                        .background(
                            if (isDark) Color(0xFF26323D).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f)
                        )
                        .border(
                            1.dp,
                            if (isDark) Color.White.copy(alpha = 0.18f) else Color.White,
                            RoundedCornerShape(AppRadius.small)
                        )
                } else {
                    Modifier.clip(RoundedCornerShape(AppRadius.small))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .then(itemModifier)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onItemSelected(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = AppTypography.Headline.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Status Pill Badge with Glass Accent
 */
@Composable
fun AppStatusBadge(
    text: String,
    color: Color = PrimaryBrand,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.xs))
            .background(color.copy(alpha = 0.14f))
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        color.copy(alpha = 0.40f),
                        color.copy(alpha = 0.15f)
                    )
                ),
                RoundedCornerShape(AppRadius.xs)
            )
            .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs)
    ) {
        Text(
            text = text,
            style = AppTypography.Caption.copy(
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
    }
}

/**
 * Standardized Section Header with Consistent Padding & Alignment
 */
@Composable
fun AppSectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTypography.Title.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!actionText.isNullOrBlank() && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs)
            ) {
                Text(
                    text = actionText,
                    style = AppTypography.Headline.copy(
                        color = PrimaryBrand,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
fun OfflineBanner(
    isOffline: Boolean,
    message: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOffline,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.xxs)
                .glassEffect(shape = RoundedCornerShape(AppRadius.small), elevation = 2.dp)
                .border(
                    1.dp,
                    StatusWarning.copy(alpha = 0.35f),
                    RoundedCornerShape(AppRadius.small)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.m, vertical = AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = StatusWarning,
                    modifier = Modifier.size(AppIconSize.small)
                )
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                Text(
                    text = message,
                    style = AppTypography.BodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AppTopHeader(
    title: String,
    subtitle: String? = null,
    strings: AppStrings,
    unreadCount: Int,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = AppTypography.Title,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                AppStatusBadge(
                    text = strings.demoModeBadge,
                    color = PrimaryBrand
                )
            }
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                Text(
                    text = subtitle,
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .size(AppComponentSize.minTouchTarget)
                    .glassEffect(shape = CircleShape, elevation = 2.dp)
                    .testTag("top_search_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(AppIconSize.medium)
                )
            }

            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(AppComponentSize.minTouchTarget)
                    .glassEffect(shape = CircleShape, elevation = 2.dp)
                    .testTag("top_notification_btn")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = StatusError,
                                contentColor = Color.White
                            ) {
                                Text(text = unreadCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(AppIconSize.medium)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Inbox,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(AppSpacing.m))
        Text(
            text = title,
            style = AppTypography.Headline,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxs))
        Text(
            text = subtitle,
            style = AppTypography.BodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorStateView(
    title: String,
    retryText: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = StatusError,
            modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(AppSpacing.m))
        Text(
            text = title,
            style = AppTypography.Headline,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(AppSpacing.m))
        AppPrimaryButton(
            text = retryText,
            onClick = onRetry
        )
    }
}

@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.xs)
    ) {
        Column(modifier = Modifier.padding(AppSpacing.cardInternal)) {
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            )
            Spacer(modifier = Modifier.height(AppSpacing.s))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            )
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            )
        }
    }
}


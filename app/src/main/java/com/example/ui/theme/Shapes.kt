package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object AppRadius {
    val xxs = 4.dp
    val xs = 6.dp
    val small = 8.dp
    val medium = 14.dp
    val large = 18.dp
    val card = 18.dp
    val glassNav = 32.dp
    val pill = 999.dp
    val full = 999.dp
}

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(AppRadius.xxs),
    small = RoundedCornerShape(AppRadius.small),
    medium = RoundedCornerShape(AppRadius.medium),
    large = RoundedCornerShape(AppRadius.large)
)


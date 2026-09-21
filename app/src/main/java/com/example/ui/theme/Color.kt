package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Brand Color
val PrimaryBrand = Color(0xFF007A87) // Refined Deep Ocean Teal (WCAG AA compliant >4.5:1 against white)
val PrimaryBrandDark = Color(0xFF004D54)
val PrimaryBrandContainer = Color(0xFFE0F4F7)

// Compatibility aliases
val AcademicTeal = PrimaryBrand
val PrimaryDark = Color(0xFF004D54)
val SecondaryTeal = Color(0xFF006D77)
val LightAccent = Color(0xFFE0F4F7)

// Neutral Palette - Light (Elevated contrast, crisp boundaries, modern slate canvas)
val LightBg = Color(0xFFEFF3F7) // Soft cool-slate canvas allowing white/glass cards to pop
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceSubtle = Color(0xFFE2E8F0) // Refined subtle container color
val LightBorder = Color(0xFFCBD5E1) // Crisp clean borders
val LightTextPrimary = Color(0xFF0F172A) // Deep slate-900 high contrast
val LightTextSecondary = Color(0xFF334155) // Slate-700 rich readable text
val LightTextTertiary = Color(0xFF64748B) // Slate-500 crisp metadata & captions

// Neutral Palette - Dark
val DarkBg = Color(0xFF0F1215) // Near-black / dark neutral
val DarkSurface = Color(0xFF171B20)
val DarkSurfaceSubtle = Color(0xFF21262D)
val DarkBorder = Color(0xFF283038)
val DarkTextPrimary = Color(0xFFF3F4F6) // High contrast
val DarkTextSecondary = Color(0xFF9CA3AF) // Muted
val DarkTextTertiary = Color(0xFF6B7280) // Subtle

// Backward compatibility aliases
val BackgroundLight = LightBg
val SurfaceLight = LightSurface
val TextPrimary = LightTextPrimary
val TextSecondary = LightTextSecondary
val BorderLight = LightBorder
val DarkBackground = DarkBg
val DarkSurfaceVariant = DarkSurfaceSubtle

// Semantic Status Colors (Vibrant, high-contrast, modern tones)
val StatusSuccess = Color(0xFF16A34A) // Vibrant Emerald 600
val StatusSuccessContainer = Color(0xFFDCFCE7)

val StatusError = Color(0xFFDC2626) // Vivid Red 600
val StatusErrorContainer = Color(0xFFFEE2E2)

val StatusWarning = Color(0xFFD97706) // Rich Amber 600
val StatusWarningContainer = Color(0xFFFEF3C7)

val StatusInfo = Color(0xFF0284C7) // Sky 600
val StatusInfoContainer = Color(0xFFE0F2FE)

// Backward compatibility status aliases
val SuccessGreen = StatusSuccess
val WarningAmber = StatusWarning
val ErrorRed = StatusError


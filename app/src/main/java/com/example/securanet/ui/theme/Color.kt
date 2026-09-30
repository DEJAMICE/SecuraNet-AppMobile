package com.example.securanet.ui.theme

import androidx.compose.ui.graphics.Color

// ── SecuraNet brand palette ──────────────────────────────────────────────────

/** Deep navy – primary actions, header backgrounds, filled buttons. */
val NavyPrimary = Color(0xFF1A2B4A)

/** Slightly lighter navy for dark-theme primary. */
val NavyPrimaryLight = Color(0xFF2E4270)

/** Container tint used for primary buttons on dark surfaces. */
val NavyPrimaryContainer = Color(0xFF26395F)

/** Alert red – SOS button, error states, progress ring. */
val SosRed = Color(0xFFD32F2F)

/** Light tint of the alert red (container / halo). */
val SosRedLight = Color(0xFFFDE8E8)

/** Very soft red halo drawn behind the SOS button. */
val SosHalo = Color(0x33D32F2F)   // 20 % opacity red

/** Selected-item indicator pill in the bottom navigation bar (light navy tint). */
val NavBarIndicator = Color(0xFFDDE5F4)

/** Off-white page background (warm hint to separate from pure white cards). */
val SoftBackground = Color(0xFFF5F8FA)

/** Pure white – card surfaces, text on dark backgrounds. */
val SurfaceWhite = Color(0xFFFFFFFF)

/** Map grid / subtle divider lines. */
val MapGrid = Color(0xFFDDE3EA)

/** Map background fill. */
val MapBackground = Color(0xFFE8EEF4)

/** Hatched risk-zone fill. */
val RiskZoneFill = Color(0x55D32F2F)   // translucent red

/** Risk-zone border (dashed). */
val RiskZoneBorder = Color(0xFFD32F2F)

/** Text and icons on dark (navy) surfaces. */
val OnNavy = Color(0xFFFFFFFF)

/** Secondary text on light backgrounds. */
val OnSurfaceVariantLight = Color(0xFF5C6370)

// ── Devices Tab specific semantic colors ─────────────────────────────────────

/** Active badge background (soft green pill). */
val ActiveBadgeBackground = Color(0xFFE8F5E9)

/** Active badge text (dark green). */
val ActiveBadgeText = Color(0xFF2E7D32)

/** Disconnected / Inactive badge background (soft neutral grey). */
val NeutralBadgeBackground = Color(0xFFF1F5F9)

/** Disconnected / Inactive badge text (slate grey). */
val NeutralBadgeText = Color(0xFF64748B)

/** Secondary gray button background (Unlink / cancel secondary actions). */
val SecondaryButtonBackground = Color(0xFFF1F5F9)

/** Secondary gray button text color. */
val SecondaryButtonText = Color(0xFF1E293B)

/** Low battery / attention banner background (soft red). */
val BannerAttentionBackground = Color(0xFFFDF2F2)

/** Low battery / attention banner border / text. */
val BannerAttentionText = Color(0xFF991B1B)

/** Disconnected banner background (soft slate grey). */
val BannerDroppedBackground = Color(0xFFF8FAFC)

/** Disconnected banner text. */
val BannerDroppedText = Color(0xFF334155)

// ── Legacy default colours (kept so old theme references don't break) ────────
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)
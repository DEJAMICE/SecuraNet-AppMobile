package com.example.securanet.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.securanet.R
import com.example.securanet.domain.model.Contact
import com.example.securanet.presentation.sos.SosPhase
import com.example.securanet.presentation.sos.SosViewModel
import com.example.securanet.ui.theme.MapBackground
import com.example.securanet.ui.theme.MapGrid
import com.example.securanet.ui.theme.RiskZoneBorder
import com.example.securanet.ui.theme.RiskZoneFill
import com.example.securanet.ui.theme.SosHalo
import com.example.securanet.ui.theme.SosRed

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    viewModel: SosViewModel,
    onNavigateToTrustedNetwork: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    when (state.phase) {
        SosPhase.IDLE, SosPhase.HOLDING -> HomeIdleScreen(
            holdProgress = state.holdProgress,
            isHolding    = state.phase == SosPhase.HOLDING,
            onHoldStart  = viewModel::onSosHoldStart,
            onHoldRelease = viewModel::onSosHoldRelease
        )
        SosPhase.COUNTDOWN -> CountdownScreen(
            seconds    = state.countdownSeconds,
            onCancel   = viewModel::cancelCountdown,
            onSendNow  = viewModel::confirmSend
        )
        SosPhase.SENT -> SosSentScreen(
            priorityContacts         = state.priorityContacts,
            hasNoContacts            = state.hasNoContacts,
            hasContactsButNoPriority = state.hasContactsButNoPriority,
            cancelHoldProgress       = state.cancelHoldProgress,
            onCancelAlertHoldStart   = viewModel::onCancelAlertHoldStart,
            onCancelAlertHoldRelease = viewModel::onCancelAlertHoldRelease,
            onNavigateToTrustedNetwork = {
                viewModel.cancelCountdown()
                onNavigateToTrustedNetwork()
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// IDLE / HOLDING — map + SOS button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomeIdleScreen(
    holdProgress: Float,
    isHolding: Boolean,
    onHoldStart: () -> Unit,
    onHoldRelease: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        MapPlaceholder(modifier = Modifier.fillMaxSize())

        SosHoldButton(
            holdProgress  = holdProgress,
            isHolding     = isHolding,
            onHoldStart   = onHoldStart,
            onHoldRelease = onHoldRelease,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SOS hold button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SosHoldButton(
    holdProgress: Float,
    isHolding: Boolean,
    onHoldStart: () -> Unit,
    onHoldRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue    = holdProgress,
        animationSpec  = tween(durationMillis = 80),
        label          = "sosHoldProgress"
    )
    val buttonDesc = stringResource(R.string.sos_button_desc)
    val caption = if (isHolding)
        stringResource(R.string.sos_release_to_cancel)
    else
        stringResource(R.string.sos_hold_instruction)

    Column(
        modifier              = modifier,
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(10.dp)
    ) {
        // Outer halo + progress ring + inner circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(148.dp)
                .semantics { contentDescription = buttonDesc }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onHoldStart()
                            try { awaitRelease() } finally { onHoldRelease() }
                        }
                    )
                }
        ) {
            // Soft halo (static, behind everything)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = SosHalo, radius = size.minDimension / 2f)
            }

            // Progress arc drawn on a slightly smaller canvas to leave halo visible
            Canvas(modifier = Modifier.size(120.dp)) {
                val strokePx = 6.dp.toPx()
                val inset    = strokePx / 2f
                val arcSize  = Size(size.width - strokePx, size.height - strokePx)
                // Track ring (light red)
                drawArc(
                    color      = SosRed.copy(alpha = 0.20f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter  = false,
                    topLeft    = Offset(inset, inset),
                    size       = arcSize,
                    style      = Stroke(strokePx, cap = StrokeCap.Round)
                )
                // Fill arc (bright red)
                if (animatedProgress > 0f) {
                    drawArc(
                        color      = SosRed,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter  = false,
                        topLeft    = Offset(inset, inset),
                        size       = arcSize,
                        style      = Stroke(strokePx, cap = StrokeCap.Round)
                    )
                }
            }

            // Filled circle button
            Surface(
                shape          = CircleShape,
                color          = SosRed,
                shadowElevation = 8.dp,
                modifier       = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text        = stringResource(R.string.sos_button),
                        color       = Color.White,
                        fontWeight  = FontWeight.ExtraBold,
                        fontSize    = 22.sp,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        // Caption below the button
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.White.copy(alpha = 0.85f)
        ) {
            Text(
                text     = caption,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style    = MaterialTheme.typography.labelMedium,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Map placeholder (Canvas) – styled like the design image
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MapPlaceholder(modifier: Modifier = Modifier) {
    val dotNavy = Color(0xFF1A2B4A)

    Canvas(modifier = modifier.background(MapBackground)) {
        val w    = size.width
        val h    = size.height
        val step = 56.dp.toPx()
        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 7f))

        // Grid lines
        var x = 0f
        while (x <= w) { drawLine(MapGrid, Offset(x, 0f), Offset(x, h), 1.dp.toPx()); x += step }
        var y = 0f
        while (y <= h) { drawLine(MapGrid, Offset(0f, y), Offset(w, y), 1.dp.toPx()); y += step }

        // Risk zone 1 – upper right (diagonal hatch simulation with fill + border)
        val rz1 = Offset(w * 0.54f, h * 0.10f)
        val rz1s = Size(w * 0.32f, h * 0.20f)
        drawRect(RiskZoneFill, topLeft = rz1, size = rz1s)
        drawRect(RiskZoneBorder, topLeft = rz1, size = rz1s,
            style = Stroke(2.dp.toPx(), pathEffect = dash))

        // Risk zone 2 – lower left
        val rz2 = Offset(w * 0.04f, h * 0.57f)
        val rz2s = Size(w * 0.30f, h * 0.18f)
        drawRect(RiskZoneFill, topLeft = rz2, size = rz2s)
        drawRect(RiskZoneBorder, topLeft = rz2, size = rz2s,
            style = Stroke(2.dp.toPx(), pathEffect = dash))

        // Location dot – grey outer ring, navy fill (matches design)
        val cx = w * 0.40f
        val cy = h * 0.47f
        drawCircle(Color(0xFFAAAAAA), 22.dp.toPx(), Offset(cx, cy))   // shadow/ring
        drawCircle(Color.White,       18.dp.toPx(), Offset(cx, cy))
        drawCircle(Color(0xFF555555), 10.dp.toPx(), Offset(cx, cy))   // grey centre
        drawCircle(dotNavy,            5.dp.toPx(), Offset(cx, cy))   // navy pip
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COUNTDOWN — full screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CountdownScreen(
    seconds: Int,
    onCancel: () -> Unit,
    onSendNow: () -> Unit
) {
    BackHandler(enabled = true) { /* swallow back */ }

    val context = LocalContext.current
    DisposableEffect(Unit) {
        val vibrator = getVibrator(context)
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0L, 400L, 200L), 0))
        onDispose { vibrator.cancel() }
    }

    val cancelDesc   = stringResource(R.string.sos_im_okay_cancel)
    val sendNowDesc  = stringResource(R.string.sos_send_now_desc)
    val countdownDesc = stringResource(R.string.sos_countdown_seconds_desc, seconds)

    // Soft pinkish background matching the screenshot
    val pageBg = SosRed.copy(alpha = 0.04f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Subtle gradient wash at the top
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)) {
            drawRect(color = pageBg)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(56.dp))

            // ── Top: icon + title + subtitle ─────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Circular red icon background
                Surface(
                    shape = CircleShape,
                    color = SosRed.copy(alpha = 0.12f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter            = painterResource(R.drawable.warning_24px),
                            contentDescription = null,
                            tint               = SosRed,
                            modifier           = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    text       = stringResource(R.string.sos_countdown_title),
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    color      = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text      = stringResource(R.string.sos_countdown_subtitle),
                    style     = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Middle: countdown ring ────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(180.dp)
                    .semantics {
                        liveRegion       = LiveRegionMode.Assertive
                        contentDescription = countdownDesc
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokePx = 8.dp.toPx()
                    val inset    = strokePx / 2f
                    val arcSize  = Size(size.width - strokePx, size.height - strokePx)
                    // Light track
                    drawArc(
                        color      = SosRed.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter  = false,
                        topLeft    = Offset(inset, inset),
                        size       = arcSize,
                        style      = Stroke(strokePx, cap = StrokeCap.Round)
                    )
                    // Red arc (5 - seconds / 5 to show how much has elapsed)
                    val elapsed = 1f - (seconds / 5f)
                    drawArc(
                        color      = SosRed,
                        startAngle = -90f,
                        sweepAngle = 360f * elapsed,
                        useCenter  = false,
                        topLeft    = Offset(inset, inset),
                        size       = arcSize,
                        style      = Stroke(strokePx, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = seconds.toString(),
                        fontSize   = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text  = stringResource(R.string.sos_countdown_seconds_desc, seconds)
                            .substringAfterLast(" ").uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Bottom: action buttons ────────────────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 36.dp)
            ) {
                // "I'm okay – Cancel" → filled navy
                Button(
                    onClick  = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .semantics { contentDescription = cancelDesc },
                    shape  = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,   // navy
                        contentColor   = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.check_circle_24px),
                        contentDescription = null,
                        modifier           = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.sos_im_okay_cancel),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // "Send SOS now" → outlined red
                OutlinedButton(
                    onClick  = onSendNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .semantics { contentDescription = sendNowDesc },
                    shape  = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SosRed),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SosRed)
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.notifications_active_24px),
                        contentDescription = null,
                        modifier           = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.sos_send_now),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SENT — full screen, scrollable body + pinned footer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SosSentScreen(
    priorityContacts: List<Contact>,
    hasNoContacts: Boolean,
    hasContactsButNoPriority: Boolean,
    cancelHoldProgress: Float,
    onCancelAlertHoldStart: () -> Unit,
    onCancelAlertHoldRelease: () -> Unit,
    onNavigateToTrustedNetwork: () -> Unit
) {
    BackHandler(enabled = true) { /* swallow */ }

    val context      = LocalContext.current
    val callDesc     = stringResource(R.string.sos_call_emergency_desc)
    val cancelDesc   = stringResource(R.string.sos_cancel_alert_desc)
    val anyNotified  = priorityContacts.isNotEmpty()
    val subtitle     = if (anyNotified)
        stringResource(R.string.sos_alert_sent_subtitle_contacts)
    else
        stringResource(R.string.sos_alert_sent_subtitle_none)

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Rounded navy header ───────────────────────────────────────────────
        Surface(
            color  = MaterialTheme.colorScheme.primary,           // navy
            shape  = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier              = Modifier.padding(top = 32.dp, bottom = 28.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment   = Alignment.CenterHorizontally
            ) {
                // Red circle with bell icon
                Surface(
                    shape = CircleShape,
                    color = SosRed,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter            = painterResource(R.drawable.notifications_active_24px),
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text       = stringResource(R.string.sos_alert_sent_title),
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White,
                    textAlign  = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text      = subtitle,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = Color.White.copy(alpha = 0.80f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // ── Scrollable body ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                hasNoContacts -> InfoSection(
                    iconRes     = R.drawable.warning_24px,
                    message     = stringResource(R.string.sos_no_contacts_message),
                    actionLabel = stringResource(R.string.sos_add_trusted_contact),
                    onAction    = onNavigateToTrustedNetwork
                )
                hasContactsButNoPriority -> InfoSection(
                    iconRes     = R.drawable.warning_24px,
                    message     = stringResource(R.string.sos_no_priority_message),
                    actionLabel = stringResource(R.string.sos_go_to_trusted_network),
                    onAction    = onNavigateToTrustedNetwork
                )
                else -> {
                    NotifiedContactsCard(contacts = priorityContacts)
                }
            }

            // Simulated location section
            SectionLabel(stringResource(R.string.sos_simulated_location))
            Surface(
                shape           = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp,
                modifier        = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                MapPlaceholder(modifier = Modifier.fillMaxSize())
            }

            // Disclaimer
            Text(
                text      = stringResource(R.string.sos_prototype_disclaimer),
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
        }

        // ── Pinned action buttons ─────────────────────────────────────────────
        Surface(shadowElevation = 8.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp)),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Call 105 — red outlined
                OutlinedButton(
                    onClick  = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:105"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .semantics { contentDescription = callDesc },
                    shape  = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SosRed),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SosRed)
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.call_24px),
                        contentDescription = null,
                        modifier           = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.sos_call_emergency),
                        fontWeight = FontWeight.Medium
                    )
                }

                // I'm safe — hold 2 s, navy fill with animated progress
                CancelAlertHoldButton(
                    holdProgress  = cancelHoldProgress,
                    onHoldStart   = onCancelAlertHoldStart,
                    onHoldRelease = onCancelAlertHoldRelease,
                    description   = cancelDesc
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable sub-composables
// ─────────────────────────────────────────────────────────────────────────────

/** Card listing all notified priority contacts with green check icons. */
@Composable
private fun NotifiedContactsCard(contacts: List<Contact>) {
    Surface(
        shape           = RoundedCornerShape(16.dp),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier        = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text       = stringResource(R.string.sos_priority_notified, contacts.size),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            contacts.forEach { contact -> NotifiedContactRow(contact) }
        }
    }
}

@Composable
private fun NotifiedContactRow(contact: Contact) {
    Row(
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Icon(
            painter            = painterResource(R.drawable.check_circle_24px),
            contentDescription = null,
            tint               = Color(0xFF2E7D32),   // green check
            modifier           = Modifier.size(22.dp)
        )
        Column {
            Text(contact.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (contact.relationship.isNotBlank()) {
                Text(
                    contact.relationship,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Red info card with a message and an action button (no-contacts / no-priority states). */
@Composable
private fun InfoSection(
    iconRes: Int,
    message: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = SosRed.copy(alpha = 0.07f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment     = Alignment.Top
            ) {
                Icon(
                    painterResource(iconRes),
                    contentDescription = null,
                    tint               = SosRed,
                    modifier           = Modifier.size(22.dp)
                )
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SosRed
                )
            }
            Spacer(Modifier.height(12.dp))
            // Red filled action button (matches right-variant design)
            Button(
                onClick  = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape  = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SosRed,
                    contentColor   = Color.White
                )
            ) {
                Text(actionLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color      = MaterialTheme.colorScheme.onSurface
    )
}

/** "I'm safe – Cancel alert" button with an animated navy fill over 2 s. */
@Composable
private fun CancelAlertHoldButton(
    holdProgress: Float,
    onHoldStart: () -> Unit,
    onHoldRelease: () -> Unit,
    description: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue   = holdProgress,
        animationSpec = tween(80),
        label         = "cancelHoldProgress"
    )
    val navy       = MaterialTheme.colorScheme.primary          // navy
    val navyLight  = MaterialTheme.colorScheme.primaryContainer

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .semantics { contentDescription = description }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onHoldStart()
                        try { awaitRelease() } finally { onHoldRelease() }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = CornerRadius(28.dp.toPx())
            // Background pill (light navy tint)
            drawRoundRect(color = navyLight, cornerRadius = radius)
            // Fill progress (dark navy)
            drawRoundRect(
                color        = navy,
                size         = Size(size.width * animatedProgress, size.height),
                cornerRadius = radius
            )
        }
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter            = painterResource(R.drawable.check_circle_24px),
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(20.dp)
            )
            Text(
                text       = stringResource(R.string.sos_cancel_alert),
                fontWeight = FontWeight.SemiBold,
                color      = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Vibrator helper (API-level safe)
// ─────────────────────────────────────────────────────────────────────────────

private fun getVibrator(context: Context): Vibrator =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

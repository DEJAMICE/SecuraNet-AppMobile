package com.example.securanet.ui.home

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.securanet.R
import com.example.securanet.presentation.sos.SosPhase
import com.example.securanet.presentation.sos.SosViewModel

@Composable
fun HomeScreen(viewModel: SosViewModel) {
    val state by viewModel.state.collectAsState()

    when (state.phase) {
        SosPhase.IDLE, SosPhase.HOLDING -> {
            HomeIdleContent(
                holdProgress = state.holdProgress,
                isHolding = state.phase == SosPhase.HOLDING,
                onHoldStart = viewModel::onHoldStart,
                onHoldRelease = viewModel::onHoldRelease
            )
        }
        SosPhase.COUNTDOWN -> {
            SosCountdownContent(
                seconds = state.countdownSeconds,
                onCancel = viewModel::cancelCountdown,
                onSendNow = viewModel::confirmSend
            )
        }
        SosPhase.SENT -> {
            SosSentContent(
                notifiedContacts = state.notifiedContacts,
                hasNoContacts = state.hasNoContacts,
                hasContactsButNoPriority = state.hasContactsButNoPriority,
                onCancelAlert = viewModel::cancelAlert
            )
        }
    }
}

// ---------------------------------------------------------------------------
// IDLE / HOLDING phase — Map + SOS button
// ---------------------------------------------------------------------------

@Composable
private fun HomeIdleContent(
    holdProgress: Float,
    isHolding: Boolean,
    onHoldStart: () -> Unit,
    onHoldRelease: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Map placeholder occupies full background
        MapPlaceholder(modifier = Modifier.fillMaxSize())

        // Title badge at the top
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.home_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(id = R.string.home_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // SOS button at the bottom center
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SosHoldButton(
                holdProgress = holdProgress,
                isHolding = isHolding,
                onHoldStart = onHoldStart,
                onHoldRelease = onHoldRelease
            )
            val instruction = if (isHolding)
                stringResource(id = R.string.sos_release_to_cancel)
            else
                stringResource(id = R.string.sos_hold_instruction)
            Text(
                text = instruction,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun SosHoldButton(
    holdProgress: Float,
    isHolding: Boolean,
    onHoldStart: () -> Unit,
    onHoldRelease: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = holdProgress,
        animationSpec = tween(durationMillis = 80),
        label = "holdProgress"
    )

    val buttonColor = if (isHolding)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.error

    val trackColor = MaterialTheme.colorScheme.errorContainer
    val sosButtonDesc = stringResource(id = R.string.sos_button_desc)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(120.dp)
            .semantics { contentDescription = sosButtonDesc }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onHoldStart()
                        try {
                            awaitRelease()
                        } finally {
                            onHoldRelease()
                        }
                    }
                )
            }
    ) {
        // Progress ring drawn behind the button circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 8.dp.toPx()
            val inset = stroke / 2
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = buttonColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        // Inner filled circle with SOS label
        Surface(
            shape = CircleShape,
            color = buttonColor,
            modifier = Modifier.size(96.dp),
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = stringResource(id = R.string.sos_button),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Simulated Map Placeholder (Canvas)
// ---------------------------------------------------------------------------

@Composable
private fun MapPlaceholder(modifier: Modifier = Modifier) {
    val gridColor = Color(0xFFDDE3EA)
    val bgColor = Color(0xFFEDF1F5)
    val riskColor = Color(0xFFB0BEC5).copy(alpha = 0.55f)
    val dotColor = Color(0xFF1A237E)
    val riskZoneLabel = stringResource(id = R.string.risk_zone)

    Canvas(modifier = modifier.background(bgColor)) {
        val w = size.width
        val h = size.height
        val gridStep = 60.dp.toPx()

        // Grid lines
        var x = 0f
        while (x <= w) {
            drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1.dp.toPx())
            x += gridStep
        }
        var y = 0f
        while (y <= h) {
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
            y += gridStep
        }

        val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))

        // Risk zone 1 (upper right)
        val rz1Left = w * 0.52f
        val rz1Top = h * 0.12f
        val rz1Size = Size(w * 0.30f, h * 0.18f)
        drawRect(riskColor, topLeft = Offset(rz1Left, rz1Top), size = rz1Size)
        drawRect(
            color = Color(0xFF78909C),
            topLeft = Offset(rz1Left, rz1Top),
            size = rz1Size,
            style = Stroke(width = 2.dp.toPx(), pathEffect = dash)
        )

        // Risk zone 2 (lower left)
        val rz2Left = w * 0.05f
        val rz2Top = h * 0.60f
        val rz2Size = Size(w * 0.28f, h * 0.16f)
        drawRect(riskColor, topLeft = Offset(rz2Left, rz2Top), size = rz2Size)
        drawRect(
            color = Color(0xFF78909C),
            topLeft = Offset(rz2Left, rz2Top),
            size = rz2Size,
            style = Stroke(width = 2.dp.toPx(), pathEffect = dash)
        )

        // User location dot (centre of map)
        val cx = w * 0.42f
        val cy = h * 0.47f
        drawCircle(Color.White, radius = 18.dp.toPx(), center = Offset(cx, cy))
        drawCircle(
            Color.White,
            radius = 18.dp.toPx(),
            center = Offset(cx, cy),
            style = Stroke(width = 3.dp.toPx())
        )
        drawCircle(dotColor, radius = 10.dp.toPx(), center = Offset(cx, cy))
    }
}

// ---------------------------------------------------------------------------
// COUNTDOWN phase
// ---------------------------------------------------------------------------

@Composable
private fun SosCountdownContent(
    seconds: Int,
    onCancel: () -> Unit,
    onSendNow: () -> Unit
) {
    val cancelDesc = stringResource(id = R.string.sos_cancel_alert_desc)
    val sendDesc = stringResource(id = R.string.sos_send_now_desc)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.warning_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.sos_countdown_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.sos_countdown_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Large countdown circle
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
            val ringColor = MaterialTheme.colorScheme.error
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = ringColor,
                    style = Stroke(width = 6.dp.toPx())
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = seconds.toString(),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(id = R.string.sos_countdown_seconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = cancelDesc },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text(
                    text = stringResource(id = R.string.sos_im_ok_cancel),
                    fontWeight = FontWeight.SemiBold
                )
            }
            OutlinedButton(
                onClick = onSendNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = sendDesc },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = stringResource(id = R.string.sos_send_now),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SENT phase
// ---------------------------------------------------------------------------

@Composable
private fun SosSentContent(
    notifiedContacts: List<com.example.securanet.domain.model.Contact>,
    hasNoContacts: Boolean,
    hasContactsButNoPriority: Boolean,
    onCancelAlert: () -> Unit
) {
    val cancelDesc = stringResource(id = R.string.sos_cancel_alert_desc)

    Column(modifier = Modifier.fillMaxSize()) {
        // Dark header
        Surface(
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.notifications_active_24px),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = R.string.sos_alert_sent),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.background,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.sos_stay_calm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                hasNoContacts -> {
                    InfoCard(
                        isSuccess = false,
                        message = stringResource(id = R.string.sos_no_contacts_at_all)
                    )
                }
                hasContactsButNoPriority -> {
                    InfoCard(
                        isSuccess = false,
                        message = stringResource(id = R.string.sos_no_priority_contacts)
                    )
                }
                else -> {
                    // Header row
                    Text(
                        text = stringResource(
                            id = R.string.sos_priority_contacts_notified,
                            notifiedContacts.size
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    notifiedContacts.forEach { contact ->
                        NotifiedContactRow(contact = contact)
                    }
                }
            }
        }

        // Cancel button at bottom
        Surface(shadowElevation = 8.dp) {
            Button(
                onClick = onCancelAlert,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(56.dp)
                    .semantics { contentDescription = cancelDesc },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text(
                    text = stringResource(id = R.string.sos_cancel_button),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun NotifiedContactRow(contact: com.example.securanet.domain.model.Contact) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.check_circle_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (contact.relationship.isNotBlank()) {
                    Text(
                        text = contact.relationship,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(isSuccess: Boolean, message: String) {
    val color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(
                    id = if (isSuccess) R.drawable.check_circle_24px else R.drawable.warning_24px
                ),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }
    }
}

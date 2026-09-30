package com.example.securanet.ui.devices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.securanet.R
import com.example.securanet.domain.model.Device
import com.example.securanet.domain.model.DeviceType
import com.example.securanet.ui.theme.*

@Composable
fun DeviceCard(
    device: Device,
    isReconnecting: Boolean,
    onTestSignal: (Device) -> Unit,
    onUnlinkRequested: (Device) -> Unit,
    onReconnect: (Device) -> Unit,
    onLinkSensor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLowBattery = device.isLinked && device.batteryPercent <= 20

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Row: Circular Icon + Device Name & Status Text + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconContainerColor = when {
                    !device.isLinked -> MaterialTheme.colorScheme.surfaceVariant
                    isLowBattery -> SosRedLight
                    device.isConnected -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                val iconTint = when {
                    !device.isLinked -> MaterialTheme.colorScheme.onSurfaceVariant
                    isLowBattery -> SosRed
                    device.isConnected -> NavyPrimary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                val iconRes = if (isLowBattery) R.drawable.warning_24px else R.drawable.devices_24px

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = device.name,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                val displayName = when (device.type) {
                    DeviceType.PANIC_BUTTON -> stringResource(id = R.string.device_panic_button)
                    DeviceType.SMART_SENSOR -> stringResource(id = R.string.device_smart_sensor)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val statusText = when {
                        !device.isLinked -> stringResource(id = R.string.device_status_not_linked)
                        device.isConnected -> stringResource(id = R.string.device_status_connected)
                        else -> stringResource(id = R.string.device_status_last_seen, device.lastSeenMinutes)
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Badge
                StatusBadge(isLinked = device.isLinked, isConnected = device.isConnected)
            }

            // Battery Section (only if linked)
            if (device.isLinked) {
                Spacer(modifier = Modifier.height(16.dp))

                val batteryColor = when {
                    isLowBattery -> SosRed
                    device.isConnected -> NavyPrimary
                    else -> NeutralBadgeText
                }

                val batteryText = if (device.isConnected) {
                    stringResource(id = R.string.device_battery, device.batteryPercent)
                } else {
                    stringResource(id = R.string.device_battery_last_sync, device.batteryPercent)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.battery_std_24px),
                            contentDescription = stringResource(id = R.string.device_battery, device.batteryPercent),
                            tint = batteryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLowBattery) "Battery Level" else "Battery",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = batteryText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = batteryColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { device.batteryPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = batteryColor,
                    trackColor = batteryColor.copy(alpha = 0.15f)
                )

                // Low Battery Warning Message
                if (isLowBattery) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.warning_24px),
                            contentDescription = stringResource(id = R.string.device_low_battery_warning),
                            tint = SosRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.device_low_battery_warning),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = SosRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!device.isLinked) {
                    Button(
                        onClick = onLinkSensor,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyPrimary,
                            contentColor = OnNavy
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_24px),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.device_action_link_sensor),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (device.isConnected) {
                    Button(
                        onClick = { onTestSignal(device) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyPrimary,
                            contentColor = OnNavy
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.check_circle_24px),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.device_action_test_signal),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onUnlinkRequested(device) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SecondaryButtonBackground,
                            contentColor = SecondaryButtonText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.close_24px),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.device_action_unlink),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Disconnected: Reconnect primary, Unlink secondary
                    Button(
                        onClick = { onReconnect(device) },
                        enabled = !isReconnecting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyPrimary,
                            contentColor = OnNavy
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                    ) {
                        if (isReconnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = OnNavy,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.device_action_reconnecting),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.refresh_24px),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.device_action_reconnect),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = { onUnlinkRequested(device) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SecondaryButtonBackground,
                            contentColor = SecondaryButtonText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.close_24px),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.device_action_unlink),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    isLinked: Boolean,
    isConnected: Boolean
) {
    val (backgroundColor, textColor, textRes) = when {
        !isLinked -> Triple(
            NeutralBadgeBackground,
            NeutralBadgeText,
            R.string.device_badge_inactive
        )
        isConnected -> Triple(
            ActiveBadgeBackground,
            ActiveBadgeText,
            R.string.device_badge_active
        )
        else -> Triple(
            NeutralBadgeBackground,
            NeutralBadgeText,
            R.string.device_badge_disconnected
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(id = textRes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

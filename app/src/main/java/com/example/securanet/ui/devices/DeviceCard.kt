package com.example.securanet.ui.devices

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
import com.example.securanet.ui.theme.SosRed

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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Icon + Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.devices_24px),
                        contentDescription = device.name,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                val displayName = when (device.type) {
                    DeviceType.PANIC_BUTTON -> stringResource(id = R.string.device_panic_button)
                    DeviceType.SMART_SENSOR -> stringResource(id = R.string.device_smart_sensor)
                }

                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Status Badge
                StatusBadge(isLinked = device.isLinked, isConnected = device.isConnected)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Connection Status Text
            val statusText = when {
                !device.isLinked -> stringResource(id = R.string.device_status_not_linked)
                device.isConnected -> stringResource(id = R.string.device_status_connected)
                else -> stringResource(id = R.string.device_status_last_seen, device.lastSeenMinutes)
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Battery Section (only if linked)
            if (device.isLinked) {
                Spacer(modifier = Modifier.height(12.dp))
                val isLowBattery = device.batteryPercent <= 20
                val batteryColor = if (isLowBattery) SosRed else MaterialTheme.colorScheme.primary

                val batteryText = if (device.isConnected) {
                    stringResource(id = R.string.device_battery, device.batteryPercent)
                } else {
                    stringResource(id = R.string.device_battery_last_sync, device.batteryPercent)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.battery_std_24px),
                        contentDescription = stringResource(id = R.string.device_battery, device.batteryPercent),
                        tint = batteryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = batteryText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = batteryColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { device.batteryPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = batteryColor,
                    trackColor = batteryColor.copy(alpha = 0.2f)
                )

                // Low Battery Warning Message (Color + Icon + Explicit text)
                if (isLowBattery) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.warning_24px),
                            contentDescription = stringResource(id = R.string.device_low_battery_warning),
                            tint = SosRed,
                            modifier = Modifier.size(18.dp)
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
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!device.isLinked) {
                    Button(
                        onClick = onLinkSensor,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(stringResource(id = R.string.device_action_link_sensor))
                    }
                } else if (device.isConnected) {
                    OutlinedButton(
                        onClick = { onTestSignal(device) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.check_circle_24px),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(id = R.string.device_action_test_signal))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { onUnlinkRequested(device) },
                        modifier = Modifier.heightIn(min = 48.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = SosRed)
                    ) {
                        Text(stringResource(id = R.string.device_action_unlink))
                    }
                } else {
                    // Disconnected: Reconnect & Unlink
                    Button(
                        onClick = { onReconnect(device) },
                        enabled = !isReconnecting,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        if (isReconnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(id = R.string.device_action_reconnecting))
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.refresh_24px),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(id = R.string.device_action_reconnect))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { onUnlinkRequested(device) },
                        modifier = Modifier.heightIn(min = 48.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = SosRed)
                    ) {
                        Text(stringResource(id = R.string.device_action_unlink))
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
            Color(0xFFEEEEEE),
            Color(0xFF616161),
            R.string.device_badge_inactive
        )
        isConnected -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            R.string.device_badge_active
        )
        else -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            R.string.device_badge_disconnected
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(id = textRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

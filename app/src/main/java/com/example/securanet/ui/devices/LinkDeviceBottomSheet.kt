package com.example.securanet.ui.devices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.securanet.R
import com.example.securanet.domain.model.DeviceType
import com.example.securanet.domain.model.DiscoveredDevice
import com.example.securanet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkDeviceBottomSheet(
    selectedType: DeviceType?,
    isAlreadyLinked: Boolean,
    isScanning: Boolean,
    discoveredDevices: List<DiscoveredDevice>,
    isLinking: Boolean,
    onSelectType: (DeviceType) -> Unit,
    onLinkDiscovered: (DiscoveredDevice) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceWhite,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(id = R.string.link_device_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(id = R.string.link_device_sheet_instruction),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Two Selectable Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SelectableDeviceTypeCard(
                    type = DeviceType.PANIC_BUTTON,
                    title = stringResource(id = R.string.device_panic_button),
                    subtitle = stringResource(id = R.string.device_panic_button_desc),
                    isSelected = selectedType == DeviceType.PANIC_BUTTON,
                    onSelect = { onSelectType(DeviceType.PANIC_BUTTON) },
                    modifier = Modifier.weight(1f)
                )

                SelectableDeviceTypeCard(
                    type = DeviceType.SMART_SENSOR,
                    title = stringResource(id = R.string.device_smart_sensor),
                    subtitle = stringResource(id = R.string.device_smart_sensor_desc),
                    isSelected = selectedType == DeviceType.SMART_SENSOR,
                    onSelect = { onSelectType(DeviceType.SMART_SENSOR) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Scanning / Results Content
            when {
                isAlreadyLinked -> {
                    Surface(
                        color = BannerAttentionBackground,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.warning_24px),
                                contentDescription = null,
                                tint = SosRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(id = R.string.link_device_already_linked),
                                style = MaterialTheme.typography.bodyMedium,
                                color = BannerAttentionText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                isScanning -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Concentric Bluetooth Halo Illustration
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(110.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(NavyPrimary.copy(alpha = 0.05f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(NavyPrimary.copy(alpha = 0.12f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(NavyPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.bluetooth_24px),
                                    contentDescription = null,
                                    tint = OnNavy,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(id = R.string.link_device_searching),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                discoveredDevices.isNotEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        discoveredDevices.forEach { device ->
                            DiscoveredDeviceRow(
                                device = device,
                                isLinking = isLinking,
                                onLink = { onLinkDiscovered(device) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cancel Button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SelectableDeviceTypeCard(
    type: DeviceType,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    OutlinedCard(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                role = Role.RadioButton
            ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
                .heightIn(min = 104.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconBg = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.surfaceVariant
                val iconTint = if (isSelected) OnNavy else MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.devices_24px),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.check_circle_24px),
                            contentDescription = null,
                            tint = OnNavy,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DiscoveredDeviceRow(
    device: DiscoveredDevice,
    isLinking: Boolean,
    onLink: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.bluetooth_24px),
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = onLink,
                enabled = !isLinking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyPrimary,
                    contentColor = OnNavy
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                if (isLinking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = OnNavy,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.link_device_action_link),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

package com.example.securanet.ui.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.securanet.R
import com.example.securanet.domain.model.Device
import com.example.securanet.domain.model.DeviceType
import com.example.securanet.presentation.devices.DevicesState
import com.example.securanet.presentation.devices.DevicesViewModel
import com.example.securanet.ui.theme.*

@Composable
fun DevicesScreen(
    viewModel: DevicesViewModel
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SoftBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            DevicesScreenContent(
                state = state,
                onLinkNewDevice = { viewModel.openLinkSheet(DeviceType.PANIC_BUTTON) },
                onTestSignal = { device -> viewModel.openTestSheet(device) },
                onUnlinkRequested = { device -> viewModel.requestUnlink(device) },
                onReconnect = { device -> viewModel.reconnectDevice(device.id) },
                onLinkSensor = { viewModel.openLinkSheet(DeviceType.SMART_SENSOR) },
                onOpenHowToLink = { viewModel.openHowToLinkSheet() }
            )

            // Unlink confirmation dialog
            state.deviceToUnlink?.let { device ->
                val displayName = when (device.type) {
                    DeviceType.PANIC_BUTTON -> stringResource(id = R.string.device_panic_button)
                    DeviceType.SMART_SENSOR -> stringResource(id = R.string.device_smart_sensor)
                }

                AlertDialog(
                    onDismissRequest = viewModel::dismissUnlinkDialog,
                    containerColor = SurfaceWhite,
                    title = { Text(stringResource(id = R.string.device_unlink_dialog_title)) },
                    text = {
                        Text(stringResource(id = R.string.device_unlink_dialog_message, displayName))
                    },
                    confirmButton = {
                        TextButton(
                            onClick = viewModel::confirmUnlink,
                            modifier = Modifier.heightIn(min = 48.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = SosRed)
                        ) {
                            Text(stringResource(id = R.string.device_unlink_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = viewModel::dismissUnlinkDialog,
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(stringResource(id = R.string.cancel))
                        }
                    }
                )
            }

            // 1. Link Device BottomSheet
            if (state.isLinkSheetOpen) {
                val isAlreadyLinked = state.devices.any {
                    it.type == state.selectedDeviceType && it.isLinked
                }

                LinkDeviceBottomSheet(
                    selectedType = state.selectedDeviceType,
                    isAlreadyLinked = isAlreadyLinked,
                    isScanning = state.isScanning,
                    discoveredDevices = state.discoveredDevices,
                    isLinking = state.isLinkingDevice,
                    onSelectType = { viewModel.selectDeviceType(it) },
                    onLinkDiscovered = { discovered -> viewModel.linkDiscoveredDevice(discovered) },
                    onDismiss = { viewModel.closeLinkSheet() }
                )
            }

            // 2. Test Signal BottomSheet
            if (state.isTestSheetOpen && state.testingDevice != null) {
                TestSignalBottomSheet(
                    device = state.testingDevice!!,
                    isWaiting = state.isTestWaiting,
                    isSuccess = state.isTestSuccess,
                    onClose = { viewModel.closeTestSheet() }
                )
            }

            // 3. Link Success Confirmation BottomSheet
            if (state.linkedSuccessDevice != null) {
                LinkSuccessBottomSheet(
                    device = state.linkedSuccessDevice!!,
                    onTestNow = { viewModel.onTestFromLinkSuccess(state.linkedSuccessDevice!!) },
                    onDone = { viewModel.closeLinkSuccessSheet() }
                )
            }

            // 4. How To Link BottomSheet
            if (state.isHowToLinkSheetOpen) {
                HowToLinkBottomSheet(
                    onClose = { viewModel.closeHowToLinkSheet() }
                )
            }
        }
    }
}

@Composable
fun DevicesScreenContent(
    state: DevicesState,
    onLinkNewDevice: () -> Unit,
    onTestSignal: (Device) -> Unit,
    onUnlinkRequested: (Device) -> Unit,
    onReconnect: (Device) -> Unit,
    onLinkSensor: () -> Unit,
    onOpenHowToLink: () -> Unit,
    modifier: Modifier = Modifier
) {
    val linkedDevices = state.devices.filter { it.isLinked }
    val connectedCount = linkedDevices.count { it.isConnected }
    val hasLowBattery = linkedDevices.any { it.batteryPercent <= 20 }
    val hasDisconnected = linkedDevices.any { !it.isConnected }

    val counterText = when {
        connectedCount > 0 -> stringResource(id = R.string.devices_count_connected, connectedCount)
        linkedDevices.isNotEmpty() -> stringResource(id = R.string.devices_count_linked, linkedDevices.size)
        else -> stringResource(id = R.string.devices_count_linked, 0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Title and Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.devices_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = counterText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Empty State: No device linked
        if (linkedDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Concentric Halo Illustration
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(140.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary.copy(alpha = 0.05f))
                        )
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary.copy(alpha = 0.1f))
                        )
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.devices_24px),
                                contentDescription = null,
                                tint = OnNavy,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(id = R.string.devices_empty_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(id = R.string.devices_empty_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = onLinkNewDevice,
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
                            text = stringResource(id = R.string.devices_link_new_device),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // How do I link my device link
                    TextButton(
                        onClick = onOpenHowToLink,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.help_outline_24px),
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.how_to_link_device_link),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyPrimary
                        )
                    }
                }
            }
        } else {
            // Device list with Banners
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                // Attention / Warning Banner at Top if Low Battery
                if (hasLowBattery) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = BannerAttentionBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.warning_24px),
                                        contentDescription = null,
                                        tint = SosRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "1 device requires attention",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BannerAttentionText
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SosRed.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "ACTION NEEDED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SosRed,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                } else if (hasDisconnected) {
                    // Connection Dropped Banner
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = BannerDroppedBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.warning_24px),
                                    contentDescription = null,
                                    tint = BannerDroppedText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Device connection dropped",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BannerDroppedText
                                )
                            }
                        }
                    }
                }

                items(state.devices, key = { it.id }) { device ->
                    DeviceCard(
                        device = device,
                        isReconnecting = state.reconnectingDeviceId == device.id,
                        onTestSignal = onTestSignal,
                        onUnlinkRequested = onUnlinkRequested,
                        onReconnect = onReconnect,
                        onLinkSensor = onLinkSensor
                    )
                }

                // Bottom + Link new device action button & How do I link link
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onLinkNewDevice,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.add_24px),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.devices_link_new_device),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        TextButton(
                            onClick = onOpenHowToLink,
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.help_outline_24px),
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.how_to_link_device_link),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── State Previews ────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "1. No Device Linked")
@Composable
fun DevicesScreen_NoDeviceLinked_Preview() {
    SecuraNetTheme {
        DevicesScreenContent(
            state = DevicesState(
                devices = listOf(
                    Device("1", DeviceType.PANIC_BUTTON, "Panic Button", isLinked = false, isConnected = false, batteryPercent = 80),
                    Device("2", DeviceType.SMART_SENSOR, "Smart Sensor", isLinked = false, isConnected = false, batteryPercent = 100)
                )
            ),
            onLinkNewDevice = {},
            onTestSignal = {},
            onUnlinkRequested = {},
            onReconnect = {},
            onLinkSensor = {},
            onOpenHowToLink = {}
        )
    }
}

@Preview(showBackground = true, name = "2. Connected State")
@Composable
fun DevicesScreen_Connected_Preview() {
    SecuraNetTheme {
        DevicesScreenContent(
            state = DevicesState(
                devices = listOf(
                    Device("1", DeviceType.PANIC_BUTTON, "Panic Button", isLinked = true, isConnected = true, batteryPercent = 80),
                    Device("2", DeviceType.SMART_SENSOR, "Smart Sensor", isLinked = false, isConnected = false, batteryPercent = 100)
                )
            ),
            onLinkNewDevice = {},
            onTestSignal = {},
            onUnlinkRequested = {},
            onReconnect = {},
            onLinkSensor = {},
            onOpenHowToLink = {}
        )
    }
}

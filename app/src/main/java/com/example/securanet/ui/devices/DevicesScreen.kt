package com.example.securanet.ui.devices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.securanet.domain.model.DiscoveredDevice
import com.example.securanet.presentation.devices.DevicesState
import com.example.securanet.presentation.devices.DevicesViewModel
import com.example.securanet.ui.theme.SecuraNetTheme
import com.example.securanet.ui.theme.SosRed
import com.example.securanet.ui.theme.SurfaceWhite

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

    val testSignalMessage = stringResource(id = R.string.device_test_signal_sent)
    val linkSuccessMessage = stringResource(id = R.string.link_device_success)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            DevicesScreenContent(
                state = state,
                onLinkNewDevice = { viewModel.openLinkSheet(DeviceType.PANIC_BUTTON) },
                onTestSignal = { device -> viewModel.testDevice(device.id, testSignalMessage) },
                onUnlinkRequested = { device -> viewModel.requestUnlink(device) },
                onReconnect = { device -> viewModel.reconnectDevice(device.id) },
                onLinkSensor = { viewModel.openLinkSheet(DeviceType.SMART_SENSOR) }
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

            // Link Device BottomSheet
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
                    onLinkDiscovered = { discovered -> viewModel.linkDiscoveredDevice(discovered, linkSuccessMessage) },
                    onDismiss = { viewModel.closeLinkSheet() }
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
    modifier: Modifier = Modifier
) {
    val linkedDevices = state.devices.filter { it.isLinked }
    val connectedCount = linkedDevices.count { it.isConnected }

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
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = counterText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    Icon(
                        painter = painterResource(id = R.drawable.devices_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onLinkNewDevice,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_24px),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.devices_link_new_device))
                    }
                }
            }
        } else {
            // Device list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
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
            onLinkSensor = {}
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
            onLinkSensor = {}
        )
    }
}

@Preview(showBackground = true, name = "3. Low Battery State")
@Composable
fun DevicesScreen_LowBattery_Preview() {
    SecuraNetTheme {
        DevicesScreenContent(
            state = DevicesState(
                devices = listOf(
                    Device("1", DeviceType.PANIC_BUTTON, "Panic Button", isLinked = true, isConnected = true, batteryPercent = 15),
                    Device("2", DeviceType.SMART_SENSOR, "Smart Sensor", isLinked = false, isConnected = false, batteryPercent = 100)
                )
            ),
            onLinkNewDevice = {},
            onTestSignal = {},
            onUnlinkRequested = {},
            onReconnect = {},
            onLinkSensor = {}
        )
    }
}

@Preview(showBackground = true, name = "4. Disconnected State")
@Composable
fun DevicesScreen_Disconnected_Preview() {
    SecuraNetTheme {
        DevicesScreenContent(
            state = DevicesState(
                devices = listOf(
                    Device("1", DeviceType.PANIC_BUTTON, "Panic Button", isLinked = true, isConnected = false, batteryPercent = 80, lastSeenMinutes = 15),
                    Device("2", DeviceType.SMART_SENSOR, "Smart Sensor", isLinked = false, isConnected = false, batteryPercent = 100)
                )
            ),
            onLinkNewDevice = {},
            onTestSignal = {},
            onUnlinkRequested = {},
            onReconnect = {},
            onLinkSensor = {}
        )
    }
}

@Preview(showBackground = true, name = "5. Smart Sensor Not Linked State")
@Composable
fun DevicesScreen_SmartSensorNotLinked_Preview() {
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
            onLinkSensor = {}
        )
    }
}

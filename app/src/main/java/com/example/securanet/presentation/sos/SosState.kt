package com.example.securanet.presentation.sos

import com.example.securanet.domain.model.Contact

/** Phase of the SOS flow. */
enum class SosPhase {
    /** Normal Home screen – SOS button is idle. */
    IDLE,

    /** User is holding the SOS button (progress 0 → 1 over 3 s). */
    HOLDING,

    /** Hold completed – 5-second cancel countdown is running. */
    COUNTDOWN,

    /** Alert was confirmed (countdown expired OR "Send now" tapped). */
    SENT
}

/** Status of the Panic Button hardware device. */
enum class PanicButtonStatus {
    NOT_LINKED,
    CONNECTED,
    DISCONNECTED
}

data class SosState(
    val phase: SosPhase = SosPhase.IDLE,

    /** 0.0 → 1.0 fill during the HOLDING phase. Also used for the 2-s cancel-alert hold. */
    val holdProgress: Float = 0f,

    /** Remaining seconds during COUNTDOWN (counts down 5 → 0). */
    val countdownSeconds: Int = 5,

    /** Priority contacts that exist in the repository (populated at SENT entry). */
    val priorityContacts: List<Contact> = emptyList(),

    /** True when the repository is completely empty. */
    val hasNoContacts: Boolean = false,

    /** True when contacts exist but none is marked as priority. */
    val hasContactsButNoPriority: Boolean = false,

    /** Progress of the "cancel alert" 2-s hold on the SENT screen (0 → 1). */
    val cancelHoldProgress: Float = 0f,

    /** Status of the Panic Button device for displaying the Home chip. */
    val panicButtonStatus: PanicButtonStatus = PanicButtonStatus.NOT_LINKED
)

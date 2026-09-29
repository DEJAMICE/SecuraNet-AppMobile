package com.example.securanet.presentation.sos

import com.example.securanet.domain.model.Contact

/**
 * Represents the different phases of the SOS feature.
 */
enum class SosPhase {
    /** Normal home state – SOS button is idle. */
    IDLE,

    /** User is pressing and holding the SOS button (0–3 s). */
    HOLDING,

    /** Hold completed – 5-second cancel countdown is running. */
    COUNTDOWN,

    /** Alert was confirmed (countdown finished or "Send now" tapped). */
    SENT
}

data class SosState(
    val phase: SosPhase = SosPhase.IDLE,

    /** Progress of the hold gesture: 0.0f (not started) → 1.0f (complete). */
    val holdProgress: Float = 0f,

    /** Remaining seconds in the cancel countdown (5 → 0). */
    val countdownSeconds: Int = 5,

    /** Priority contacts that were notified when the alert was sent. */
    val notifiedContacts: List<Contact> = emptyList(),

    /** True when all contacts exist but none is marked priority. */
    val hasContactsButNoPriority: Boolean = false,

    /** True when the repository has no contacts at all. */
    val hasNoContacts: Boolean = false
)

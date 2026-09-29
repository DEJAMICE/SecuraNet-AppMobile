package com.example.securanet.presentation.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.securanet.common.Constants
import com.example.securanet.domain.model.Contact
import com.example.securanet.domain.repository.ContactRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SosViewModel(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SosState())
    val state: StateFlow<SosState> = _state.asStateFlow()

    // Tracks the coroutine that drives the hold-progress animation
    private var holdJob: Job? = null

    // Tracks the coroutine that drives the cancel countdown
    private var countdownJob: Job? = null

    // -- Hold gesture --------------------------------------------------------

    /** Called when the user starts pressing the SOS button. */
    fun onHoldStart() {
        if (_state.value.phase != SosPhase.IDLE) return

        _state.update { it.copy(phase = SosPhase.HOLDING, holdProgress = 0f) }

        holdJob = viewModelScope.launch {
            val steps = 60
            val stepDelayMs = Constants.SOS_HOLD_DURATION_MS / steps
            for (i in 1..steps) {
                delay(stepDelayMs)
                // Check if hold was released in the meantime
                if (_state.value.phase != SosPhase.HOLDING) return@launch
                _state.update { it.copy(holdProgress = i.toFloat() / steps) }
            }
            // Hold completed – start the cancel countdown
            startCountdown()
        }
    }

    /** Called when the user releases the SOS button before the hold completes. */
    fun onHoldRelease() {
        if (_state.value.phase != SosPhase.HOLDING) return
        holdJob?.cancel()
        _state.update { it.copy(phase = SosPhase.IDLE, holdProgress = 0f) }
    }

    // -- Countdown -----------------------------------------------------------

    private fun startCountdown() {
        _state.update {
            it.copy(
                phase = SosPhase.COUNTDOWN,
                holdProgress = 1f,
                countdownSeconds = Constants.SOS_CANCEL_COUNTDOWN_SEC
            )
        }

        countdownJob = viewModelScope.launch {
            for (remaining in (Constants.SOS_CANCEL_COUNTDOWN_SEC - 1) downTo 0) {
                delay(1000L)
                if (_state.value.phase != SosPhase.COUNTDOWN) return@launch
                _state.update { it.copy(countdownSeconds = remaining) }
            }
            // Countdown expired → send the alert automatically
            confirmSend()
        }
    }

    /** "I'm OK – cancel" button: abort the countdown and reset. */
    fun cancelCountdown() {
        countdownJob?.cancel()
        _state.update {
            SosState() // full reset
        }
    }

    /** "Send SOS now" button or countdown expired. */
    fun confirmSend() {
        countdownJob?.cancel()
        viewModelScope.launch {
            val allContacts: List<Contact> = contactRepository.getContacts().first()
            val priority = allContacts.filter { it.isPriority }

            _state.update {
                it.copy(
                    phase = SosPhase.SENT,
                    notifiedContacts = priority,
                    hasNoContacts = allContacts.isEmpty(),
                    hasContactsButNoPriority = allContacts.isNotEmpty() && priority.isEmpty()
                )
            }
        }
    }

    /** "I'm safe – cancel alert" button on the SOS Sent screen. */
    fun cancelAlert() {
        _state.update { SosState() } // full reset
    }
}

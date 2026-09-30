package com.example.securanet.presentation.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.securanet.common.Constants
import com.example.securanet.domain.model.Contact
import com.example.securanet.domain.model.DeviceType
import com.example.securanet.domain.repository.ContactRepository
import com.example.securanet.domain.repository.DeviceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SosViewModel(
    private val contactRepository: ContactRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SosState())
    val state: StateFlow<SosState> = _state.asStateFlow()

    private var holdJob: Job? = null
    private var countdownJob: Job? = null
    private var cancelHoldJob: Job? = null

    init {
        viewModelScope.launch {
            deviceRepository.getDevices().collect { devices ->
                val panicButton = devices.firstOrNull { it.type == DeviceType.PANIC_BUTTON }
                val status = when {
                    panicButton == null || !panicButton.isLinked -> PanicButtonStatus.NOT_LINKED
                    panicButton.isConnected -> PanicButtonStatus.CONNECTED
                    else -> PanicButtonStatus.DISCONNECTED
                }
                _state.update { it.copy(panicButtonStatus = status) }
            }
        }
    }

    // -------------------------------------------------------------------------
    // SOS button hold (IDLE → HOLDING → COUNTDOWN)
    // -------------------------------------------------------------------------

    /** Called when the user begins pressing the SOS button. */
    fun onSosHoldStart() {
        if (_state.value.phase != SosPhase.IDLE) return
        _state.update { it.copy(phase = SosPhase.HOLDING, holdProgress = 0f) }

        holdJob = viewModelScope.launch {
            val steps = 60
            val stepDelayMs = Constants.SOS_HOLD_DURATION_MS / steps
            for (i in 1..steps) {
                delay(stepDelayMs)
                if (_state.value.phase != SosPhase.HOLDING) return@launch
                _state.update { it.copy(holdProgress = i.toFloat() / steps) }
            }
            startCountdown()
        }
    }

    /** Called when the user releases the SOS button before 3 s have elapsed. */
    fun onSosHoldRelease() {
        if (_state.value.phase != SosPhase.HOLDING) return
        holdJob?.cancel()
        _state.update { it.copy(phase = SosPhase.IDLE, holdProgress = 0f) }
    }

    // -------------------------------------------------------------------------
    // Countdown (COUNTDOWN)
    // -------------------------------------------------------------------------

    private fun startCountdown() {
        _state.update {
            it.copy(
                phase = SosPhase.COUNTDOWN,
                holdProgress = 0f,
                countdownSeconds = Constants.SOS_CANCEL_COUNTDOWN_SEC
            )
        }
        countdownJob = viewModelScope.launch {
            for (remaining in (Constants.SOS_CANCEL_COUNTDOWN_SEC - 1) downTo 0) {
                delay(1000L)
                if (_state.value.phase != SosPhase.COUNTDOWN) return@launch
                _state.update { it.copy(countdownSeconds = remaining) }
            }
            // Auto-send when the countdown reaches 0
            confirmSend()
        }
    }

    /** "I'm okay – Cancel" pressed during countdown. */
    fun cancelCountdown() {
        countdownJob?.cancel()
        _state.update { it.copy(phase = SosPhase.IDLE, holdProgress = 0f, countdownSeconds = Constants.SOS_CANCEL_COUNTDOWN_SEC) }
    }

    /** "Send SOS now" pressed, or countdown expired. */
    fun confirmSend() {
        countdownJob?.cancel()
        viewModelScope.launch {
            val all: List<Contact> = contactRepository.getContacts().first()
            val priority = all.filter { it.isPriority }
            _state.update {
                it.copy(
                    phase = SosPhase.SENT,
                    priorityContacts = priority,
                    hasNoContacts = all.isEmpty(),
                    hasContactsButNoPriority = all.isNotEmpty() && priority.isEmpty()
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // Cancel-alert hold on the SENT screen (hold 2 s → returns to IDLE)
    // -------------------------------------------------------------------------

    /** Called when the user begins holding "I'm safe – Cancel alert". */
    fun onCancelAlertHoldStart() {
        if (_state.value.phase != SosPhase.SENT) return
        _state.update { it.copy(cancelHoldProgress = 0f) }

        val holdMs = 2000L
        val steps = 40
        val stepDelayMs = holdMs / steps

        cancelHoldJob = viewModelScope.launch {
            for (i in 1..steps) {
                delay(stepDelayMs)
                if (_state.value.phase != SosPhase.SENT) return@launch
                val progress = i.toFloat() / steps
                _state.update { it.copy(cancelHoldProgress = progress) }
            }
            // Hold complete – reset phase to IDLE
            _state.update { it.copy(phase = SosPhase.IDLE, cancelHoldProgress = 0f) }
        }
    }

    /** Called when the user releases the cancel-alert button before 2 s. */
    fun onCancelAlertHoldRelease() {
        cancelHoldJob?.cancel()
        if (_state.value.phase == SosPhase.SENT) {
            _state.update { it.copy(cancelHoldProgress = 0f) }
        }
    }
}

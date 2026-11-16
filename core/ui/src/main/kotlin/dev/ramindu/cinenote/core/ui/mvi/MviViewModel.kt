package dev.ramindu.cinenote.core.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Unidirectional data flow:
 *  UI --[ACTION]--> ViewModel --(reduce)--> [STATE] --> UI
 *                           \--[EFFECT]--> one-off events (navigation, snackbar)
 */
abstract class MviViewModel<STATE, ACTION, EFFECT>(initialState: STATE) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<STATE> = _state.asStateFlow()

    private val _effects = Channel<EFFECT>(Channel.BUFFERED)
    val effects: Flow<EFFECT> = _effects.receiveAsFlow()

    protected val currentState: STATE get() = _state.value

    abstract fun onAction(action: ACTION)

    protected fun setState(reducer: STATE.() -> STATE) = _state.update(reducer)

    protected fun sendEffect(effect: EFFECT) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

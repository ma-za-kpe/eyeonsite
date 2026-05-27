package com.maku.eyeonsite.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maku.eyeonsite.analytics.AnalyticsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FundTaskUiState {
    data object Idle : FundTaskUiState()

    data object CreatingIntent : FundTaskUiState()

    data class ReadyToPay(
        val clientSecret: String,
        val paymentIntentId: String,
    ) : FundTaskUiState()

    data object Funded : FundTaskUiState()

    data class Error(
        val message: String,
    ) : FundTaskUiState()
}

class FundTaskViewModel(
    private val repository: PaymentRepository,
    private val analytics: AnalyticsService,
) : ViewModel() {
    private val _state = MutableStateFlow<FundTaskUiState>(FundTaskUiState.Idle)
    val state: StateFlow<FundTaskUiState> = _state.asStateFlow()

    fun startPayment(taskId: String) {
        if (_state.value !is FundTaskUiState.Idle && _state.value !is FundTaskUiState.Error) return
        _state.value = FundTaskUiState.CreatingIntent
        viewModelScope.launch {
            repository
                .createPaymentIntent(taskId)
                .onSuccess { result ->
                    _state.value =
                        FundTaskUiState.ReadyToPay(
                            clientSecret = result.clientSecret,
                            paymentIntentId = result.paymentIntentId,
                        )
                    analytics.logEvent("payment_intent_created", mapOf("taskId" to taskId))
                }.onFailure { e ->
                    _state.value =
                        FundTaskUiState.Error(
                            e.message ?: "Failed to initialise payment. Please try again.",
                        )
                }
        }
    }

    fun confirmPaymentSucceeded(taskId: String) {
        if (_state.value !is FundTaskUiState.ReadyToPay) return
        analytics.logEvent("escrow_funded", mapOf("taskId" to taskId))
        _state.value = FundTaskUiState.Funded
    }

    fun confirmPaymentFailed(reason: String) {
        if (_state.value !is FundTaskUiState.ReadyToPay) return
        _state.value = FundTaskUiState.Error(reason)
    }

    fun retry() {
        _state.value = FundTaskUiState.Idle
    }
}

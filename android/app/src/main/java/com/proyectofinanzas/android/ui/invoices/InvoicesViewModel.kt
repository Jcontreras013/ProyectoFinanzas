package com.proyectofinanzas.android.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectofinanzas.android.data.AppContainer
import com.proyectofinanzas.android.data.remote.InvoiceDto
import com.proyectofinanzas.android.data.remote.friendlyErrorMessage
import com.proyectofinanzas.android.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InvoicesListViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<InvoiceDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<InvoiceDto>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                UiState.Success(AppContainer.apiService.listInvoices(page = 0, size = 30).content)
            } catch (e: Exception) {
                UiState.Error(friendlyErrorMessage(e))
            }
        }
    }
}

class InvoiceDetailViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<InvoiceDto>>(UiState.Loading)
    val state: StateFlow<UiState<InvoiceDto>> = _state.asStateFlow()

    fun load(invoiceId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                UiState.Success(AppContainer.apiService.getInvoice(invoiceId))
            } catch (e: Exception) {
                UiState.Error(friendlyErrorMessage(e))
            }
        }
    }
}

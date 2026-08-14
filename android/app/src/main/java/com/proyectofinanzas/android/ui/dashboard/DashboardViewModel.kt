package com.proyectofinanzas.android.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectofinanzas.android.data.AppContainer
import com.proyectofinanzas.android.data.remote.DashboardKpisDto
import com.proyectofinanzas.android.data.remote.friendlyErrorMessage
import com.proyectofinanzas.android.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<DashboardKpisDto>>(UiState.Loading)
    val state: StateFlow<UiState<DashboardKpisDto>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                UiState.Success(AppContainer.apiService.dashboardKpis())
            } catch (e: Exception) {
                UiState.Error(friendlyErrorMessage(e))
            }
        }
    }
}

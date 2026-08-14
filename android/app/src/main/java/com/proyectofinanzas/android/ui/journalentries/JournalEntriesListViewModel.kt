package com.proyectofinanzas.android.ui.journalentries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectofinanzas.android.data.AppContainer
import com.proyectofinanzas.android.data.remote.JournalEntryDto
import com.proyectofinanzas.android.data.remote.friendlyErrorMessage
import com.proyectofinanzas.android.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JournalEntriesListViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<JournalEntryDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<JournalEntryDto>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                UiState.Success(AppContainer.apiService.listJournalEntries(page = 0, size = 30).content)
            } catch (e: Exception) {
                UiState.Error(friendlyErrorMessage(e))
            }
        }
    }
}

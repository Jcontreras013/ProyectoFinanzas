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

class JournalEntryDetailViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<JournalEntryDto>>(UiState.Loading)
    val state: StateFlow<UiState<JournalEntryDto>> = _state.asStateFlow()

    fun load(entryId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                UiState.Success(AppContainer.apiService.getJournalEntry(entryId))
            } catch (e: Exception) {
                UiState.Error(friendlyErrorMessage(e))
            }
        }
    }
}

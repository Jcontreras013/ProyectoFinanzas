package com.proyectofinanzas.android.ui.journalentries

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectofinanzas.android.data.AppContainer
import com.proyectofinanzas.android.data.remote.AccountDto
import com.proyectofinanzas.android.data.remote.CreateJournalEntryRequest
import com.proyectofinanzas.android.data.remote.JournalEntryLineRequest
import com.proyectofinanzas.android.data.remote.friendlyErrorMessage
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Cada campo es un mutableStateOf individual (no un simple `var`) para que Compose
 * recomponga cuando cambia, aunque la mutación ocurra dentro de un objeto ya existente
 * en la SnapshotStateList `lines` (que solo observa cambios estructurales por sí sola).
 */
class LineDraft {
    var accountId by mutableStateOf("")
    var debit by mutableStateOf("")
    var credit by mutableStateOf("")
}

class JournalEntryCreateViewModel : ViewModel() {
    var accounts by mutableStateOf<List<AccountDto>>(emptyList())
        private set
    var entryDate by mutableStateOf(LocalDate.now().toString())
    var description by mutableStateOf("")
    val lines = mutableStateListOf(LineDraft(), LineDraft())
    var isSaving by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var createdEntryId by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            runCatching { AppContainer.apiService.listAccounts() }
                .onSuccess { accounts = it.filter { a -> a.allowsPosting && a.isActive }.sortedBy { a -> a.code } }
        }
    }

    fun addLine() = lines.add(LineDraft())

    fun removeLine(index: Int) {
        if (lines.size > 2) lines.removeAt(index)
    }

    fun totalDebit(): Double = lines.sumOf { it.debit.toDoubleOrNull() ?: 0.0 }
    fun totalCredit(): Double = lines.sumOf { it.credit.toDoubleOrNull() ?: 0.0 }
    fun isBalanced(): Boolean {
        val debit = totalDebit()
        val credit = totalCredit()
        return debit > 0 && kotlin.math.abs(debit - credit) < 0.001
    }

    fun submit() {
        if (isSaving || !isBalanced()) return
        viewModelScope.launch {
            isSaving = true
            error = null
            try {
                val request = CreateJournalEntryRequest(
                    entryDate = entryDate,
                    description = description,
                    lines = lines.filter { it.accountId.isNotBlank() }.map {
                        JournalEntryLineRequest(
                            accountId = it.accountId,
                            debit = it.debit.ifBlank { "0" },
                            credit = it.credit.ifBlank { "0" },
                        )
                    },
                )
                val entry = AppContainer.apiService.createJournalEntry(request)
                createdEntryId = entry.id
            } catch (e: Exception) {
                error = friendlyErrorMessage(e)
            } finally {
                isSaving = false
            }
        }
    }
}

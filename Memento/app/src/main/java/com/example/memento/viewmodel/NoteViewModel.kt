package com.example.memento.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memento.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(private val repo: NoteRepository) : ViewModel() {

    val weeksWithNotes: StateFlow<Set<Int>> = repo.weeksWithNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun noteForWeek(weekIdx: Int): Flow<String> = repo.noteForWeek(weekIdx)

    private val saveJobs = mutableMapOf<Int, Job>()

    fun saveNote(weekIdx: Int, note: String) {
        saveJobs[weekIdx]?.cancel()
        saveJobs[weekIdx] = viewModelScope.launch {
            delay(300)
            repo.save(weekIdx, note)
            saveJobs.remove(weekIdx)
        }
    }

    fun saveNoteNow(weekIdx: Int, note: String) {
        saveJobs[weekIdx]?.cancel()
        saveJobs.remove(weekIdx)
        viewModelScope.launch { repo.save(weekIdx, note) }
    }
}

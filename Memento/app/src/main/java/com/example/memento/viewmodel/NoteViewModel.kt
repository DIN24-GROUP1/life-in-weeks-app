package com.example.memento.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memento.db.WeekNoteDao
import com.example.memento.model.WeekNote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(private val dao: WeekNoteDao) : ViewModel() {

    val weeksWithNotes: StateFlow<Set<Int>> = dao.getWeekIndicesWithNotes()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun noteForWeek(weekIdx: Int): Flow<String> =
        dao.getNoteForWeek(weekIdx).map { it ?: "" }

    private val saveJobs = mutableMapOf<Int, Job>()

    fun saveNote(weekIdx: Int, note: String) {
        saveJobs[weekIdx]?.cancel()
        saveJobs[weekIdx] = viewModelScope.launch {
            delay(300)
            persist(weekIdx, note)
            saveJobs.remove(weekIdx)
        }
    }

    fun saveNoteNow(weekIdx: Int, note: String) {
        saveJobs[weekIdx]?.cancel()
        saveJobs.remove(weekIdx)
        viewModelScope.launch { persist(weekIdx, note) }
    }

    private suspend fun persist(weekIdx: Int, note: String) {
        if (note.isBlank()) dao.delete(weekIdx)
        else dao.upsert(WeekNote(weekIdx, note))
    }
}

package com.example.memento.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memento.db.WeekTagDao
import com.example.memento.model.WeekTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagViewModel @Inject constructor(private val dao: WeekTagDao) : ViewModel() {

    val PREDEFINED_TAGS = listOf(
        "travel", "milestone", "hard times", "family", "work",
        "health", "education", "celebration", "loss", "adventure"
    )

    val weeksWithTags: StateFlow<Set<Int>> = dao.getWeekIndicesWithTags()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val allUsedTagNames: StateFlow<List<String>> = dao.getAllUsedTagNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun tagsForWeek(weekIdx: Int): Flow<List<String>> = dao.getTagsForWeek(weekIdx)

    fun addTag(weekIdx: Int, tagName: String) {
        viewModelScope.launch { dao.insert(WeekTag(weekIdx, tagName)) }
    }

    fun removeTag(weekIdx: Int, tagName: String) {
        viewModelScope.launch { dao.delete(weekIdx, tagName) }
    }
}

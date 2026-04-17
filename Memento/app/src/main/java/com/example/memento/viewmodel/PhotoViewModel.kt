package com.example.memento.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memento.model.WeekPhoto
import com.example.memento.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoViewModel @Inject constructor(private val repo: PhotoRepository) : ViewModel() {

    val weeksWithPhotos: StateFlow<Set<Int>> = repo.weeksWithPhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun photoForWeek(weekIdx: Int): Flow<WeekPhoto?> = repo.photoForWeek(weekIdx)

    fun savePhoto(weekIdx: Int, uri: Uri) {
        viewModelScope.launch { repo.save(weekIdx, uri) }
    }

    fun deletePhoto(weekIdx: Int) {
        viewModelScope.launch { repo.delete(weekIdx) }
    }
}

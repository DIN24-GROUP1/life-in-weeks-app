package com.example.memento.viewmodel

import android.icu.text.SimpleDateFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memento.db.LifePhaseDao
import com.example.memento.model.LifePhase
import com.example.memento.model.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val dateFormatter: SimpleDateFormat,
    private val savedStateHandle: SavedStateHandle,
    private val phaseDao: LifePhaseDao,
) : ViewModel() {

    var birthdayText by mutableStateOf(
        savedStateHandle["date"] ?: ""
    )
        private set

    var lifeExpectancyText by mutableStateOf(
        savedStateHandle["lifeExpectancy"] ?: ""
    )
        private set

    val birthday: LocalDate?
        get() = runCatching {
            LocalDate.parse(
                birthdayText,
                DateTimeFormatter.ofPattern("dd.MM.yyyy")
            )
        }.getOrNull()

    val lifeExpectancyYears: Int
        get() = lifeExpectancyText.toIntOrNull() ?: 90

    val user: UserModel
        get() = UserModel(
            birthday = birthday,
            lifeExpectancyYears = lifeExpectancyYears
        )

    val phases: StateFlow<List<LifePhase>> = phaseDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun convertMillisToDate(millis: Long) {
        val formatted = dateFormatter.format(Date(millis))
        birthdayText = formatted
        savedStateHandle["date"] = formatted
    }

    fun updateLifeExpectancy(input: String) {
        lifeExpectancyText = input
        savedStateHandle["lifeExpectancy"] = input
    }

    fun addPhase(phase: LifePhase) = viewModelScope.launch { phaseDao.insert(phase) }
    fun updatePhase(phase: LifePhase) = viewModelScope.launch { phaseDao.update(phase) }
    fun deletePhase(phase: LifePhase) = viewModelScope.launch { phaseDao.delete(phase) }
}

package com.example.memento.viewmodel

import android.icu.text.SimpleDateFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memento.model.LifePhase
import com.example.memento.model.UserModel
import com.example.memento.repository.LifePhaseRepository
import com.example.memento.repository.UserProfile
import com.example.memento.repository.UserProfileRepository
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
    private val profileRepository: UserProfileRepository,
    private val phaseRepository: LifePhaseRepository,
) : ViewModel() {

    var birthdayText by mutableStateOf("")
        private set

    var lifeExpectancyText by mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            profileRepository.loadProfile()?.let { profile ->
                birthdayText = profile.birthday
                if (profile.lifeExpectancyYears != 90) {
                    lifeExpectancyText = profile.lifeExpectancyYears.toString()
                }
            }
        }
    }

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

    val phases: StateFlow<List<LifePhase>> = phaseRepository.phases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun convertMillisToDate(millis: Long) {
        birthdayText = dateFormatter.format(Date(millis))
        saveProfile()
    }

    fun updateLifeExpectancy(input: String) {
        lifeExpectancyText = input
        saveProfile()
    }

    private fun saveProfile() {
        viewModelScope.launch {
            profileRepository.saveProfile(
                UserProfile(
                    birthday = birthdayText,
                    lifeExpectancyYears = lifeExpectancyYears,
                )
            )
        }
    }

    fun addPhase(phase: LifePhase) = viewModelScope.launch { phaseRepository.addPhase(phase) }
    fun updatePhase(phase: LifePhase) = viewModelScope.launch { phaseRepository.updatePhase(phase) }
    fun deletePhase(phase: LifePhase) = viewModelScope.launch { phaseRepository.deletePhase(phase) }
}

package com.example.memento.viewmodel

import android.icu.text.SimpleDateFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memento.model.CountryData
import com.example.memento.model.LifePhase
import com.example.memento.model.UserModel
import com.example.memento.model.allCountries
import com.example.memento.model.calculateLifeExpectancy
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

    var genderSliderPosition by mutableFloatStateOf(0f)
        private set

    var selectedCountry by mutableStateOf<CountryData?>(null)
        private set

    var isProfileLoaded by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            profileRepository.loadProfile()?.let { profile ->
                birthdayText = profile.birthday
                lifeExpectancyText = if (profile.lifeExpectancyYears != 90)
                    profile.lifeExpectancyYears.toString() else ""
                genderSliderPosition = profile.genderSliderPosition
                selectedCountry = allCountries.find { it.name == profile.country }
            }
            isProfileLoaded = true
            birthday?.let { phaseRepository.seedDefaultPhasesIfEmpty(it) }
        }
    }

    val birthday: LocalDate?
        get() = runCatching {
            LocalDate.parse(birthdayText, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        }.getOrNull()

    val lifeExpectancyYears: Int
        get() = lifeExpectancyText.toIntOrNull() ?: 90

    val user: UserModel
        get() = UserModel(birthday = birthday, lifeExpectancyYears = lifeExpectancyYears)

    val phases: StateFlow<List<LifePhase>> = phaseRepository.phases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun convertMillisToDate(millis: Long) {
        birthdayText = dateFormatter.format(Date(millis))
        saveProfile()
        viewModelScope.launch {
            birthday?.let { phaseRepository.seedDefaultPhasesIfEmpty(it) }
        }
    }

    fun updateLifeExpectancy(input: String) {
        lifeExpectancyText = input
        saveProfile()
    }

    fun updateGenderSlider(position: Float) {
        genderSliderPosition = position
        recalculateLifeExpectancy()
        saveProfile()
    }

    fun updateCountry(country: CountryData) {
        selectedCountry = country
        recalculateLifeExpectancy()
        saveProfile()
    }

    private fun recalculateLifeExpectancy() {
        val country = selectedCountry ?: return
        lifeExpectancyText = calculateLifeExpectancy(country, genderSliderPosition).toString()
    }

    private fun saveProfile() {
        viewModelScope.launch {
            profileRepository.saveProfile(
                UserProfile(
                    birthday = birthdayText,
                    lifeExpectancyYears = lifeExpectancyYears,
                    genderSliderPosition = genderSliderPosition,
                    country = selectedCountry?.name ?: "",
                )
            )
        }
    }

    fun addPhase(phase: LifePhase) = viewModelScope.launch { phaseRepository.addPhase(phase) }
    fun updatePhase(phase: LifePhase) = viewModelScope.launch { phaseRepository.updatePhase(phase) }
    fun deletePhase(phase: LifePhase) = viewModelScope.launch { phaseRepository.deletePhase(phase) }
}

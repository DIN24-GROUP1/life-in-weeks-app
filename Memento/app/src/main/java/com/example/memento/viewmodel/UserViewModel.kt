package com.example.memento.viewmodel

import android.content.Context
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
import com.example.memento.repository.FactOfTheDayRepository
import com.example.memento.repository.LifePhaseRepository
import com.example.memento.repository.PhotoRepository
import com.example.memento.repository.UserProfile
import com.example.memento.repository.UserProfileRepository
import com.example.memento.ui.theme.ThemeMode
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val dateFormatter: SimpleDateFormat,
    private val profileRepository: UserProfileRepository,
    private val phaseRepository: LifePhaseRepository,
    private val photoRepository: PhotoRepository,
    private val factRepository: FactOfTheDayRepository,
    private val auth: FirebaseAuth,
) : ViewModel() {

    private val prefs = context.getSharedPreferences("memento_prefs", Context.MODE_PRIVATE)

    var themeMode by mutableStateOf(
        ThemeMode.entries.find { it.name == prefs.getString("theme_mode", ThemeMode.System.name) }
            ?: ThemeMode.System
    )
        private set

    fun updateThemeMode(mode: ThemeMode) {
        themeMode = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

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

    private var currentUserId: String? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null && uid != currentUserId) {
            currentUserId = uid
            viewModelScope.launch { loadProfileAndSeed() }
        }
    }

    private val _factOfTheDay = MutableStateFlow<String?>(null)
    val factOfTheDay: StateFlow<String?> = _factOfTheDay.asStateFlow()

    init {
        auth.addAuthStateListener(authStateListener)
        viewModelScope.launch { _factOfTheDay.value = factRepository.fetchFactForToday() }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    private suspend fun loadProfileAndSeed() {
        isProfileLoaded = false
        profileRepository.loadProfile()?.let { profile ->
            birthdayText = profile.birthday
            lifeExpectancyText = if (profile.lifeExpectancyYears != 90)
                profile.lifeExpectancyYears.toString() else ""
            genderSliderPosition = profile.genderSliderPosition
            selectedCountry = allCountries.find { it.name == profile.country }
        }
        isProfileLoaded = true
        birthday?.let { phaseRepository.syncFromFirestore(it) }
        photoRepository.syncFromStorage()
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

package com.example.memento.viewmodel

import android.icu.text.SimpleDateFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val dateFormatter: SimpleDateFormat,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var date by mutableStateOf(
        savedStateHandle["date"] ?: ""
    )
        private set

    var lifeExpectancy by mutableStateOf(
        savedStateHandle["lifeExpectancy"] ?: ""
    )

        private set
    val birthday: LocalDate?
        get() = runCatching {
            LocalDate.parse(
                date,
                DateTimeFormatter.ofPattern("dd.MM.yyyy")
            )
        }.getOrNull()

    val lifeExpectancyYears: Int
        get() = lifeExpectancy.toIntOrNull() ?: 90
    fun convertMillisToDate(millis: Long) {
        val formatted = dateFormatter.format(Date(millis))
        date = formatted
        savedStateHandle["date"] = formatted
    }

    fun updateLifeExpectancy(lifeInput: String) {
        lifeExpectancy = lifeInput
        savedStateHandle["lifeExpectancy"] = lifeInput
    }
}
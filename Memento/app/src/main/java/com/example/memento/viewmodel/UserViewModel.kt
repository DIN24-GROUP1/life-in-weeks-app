package com.example.memento.viewmodel

import android.icu.text.SimpleDateFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue
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
) : ViewModel() {

    var date by mutableStateOf("")
        private set
    var lifeExpectancy by mutableIntStateOf(0)
        private set

    fun convertMillisToDate(millis: Long) {
        date = dateFormatter.format(Date(millis))
    }
    fun updateLifeExpectancy(lifeInput: Int) {
        lifeExpectancy = lifeInput
    }
    init {
        date = dateFormatter.format(Date())
        lifeExpectancy = 90
    }

}
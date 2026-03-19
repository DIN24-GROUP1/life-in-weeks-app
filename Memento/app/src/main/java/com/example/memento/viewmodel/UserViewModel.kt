package com.example.memento.viewmodel

import android.icu.text.SimpleDateFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class UserViewModel : ViewModel(){

    var date by mutableStateOf("")
        private set
    var lifeExpectancy by mutableIntStateOf(0)
        private set

    fun convertMillisToDate(millis: Long) {
        val formatter = SimpleDateFormat("dd/mm/yyyy", Locale.getDefault())
        date = formatter.format(Date(millis))
    }
    fun setLifeExpectancy(lifeInput: Int) {
        lifeExpectancy = lifeInput
    }

}
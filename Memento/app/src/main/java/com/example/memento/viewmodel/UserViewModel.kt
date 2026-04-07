package com.example.memento.viewmodel

import android.icu.text.SimpleDateFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.memento.model.UserModel
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


    fun convertMillisToDate(millis: Long) {
        val formatted = dateFormatter.format(Date(millis))
        birthdayText = formatted
        savedStateHandle["date"] = formatted
    }

    fun updateLifeExpectancy(input: String) {
        lifeExpectancyText = input
        savedStateHandle["lifeExpectancy"] = input
    }


}

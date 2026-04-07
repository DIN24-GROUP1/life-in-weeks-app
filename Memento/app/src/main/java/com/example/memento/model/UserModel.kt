package com.example.memento.model

import java.time.LocalDate

data class UserModel(
    val name: String = "",
    val birthday: LocalDate? = null,
    val lifeExpectancyYears: Int = 90,
)


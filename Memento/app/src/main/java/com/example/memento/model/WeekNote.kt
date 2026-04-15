package com.example.memento.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "week_notes")
data class WeekNote(
    @PrimaryKey val weekIdx: Int,
    val note: String
)

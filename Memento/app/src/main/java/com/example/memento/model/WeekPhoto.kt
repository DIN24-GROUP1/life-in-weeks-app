package com.example.memento.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "week_photos")
data class WeekPhoto(
    @PrimaryKey val weekIdx: Int,
    val localUri: String,
)

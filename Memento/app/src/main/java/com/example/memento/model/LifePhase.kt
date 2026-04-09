package com.example.memento.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phases")
data class LifePhase(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorArgb: Int,
    val startEpochDay: Long,
    val endEpochDay: Long,
)

val PhaseColorPresets: List<Int> = listOf(
    0xFF3B82F6.toInt(), // blue
    0xFF10B981.toInt(), // emerald
    0xFFF59E0B.toInt(), // amber
    0xFF8B5CF6.toInt(), // violet
    0xFFEC4899.toInt(), // pink
    0xFF64748B.toInt(), // slate
)

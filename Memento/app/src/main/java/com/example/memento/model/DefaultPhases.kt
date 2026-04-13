package com.example.memento.model

data class DefaultPhase(
    val name: String,
    val startYear: Int,
    val endYear: Int,
    val colorArgb: Int,
)

val defaultLifePhases: List<DefaultPhase> = listOf(
    DefaultPhase("Childhood",    0,   6,  0xFF3B82F6.toInt()),  // blue
    DefaultPhase("School",       6,  18,  0xFF10B981.toInt()),  // emerald
    DefaultPhase("University",  18,  23,  0xFFF59E0B.toInt()),  // amber
    DefaultPhase("Career",      23,  65,  0xFF8B5CF6.toInt()),  // violet
    DefaultPhase("Senior",      65,  90,  0xFF64748B.toInt()),  // slate
)


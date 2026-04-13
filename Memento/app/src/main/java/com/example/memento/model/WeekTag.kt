package com.example.memento.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "week_tags",
    indices = [Index("weekIdx")],
    primaryKeys = ["weekIdx", "tagName"]
)
data class WeekTag(val weekIdx: Int, val tagName: String)

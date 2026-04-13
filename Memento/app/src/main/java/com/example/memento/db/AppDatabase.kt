package com.example.memento.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.memento.model.LifePhase
import com.example.memento.model.WeekTag

@Database(entities = [LifePhase::class, WeekTag::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun phaseDao(): LifePhaseDao
    abstract fun weekTagDao(): WeekTagDao
}

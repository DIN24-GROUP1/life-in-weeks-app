package com.example.memento.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.memento.model.LifePhase

@Database(entities = [LifePhase::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun phaseDao(): LifePhaseDao
}

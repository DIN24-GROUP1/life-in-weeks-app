package com.example.memento.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.memento.model.LifePhase
import kotlinx.coroutines.flow.Flow

@Dao
interface LifePhaseDao {
    @Query("SELECT * FROM phases ORDER BY startEpochDay ASC")
    fun getAll(): Flow<List<LifePhase>>

    @Insert suspend fun insert(phase: LifePhase): Long
    @Update suspend fun update(phase: LifePhase)
    @Delete suspend fun delete(phase: LifePhase)
}

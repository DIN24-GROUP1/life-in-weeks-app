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

    @Query("SELECT COUNT(*) FROM phases")
    suspend fun count(): Int

    @Insert suspend fun insert(phase: LifePhase): Long
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAll(phases: List<LifePhase>)
    @Update suspend fun update(phase: LifePhase)
    @Delete suspend fun delete(phase: LifePhase)
    @Query("DELETE FROM phases") suspend fun clearAll()
}

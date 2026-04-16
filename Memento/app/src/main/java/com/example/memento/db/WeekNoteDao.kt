package com.example.memento.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.memento.model.WeekNote
import kotlinx.coroutines.flow.Flow

@Dao
interface WeekNoteDao {
    @Query("SELECT note FROM week_notes WHERE weekIdx = :weekIdx")
    fun getNoteForWeek(weekIdx: Int): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(weekNote: WeekNote)

    @Query("DELETE FROM week_notes WHERE weekIdx = :weekIdx")
    suspend fun delete(weekIdx: Int)

    @Query("SELECT weekIdx FROM week_notes")
    fun getWeekIndicesWithNotes(): Flow<List<Int>>
}

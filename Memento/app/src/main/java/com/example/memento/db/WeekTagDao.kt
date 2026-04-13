package com.example.memento.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.memento.model.WeekTag
import kotlinx.coroutines.flow.Flow

@Dao
interface WeekTagDao {

    @Query("SELECT tagName FROM week_tags WHERE weekIdx = :weekIdx")
    fun getTagsForWeek(weekIdx: Int): Flow<List<String>>

    @Query("SELECT DISTINCT tagName FROM week_tags")
    fun getAllUsedTagNames(): Flow<List<String>>

    @Query("SELECT DISTINCT weekIdx FROM week_tags")
    fun getWeekIndicesWithTags(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(weekTag: WeekTag)

    @Query("DELETE FROM week_tags WHERE weekIdx = :weekIdx AND tagName = :tagName")
    suspend fun delete(weekIdx: Int, tagName: String)
}

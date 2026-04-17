package com.example.memento.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.memento.model.WeekPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface WeekPhotoDao {
    @Query("SELECT * FROM week_photos WHERE weekIdx = :weekIdx")
    fun getPhotoForWeek(weekIdx: Int): Flow<WeekPhoto?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(photo: WeekPhoto)

    @Query("DELETE FROM week_photos WHERE weekIdx = :weekIdx")
    suspend fun delete(weekIdx: Int)

    @Query("SELECT weekIdx FROM week_photos")
    fun getWeekIndicesWithPhotos(): Flow<List<Int>>
}

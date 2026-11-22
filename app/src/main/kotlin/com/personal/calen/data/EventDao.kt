package com.personal.calen.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY startDate ASC, startTime ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    fun getEventById(id: Long): EventEntity?

    @Query("SELECT * FROM events WHERE reminderOffsetMinutes IS NOT NULL")
    fun getEventsWithReminders(): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvent(event: EventEntity): Long

    @Update
    fun updateEvent(event: EventEntity)

    @Delete
    fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    fun deleteEventById(id: Long)
}

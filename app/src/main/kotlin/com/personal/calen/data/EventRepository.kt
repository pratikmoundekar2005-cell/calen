package com.personal.calen.data

import com.personal.calen.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface EventRepository {
    fun getAllEvents(): Flow<List<Event>>
    suspend fun getEventById(id: Long): Event?
    suspend fun getEventsWithReminders(): List<Event>
    suspend fun saveEvent(event: Event): Long
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    suspend fun deleteEventById(id: Long)
}

class RoomEventRepository(
    private val eventDao: EventDao
) : EventRepository {

    override fun getAllEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getEventById(id: Long): Event? = withContext(Dispatchers.IO) {
        eventDao.getEventById(id)?.toDomain()
    }

    override suspend fun getEventsWithReminders(): List<Event> = withContext(Dispatchers.IO) {
        eventDao.getEventsWithReminders().map { it.toDomain() }
    }

    override suspend fun saveEvent(event: Event): Long = withContext(Dispatchers.IO) {
        eventDao.insertEvent(event.toEntity())
    }

    override suspend fun updateEvent(event: Event): Unit = withContext(Dispatchers.IO) {
        eventDao.updateEvent(event.toEntity())
    }

    override suspend fun deleteEvent(event: Event): Unit = withContext(Dispatchers.IO) {
        eventDao.deleteEvent(event.toEntity())
    }

    override suspend fun deleteEventById(id: Long): Unit = withContext(Dispatchers.IO) {
        eventDao.deleteEventById(id)
    }
}

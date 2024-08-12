package com.example.hicure.alarm

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class AlarmRepository(
    private val alarmDao: AlarmDao
) {

    // Expose a Flow of all alarms for observing in the UI
    val getAllAlarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()

    // Insert a single alarm into the database
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertAlarm(alarm: AlarmEntity) {
        alarmDao.insert(alarm)
    }

    // Update a single alarm in the database
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateAlarm(alarm: AlarmEntity) {
        alarmDao.update(alarm)
    }

    // Insert or update an alarm based on its ID
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertOrUpdateAlarm(alarm: AlarmEntity) {
        val existingAlarm = alarmDao.getAlarmById(alarm.id)
        if (existingAlarm != null) {
            updateAlarm(alarm)
        } else {
            insertAlarm(alarm)
        }
    }

    @WorkerThread
    suspend fun getAlarmById(id: Int): AlarmEntity? {
        return alarmDao.getAlarmById(id)
    }

    suspend fun clearAllAlarms() {
        alarmDao.deleteAllAlarms()
    }
}

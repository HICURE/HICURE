package com.example.hicure.alarm

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class AlarmRepository(
    private val alarmDao: AlarmDao
) {

    val getAllAlarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()

    @WorkerThread
    suspend fun insertAlarm(alarm: AlarmEntity) {
        alarmDao.insert(alarm)
    }

    @WorkerThread
    suspend fun updateAlarm(alarm: AlarmEntity) {
        alarmDao.update(alarm)
    }

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

    @WorkerThread
    suspend fun clearAllAlarms() {
        alarmDao.deleteAllAlarms()
    }
}

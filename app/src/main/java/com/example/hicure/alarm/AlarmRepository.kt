package com.example.hicure.alarm

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class AlarmRepository(
    private val alarmDao: AlarmDao
) {

    val getallAlarm: Flow<List<AlarmEntity>> = alarmDao.getAllAlarm()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertAlarm(alarm: AlarmEntity) {
        alarmDao.insertAlarm(alarm)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateAlarm(alarm: AlarmEntity) {
        alarmDao.updateAlarm(alarm)
    }
}
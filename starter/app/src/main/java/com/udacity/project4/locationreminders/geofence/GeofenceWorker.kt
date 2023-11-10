package com.udacity.project4.locationreminders.geofence

import  android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GeofenceWorker(
    context: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(context, parameters), KoinComponent {
    companion object {
        const val GEOFENCE_WORKER_KEY = "GEOFENCE_WORKER_KEY"
        fun enqueueWork(context: Context, intent: Intent) {
            if (intent.action == SaveReminderFragment.ACTION_GEOFENCE_FENCE) {
                GeofencingEvent.fromIntent(intent)?.let { geofencingEvent ->
                    if (geofencingEvent.hasError()) {
                        return
                    }

                    if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                        geofencingEvent.triggeringGeofences?.let { geofence ->
                            if (geofence.isNotEmpty()) {
                                val geofenceIds =
                                    geofence.map { it.requestId }.toTypedArray()
                                val inputData = Data.Builder()
                                inputData.putStringArray(GEOFENCE_WORKER_KEY, geofenceIds)
                                val request =
                                    OneTimeWorkRequest.Builder(GeofenceWorker::class.java)
                                        .setInputData(inputData.build())
                                        .build()
                                WorkManager.getInstance(context).enqueue(request)
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun doWork(): Result {
        inputData.getStringArray(GEOFENCE_WORKER_KEY)?.let { requestIds ->
            requestIds.forEach {
                sendNotification(it)
            }
            return Result.success()
        }
        return Result.failure()
    }

    private suspend fun sendNotification(requestId: String) {
        val remindersLocalRepository: ReminderDataSource by inject()
        coroutineScope {
            launch(SupervisorJob()) {
                val result = remindersLocalRepository.getReminder(requestId)
                if (result is com.udacity.project4.locationreminders.data.dto.Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    sendNotification(
                        applicationContext, ReminderDataItem(
                            id = reminderDTO.id,
                            title = reminderDTO.title,
                            description = reminderDTO.description,
                            location = reminderDTO.location,
                            latitude = reminderDTO.latitude,
                            longitude = reminderDTO.longitude
                        )
                    )
                }
            }
        }
    }
}
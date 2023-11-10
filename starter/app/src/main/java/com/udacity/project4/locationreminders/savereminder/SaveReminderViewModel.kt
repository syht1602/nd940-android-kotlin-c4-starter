package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData<String?>()
    val selectedPOI = MutableLiveData<PointOfInterest?>()
    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()
    val reminderData = MutableLiveData<ReminderDataItem>()

    private val _isSaveLocationClick = MutableLiveData<Boolean?>()
    val isSaveLocationClick: LiveData<Boolean?>
        get() = _isSaveLocationClick

    private val _isSaveReminderClick = MutableLiveData<Boolean?>()
    val isSaveReminderClick: LiveData<Boolean?>
        get() = _isSaveReminderClick

    private val _isAddGeoFenceData = MutableLiveData<Boolean?>()
    val isAddGeoFenceData: LiveData<Boolean?>
        get() = _isAddGeoFenceData

    private val _isNavigateToSelectLocation = MutableLiveData<Boolean?>()
    val isNavigateToSelectLocation: LiveData<Boolean?>
        get() = _isNavigateToSelectLocation

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder() {
        reminderData.value = ReminderDataItem(
            title = reminderTitle.value,
            description = reminderDescription.value,
            location = reminderSelectedLocationStr.value,
            latitude = latitude.value,
            longitude = longitude.value
        )
        reminderData.value?.let {
            if (validateEnteredData(it)) {
                saveReminder(it)
            }
        }
    }

    /**
     * Save the reminder to the data source
     */
    private fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            try {
                dataSource.saveReminder(
                    ReminderDTO(
                        reminderData.title,
                        reminderData.description,
                        reminderData.location,
                        reminderData.latitude,
                        reminderData.longitude,
                        reminderData.id
                    )
                )
                showLoading.value = false
                showToast.value = app.getString(R.string.reminder_saved)
                navigationCommand.value = NavigationCommand.Back
                onAddGeoFenceData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    private fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }

    fun onGeoFenceUpdateSuccessfully(){
        showToast.value = app.getString(R.string.reminder_saved)
        navigationCommand.value = NavigationCommand.Back
    }

    fun onGeoFenceUpdateError(){
        showErrorMessage.value = app.getString(R.string.error_adding_geofence)
    }
    fun onCheckLocationUnknownError(){
        showErrorMessage.value = app.getString(R.string.geofence_unknown_error)
    }
    fun onCheckLocationError(){
        showErrorMessage.value = app.getString(R.string.permission_denied_explanation)
    }

    fun onSaveReminder() {
        _isSaveReminderClick.value = true
    }

    fun onSaveReminderCompleted() {
        _isSaveReminderClick.value = null
    }

    fun onAddGeoFenceData() {
        _isAddGeoFenceData.value = true
    }

    fun onAddGeoFenceDataCompleted() {
        _isAddGeoFenceData.value = null
    }

    fun onNavigateToSelectLocation() {
        _isNavigateToSelectLocation.value = true
    }

    fun onNavigateToSelectLocationCompleted() {
        _isNavigateToSelectLocation.value = null
    }

    fun onSaveLocation() {
        _isSaveLocationClick.value = true
    }

    fun onSaveLocationCompleted() {
        _isSaveLocationClick.value = null
    }
}
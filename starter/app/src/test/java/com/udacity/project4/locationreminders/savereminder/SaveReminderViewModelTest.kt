package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SaveReminderViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    private val mockApplication = Mockito.mock(Application::class.java)

    private lateinit var reminderDataSource: FakeDataSource

    private lateinit var viewModel: SaveReminderViewModel

    val reminderTestData =
        ReminderDTO(
            title = "Title 1",
            description = "Description 1",
            location = "Location 1",
            latitude = 10.0,
            longitude = 10.0,
            id = UUID.randomUUID().toString()
        )

    @Before
    fun setup() {
        reminderDataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(
            mockApplication,
            reminderDataSource
        )
    }

    @Test
    fun onClear_allParamsValue_null() = runTest {
        //Given
        viewModel.apply {
            reminderTitle.value = "title"
            reminderDescription.value = "description"
            reminderSelectedLocationStr.value = "location"
            selectedPOI.value = PointOfInterest(LatLng(1.0, 1.0), "placeId", "name")
            latitude.value = 1.0
            longitude.value = 1.0
        }

        // When
        viewModel.onClear()

        //Then
        viewModel.run {
            Assert.assertEquals(null, reminderTitle.value)
            Assert.assertEquals(null, reminderDescription.value)
            Assert.assertEquals(null, reminderSelectedLocationStr.value)
            Assert.assertEquals(null, selectedPOI.value)
            Assert.assertEquals(null, latitude.value)
            Assert.assertEquals(null, longitude.value)
        }
    }

    @Test
    fun saveReminder_saveReminderToDb_success() = runTest {
        //Given
        viewModel.apply {
            reminderTitle.value = "title"
            reminderDescription.value = "description"
            reminderSelectedLocationStr.value = "location"
            selectedPOI.value = PointOfInterest(LatLng(1.0, 1.0), "placeId", "name")
            latitude.value = 1.0
            longitude.value = 1.0
        }

        viewModel.validateAndSaveReminder()

        Assert.assertEquals(true, viewModel.showLoading.getOrAwaitValue())
        advanceUntilIdle()
        Assert.assertEquals(false, viewModel.showLoading.getOrAwaitValue())
        Assert.assertEquals(true, reminderDataSource.reminders.size > 0)
    }
}

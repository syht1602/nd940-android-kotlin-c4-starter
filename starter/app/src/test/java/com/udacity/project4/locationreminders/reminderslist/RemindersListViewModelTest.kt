package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import junit.framework.TestCase
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
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    private val mockApplication = Mockito.mock(Application::class.java)

    private lateinit var reminderDataSource: FakeDataSource

    private lateinit var viewModel: RemindersListViewModel

    private val reminderTestData = listOf(
        ReminderDTO(
            title = "Title 1",
            description = "Description 1",
            location = "Location 1",
            latitude = 10.0,
            longitude = 10.0,
            id = UUID.randomUUID().toString()
        ),
        ReminderDTO(
            title = "Title 2",
            description = "Description 2",
            location = "Location 2",
            latitude = 20.0,
            longitude = 20.0,
            id = UUID.randomUUID().toString()
        ),
    )

    @Before
    fun setup() {
        reminderDataSource = FakeDataSource()
        viewModel = RemindersListViewModel(
            mockApplication,
            reminderDataSource
        )
    }

    @Test
    fun loadReminders_dbHaveNoReminders_returnEmptyReminders() = runTest {
        // Given
        reminderTestData.forEach {
            reminderDataSource.saveReminder(it)
        }

        // When
        Dispatchers.setMain(StandardTestDispatcher())
        reminderDataSource.deleteAllReminders()

        viewModel.loadReminders()
        advanceUntilIdle()
        val value = viewModel.remindersList.getOrAwaitValue()

        // Then
        Assert.assertTrue(value.isEmpty())
    }

    @Test
    fun loadReminders_dbResultError_showToastErrorMessage() = runTest {
        // Given
        reminderDataSource.isError = true

        // When
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel.loadReminders()
        advanceUntilIdle()

        // Then
        TestCase.assertEquals(
            reminderDataSource.errorMsg,
            viewModel.showSnackBar.getOrAwaitValue()
        )
    }

    @Test
    fun loadReminders_dbResultSuccess_displayLoaded() = runTest {
        // When
        // Pause dispatcher so we can verify initial values
        // Main dispatcher will not run coroutines eagerly for this test
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel.loadReminders()

        // Then
        Assert.assertEquals(
            true,
            viewModel.showLoading.getOrAwaitValue()
        )

        // Execute pending coroutine actions
        // Wait until coroutine in viewModel.validateAndSaveReminder() complete
        advanceUntilIdle()

        Assert.assertEquals(
            false,
            viewModel.showLoading.getOrAwaitValue()
        )
    }

    @Test
    fun onAddFabClick_returnNavigationToAddReminder_True() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel.onAddFabClick()
        Assert.assertEquals(true, viewModel.isNavigateToAddReminder.value)
    }

    @Test
    fun onAddFabClickCompleted_returnNavigationToAddReminder_Null() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel.onAddFabClickCompleted()
        Assert.assertEquals(null, viewModel.isNavigateToAddReminder.value)
    }

}
package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var db: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository
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
    private val insertTestData =
        ReminderDTO(
            title = "Title 3",
            description = "Description 3",
            location = "Location 3",
            latitude = 30.0,
            longitude = 30.0,
            id = UUID.randomUUID().toString()
        )

    @Before
    fun setupDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries().build()
        repository = RemindersLocalRepository(db.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun getReminders_dbAlreadyStoredReminders_notEmpty() = runTest {
        reminderTestData.forEach {
            repository.saveReminder(it)
        }

        val result = repository.getReminders()

        assert(result is Result.Success)
        assert((result as Result.Success).data.count() == 2)
    }

    @Test
    fun getReminder_dbAlreadyStoredReminders_gotReminderByGivenId() = runTest {
        reminderTestData.forEach {
            repository.saveReminder(it)
        }

        val result = repository.getReminder(reminderTestData[1].id)

        assert(result is Result.Success)
        val reminderResult = (result as Result.Success).data
        Assert.assertEquals(reminderTestData[1].id, reminderResult.id)
        Assert.assertEquals(reminderTestData[1].title, reminderResult.title)
        Assert.assertEquals(reminderTestData[1].description, reminderResult.description)
        Assert.assertEquals(reminderTestData[1].location, reminderResult.location)
        Assert.assertEquals(reminderTestData[1].latitude, reminderResult.latitude)
        Assert.assertEquals(reminderTestData[1].longitude, reminderResult.longitude)
    }

    @Test
    fun getReminder_notExistingId_gotErrorByGivenId() = runTest {
        reminderTestData.forEach {
            repository.saveReminder(it)
        }

        val result = repository.getReminder("1")

        assert(result is Result.Error)
    }

    @Test
    fun saveReminder_prepareDataForInsertToDb_returnInsertedReminder() = runTest {
        repository.saveReminder(insertTestData)

        val result = repository.getReminder(insertTestData.id)

        assert(result is Result.Success)
        val reminderResult = (result as Result.Success).data
        Assert.assertEquals(insertTestData.id, reminderResult.id)
        Assert.assertEquals(insertTestData.title, reminderResult.title)
        Assert.assertEquals(insertTestData.description, reminderResult.description)
        Assert.assertEquals(insertTestData.location, reminderResult.location)
        Assert.assertEquals(insertTestData.latitude, reminderResult.latitude)
        Assert.assertEquals(insertTestData.longitude, reminderResult.longitude)
    }

    @Test
    fun deleteAllReminders_dbAlreadyStoredReminders_allOfRemindersRemoveFromDb() = runTest {
        reminderTestData.forEach {
            repository.saveReminder(it)
        }

        repository.deleteAllReminders()

        val result = repository.getReminders()

        assert(result is Result.Success)
        assert((result as Result.Success).data.isEmpty())
    }

}
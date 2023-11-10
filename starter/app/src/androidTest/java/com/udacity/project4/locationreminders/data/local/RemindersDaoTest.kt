package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
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
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    //    TODO: Add testing implementation to the RemindersDao.kt
    private lateinit var db: RemindersDatabase
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
        db = Room.inMemoryDatabaseBuilder(getApplicationContext(), RemindersDatabase::class.java)
            .allowMainThreadQueries().build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun getReminders_dbAlreadyStoredReminders_notEmpty() = runTest {
        reminderTestData.forEach {
            db.reminderDao().saveReminder(it)
        }

        val result = db.reminderDao().getReminders()

        assert(result.count() == 2)
    }

    @Test
    fun getReminderById_dbAlreadyStoredReminders_gotReminderByGivenId() = runTest {
        reminderTestData.forEach {
            db.reminderDao().saveReminder(it)
        }

        val result = db.reminderDao().getReminderById(reminderTestData[1].id)

        Assert.assertEquals(reminderTestData[1].id, result?.id)
        Assert.assertEquals(reminderTestData[1].title, result?.title)
        Assert.assertEquals(reminderTestData[1].description, result?.description)
        Assert.assertEquals(reminderTestData[1].location, result?.location)
        Assert.assertEquals(reminderTestData[1].latitude, result?.latitude)
        Assert.assertEquals(reminderTestData[1].longitude, result?.longitude)
    }

    @Test
    fun saveReminder_prepareDataForInsertToDb_returnInsertedReminder() = runTest {
        db.reminderDao().saveReminder(insertTestData)

        val result = db.reminderDao().getReminderById(insertTestData.id)

        Assert.assertEquals(insertTestData.id, result?.id)
        Assert.assertEquals(insertTestData.title, result?.title)
        Assert.assertEquals(insertTestData.description, result?.description)
        Assert.assertEquals(insertTestData.location, result?.location)
        Assert.assertEquals(insertTestData.latitude, result?.latitude)
        Assert.assertEquals(insertTestData.longitude, result?.longitude)
    }

    @Test
    fun deleteAllReminders_dbAlreadyStoredReminders_allOfRemindersRemoveFromDb() = runTest {
        reminderTestData.forEach {
            db.reminderDao().saveReminder(it)
        }

        db.reminderDao().deleteAllReminders()

        val result = db.reminderDao().getReminders()

        assert(result.isEmpty())
    }

}
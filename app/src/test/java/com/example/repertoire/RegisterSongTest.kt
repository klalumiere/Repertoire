package com.example.repertoire

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.launchActivity
import androidx.test.platform.app.InstrumentationRegistry
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28]) // >= 29 not supported by Android Studio right now
class RegisterSongTest {
    private val contentUri = Uri.parse("content://arbitrary/uri")
    private val songName = "Pearl Jam - Black"
    private lateinit var db: AppDatabase
    private lateinit var songDao: SongDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries().build()
        songDao = db.songDao()
    }

    @Test
    fun registerSongTakesPersistableUriPermission() {
        val contentResolver = mock<ContentResolver>()
        val scenario = launchActivity<MainActivity>()
        scenario.onActivity { activity ->
            activity.registerSong(contentUri, songName, resolver = contentResolver, db = db)
        }
        verify(contentResolver).takePersistableUriPermission(contentUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    @Test
    fun unregisterSongReleasesPersistableUriPermission() {
        val contentResolver = mock<ContentResolver>()
        val scenario = launchActivity<MainActivity>()
        scenario.onActivity { activity ->
            activity.unregisterSong(contentUri, resolver = contentResolver, db = db)
        }
        verify(contentResolver).releasePersistableUriPermission(contentUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

//    @Test
//    fun registerSongAddsSongToDb() {
//        val contentResolver = mock<ContentResolver>()
//        val scenario = launchActivity<MainActivity>()
//        scenario.onActivity { activity ->
//            activity.registerSong(contentUri, songName, resolver = contentResolver, db = db)
//        }
//        val song = Song(
//            uri = contentUri.toString(),
//            name = songName
//        )
//        assertEquals(songDao.getAll(), listOf(song))
//    }
//
//    @Test
//    fun unregisterSongRemovesSongFromDb() {
//        val contentResolver = mock<ContentResolver>()
//        val scenario = launchActivity<MainActivity>()
//        scenario.onActivity { activity ->
//            activity.registerSong(contentUri, songName, resolver = contentResolver, db = db)
//            activity.unregisterSong(contentUri, resolver = contentResolver, db = db)
//        }
//        assertTrue(songDao.getAll().isEmpty())
//    }


    @After
    fun closeDb() {
        db.close()
    }
}

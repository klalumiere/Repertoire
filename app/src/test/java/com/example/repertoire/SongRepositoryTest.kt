package com.example.repertoire

import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28]) // >= 29 not supported by Android Studio right now
class SongRepositoryTest {
    private val contentUri = Uri.parse("content://arbitrary/uri")
    private val songName = "Pearl Jam - Black"
    private lateinit var contentResolver: ContentResolver
    private lateinit var db: AppDatabase
    private lateinit var register: SongRepository
    private lateinit var songDao: SongDao

    @Before
    fun createRegister() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        contentResolver = mock<ContentResolver>()
        db = AppDatabase.createInMemoryDatabaseBuilder(context).allowMainThreadQueries().build()
        register = SongRepository(contentResolver, db)
        songDao = db.songDao()
    }

    @Test
    fun addTakesPersistableUriPermission() {
        runBlocking { register.add(contentUri, songName) }
        verify(contentResolver).takePersistableUriPermission(contentUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    @Test
    fun removeReleasesPersistableUriPermission() {
        register.remove(contentUri)
        verify(contentResolver).releasePersistableUriPermission(contentUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    @Test
    fun addAddsSongToDb() {
        runBlocking { register.add(contentUri, songName) }
        val song = Song(
            uri = contentUri.toString(),
            name = songName
        )
        assertEquals(songDao.getAll(), listOf(song))
    }

    @Test
    fun addRemovesExtensionFromSongName() {
        runBlocking { register.add(contentUri, "Pantera - Walk.md") }
        val song = Song(
            uri = contentUri.toString(),
            name = "Pantera - Walk"
        )
        assertEquals(songDao.getAll(), listOf(song))
    }

    @Test
    fun removeRemovesSongFromDb() {
        runBlocking { register.add(contentUri, songName) }
        register.remove(contentUri)
        assertTrue(songDao.getAll().isEmpty())
    }

    @Test
    fun addWithoutNameUseContentResolverToFindName() {
        val cursor = mock<Cursor> {
            on { moveToFirst() } doReturn true
            on { getString(anyOrNull()) } doReturn songName
        }
        val contentResolver = mock<ContentResolver> {
            on {
                query(same(contentUri),
                    anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            } doReturn cursor
        }
        val register = SongRepository(contentResolver, db)

        runBlocking { register.add(contentUri) }

        val song = Song(
            uri = contentUri.toString(),
            name = songName
        )
        assertEquals(songDao.getAll(), listOf(song))
    }


    @After
    fun closeDb() {
        db.close()
    }
}

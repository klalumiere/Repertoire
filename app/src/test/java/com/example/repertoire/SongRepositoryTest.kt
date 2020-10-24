package com.example.repertoire

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28]) // >= 29 not supported by Android Studio right now
class SongRepositoryTest {
    private val contentUri = Uri.parse("content://arbitrary/uri")
    private val songName = "Pearl Jam - Black"
    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var db: AppDatabase
    private lateinit var register: SongRepository
    private lateinit var songDao: SongDao

    @Before
    fun createRegister() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        contentResolver = mock()
        db = AppDatabase.createInMemoryDatabaseBuilderForTests(context).allowMainThreadQueries().build()
        register = SongRepository(context).apply {
            injectContentResolverForTests(contentResolver)
            injectDatabaseForTests(db)
        }
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
        runBlocking { register.remove(contentUri) }
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
        runBlocking { register.remove(contentUri) }
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
        register.injectContentResolverForTests(contentResolver)

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

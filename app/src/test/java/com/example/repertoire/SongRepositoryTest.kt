package com.example.repertoire

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
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
    private lateinit var repository: SongRepository

    @Before
    fun createRepository() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        contentResolver = mock()
        db = AppDatabase.createInMemoryDatabaseBuilderForTests(context).allowMainThreadQueries().build()
        repository = SongRepository(context).apply {
            val nativeResolver = NativeContentResolver(context).apply {
                injectContentResolverForTests(contentResolver)
            }
            injectContentResolverForTests(nativeResolver)
            injectDatabaseForTests(db)
        }
    }

    @Test
    fun addTakesPersistableUriPermission() {
        runBlocking { repository.add(contentUri, songName) }
        verify(contentResolver).takePersistableUriPermission(contentUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    @Test
    fun removeReleasesPersistableUriPermission() {
        runBlocking { repository.remove(contentUri) }
        verify(contentResolver).releasePersistableUriPermission(contentUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    @Test
    fun addAddsSongToDb() {
        runBlocking { repository.add(contentUri, songName) }
        val song = Song(
            uri = contentUri.toString(),
            name = songName
        )
        assertEquals(repository.getAllSongs().getOrAwaitValue(), listOf(song))
    }

    @Test
    fun addRemovesExtensionFromSongName() {
        runBlocking { repository.add(contentUri, "Pantera - Walk.md") }
        val song = Song(
            uri = contentUri.toString(),
            name = "Pantera - Walk"
        )
        assertEquals(repository.getAllSongs().getOrAwaitValue(), listOf(song))
    }

    @Test
    fun removeRemovesSongFromDb() {
        runBlocking { repository.add(contentUri, songName) }
        runBlocking { repository.remove(contentUri) }
        assertTrue(repository.getAllSongs().getOrAwaitValue().isEmpty())
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
        val nativeResolver = NativeContentResolver(context).apply {
            injectContentResolverForTests(contentResolver)
        }
        repository.injectContentResolverForTests(nativeResolver)

        runBlocking { repository.add(contentUri) }

        val song = Song(
            uri = contentUri.toString(),
            name = songName
        )
        assertEquals(repository.getAllSongs().getOrAwaitValue(), listOf(song))
    }

    @Test
    fun setSongContent() {
        runBlocking { repository.setSongContent("J'entre avec l'aube") }
        val expected = SongContent(listOf(
            Verse(lyrics="J'entre avec l'aube", listOf()),
        ))
        assertEquals(expected, repository.getSongContent().getOrAwaitValue())
    }


    @After
    fun closeDb() {
        db.close()
    }

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()
}

package klalumiere.repertoire

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
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
    private val songContent = "Sheets of empty canvas"
    private lateinit var dispatcherInjector: DispatchersFactory.InjectForTests
    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var contentResolverInjector: RepertoireContentResolverFactory.InjectForTests
    private lateinit var db: AppDatabase
    private lateinit var repository: SongRepository

    @ExperimentalCoroutinesApi
    @Before
    fun createRepository() {
        dispatcherInjector = DispatchersFactory.InjectForTests(TestCoroutineDispatcher())
        context = InstrumentationRegistry.getInstrumentation().targetContext
        contentResolver = mock<ContentResolver> {
            on {
                openInputStream(same(contentUri))
            } doReturn songContent.byteInputStream()
        }
        val nativeResolver = NativeContentResolver(context).apply {
            injectContentResolverForTests(contentResolver)
        }
        contentResolverInjector = RepertoireContentResolverFactory.InjectForTests(nativeResolver)

        db = AppDatabase.createInMemoryDatabaseBuilderForTests(context).allowMainThreadQueries().build()
        repository = SongRepository(context).apply {
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
            name = songName,
            content = songContent
        )
        assertEquals(repository.getAllSongs().getOrAwaitValue(), listOf(song))
    }

    @Test
    fun addRemovesExtensionFromSongName() {
        runBlocking { repository.add(contentUri, "Pantera - Walk.md") }
        val song = Song(
            uri = contentUri.toString(),
            name = "Pantera - Walk",
            content = songContent
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
            on {
                openInputStream(same(contentUri))
            } doReturn songContent.byteInputStream()
        }
        val nativeResolver = NativeContentResolver(context).apply {
            injectContentResolverForTests(contentResolver)
        }
        RepertoireContentResolverFactory.InjectForTests(nativeResolver).use {
            val repository = SongRepository(context).apply {
                injectDatabaseForTests(db)
            }
            runBlocking { repository.add(contentUri) }

            val song = Song(
                uri = contentUri.toString(),
                name = songName,
                content = songContent
            )
            assertEquals(repository.getAllSongs().getOrAwaitValue(), listOf(song))
        }
    }

    @Test
    fun getSongContent() {
        val repository = SongRepository(context).apply {
            injectDatabaseForTests(db)
        }
        val expected = SongContent(
            listOf(
                Verse(lyrics = songContent, listOf()),
            )
        )
        assertEquals(expected, repository.getSongContent(contentUri).getOrAwaitValue())
    }


    @After
    fun closeResources() {
        db.close()
        dispatcherInjector.close()
        contentResolverInjector.close()
    }

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()
}

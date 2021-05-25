package klalumiere.repertoire

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private val uri = "content://arbitrary/uri"
    private val arbitrarySong = Song(
        uri = uri,
        name = "Led Zeppelin - Stairway to Heaven",
        content = "There’s a [l](Am)ady who’s [s](E+)ure"
    )
    private lateinit var db: AppDatabase
    private lateinit var songDao: SongDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.createInMemoryDatabaseBuilderForTests(context).build()
        songDao = db.songDao()
    }

    @Test
    fun canInsert() {
        runBlocking { songDao.insert(arbitrarySong) }
        val song = runBlocking { songDao.get(uri) }
        assertThat(song, equalTo(arbitrarySong))
    }

    @Test
    fun canDelete() {
        runBlocking { songDao.insert(arbitrarySong) }
        runBlocking { songDao.delete(arbitrarySong) }
        assertThat(songDao.getAll().getOrAwaitValue(),empty())
    }

    @Test
    fun canDeleteFromUri() {
        runBlocking { songDao.insert(arbitrarySong) }
        runBlocking { songDao.delete(arbitrarySong.uri) }
        assertThat(songDao.getAll().getOrAwaitValue(),empty())
    }

    @Test
    fun updateIfSameUri() {
        runBlocking { songDao.insert(arbitrarySong) }
        val anotherArbitrarySong = Song(
            uri = arbitrarySong.uri,
            name = "ZZ Top - La Grange",
            content = "Rumors spreadin' 'round in that Texas town"
        )
        runBlocking { songDao.insert(anotherArbitrarySong) }
        assertThat(songDao.getAll().getOrAwaitValue(), equalTo(listOf(anotherArbitrarySong)))
    }

    @Test
    fun songsAreOrderedAlphabeticallyByName() {
        val songL = Song(
            uri = "content://arbitrary/uri0",
            name = "Led Zeppelin - Stairway to Heaven",
            content = "There’s a [l](Am)ady who’s [s](E+)ure"
        )
        val songZ = Song(
            uri = "content://arbitrary/uri1",
            name = "ZZ Top - La Grange",
            content = "Rumors spreadin' 'round in that Texas town"
        )
        val songA = Song(
            uri = "content://arbitrary/uri2",
            name = "AC/DC - Back in Black",
            content = "Back in black"
        )
        songDao.insertAll(songL,songZ,songA)
        assertThat(songDao.getAll().getOrAwaitValue(),contains(songA,songL,songZ))
    }

    @Test
    fun canDeleteAll() {
        val anotherArbitrarySong = Song(
            uri = "content://arbitrary/uri0",
            name = "Led Zeppelin - Stairway to Heaven",
            content = "There’s a [l](Am)ady who’s [s](E+)ure"
        )
        songDao.insertAll(arbitrarySong, anotherArbitrarySong)
        songDao.deleteAll()
        assertThat(songDao.getAll().getOrAwaitValue(),empty())
    }


    @After
    fun closeDb() {
        db.close()
    }

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()
}

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.repertoire.AppDatabase
import com.example.repertoire.Song
import com.example.repertoire.SongDao
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private val arbitrarySong = Song(
        uri = "content://arbitrary/uri",
        name = "Led Zeppelin - Stairway to Heaven"
    )
    private lateinit var db: AppDatabase
    private lateinit var songDao: SongDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.createInMemoryDatabaseBuilder(context).build()
        songDao = db.songDao()
    }

    @Test
    fun canInsert() {
        runBlocking { songDao.insert(arbitrarySong) }
        assertThat(songDao.getAll(),contains(arbitrarySong))
    }

    @Test
    fun canDelete() {
        runBlocking { songDao.insert(arbitrarySong) }
        runBlocking { songDao.delete(arbitrarySong) }
        assertThat(songDao.getAll(),empty())
    }

    @Test
    fun canDeleteFromUri() {
        runBlocking { songDao.insert(arbitrarySong) }
        runBlocking { songDao.delete(arbitrarySong.uri) }
        assertThat(songDao.getAll(),empty())
    }

    @Test
    fun ignoreDuplicateUri() {
        runBlocking { songDao.insert(arbitrarySong) }
        runBlocking { songDao.insert(arbitrarySong) }
        assertThat(songDao.getAll(),contains(arbitrarySong))
    }

    @Test
    fun songsAreOrderedAlphabeticallyByName() {
        val songL = Song(
            uri = "content://arbitrary/uri0",
            name = "Led Zeppelin - Stairway to Heaven"
        )
        val songZ = Song(
            uri = "content://arbitrary/uri1",
            name = "ZZ Top - La Grange"
        )
        val songA = Song(
            uri = "content://arbitrary/uri2",
            name = "AC/DC - Back in Black"
        )
        songDao.insertAll(songL,songZ,songA)
        assertThat(songDao.getAll(),contains(songA,songL,songZ))
    }


    @After
    fun closeDb() {
        db.close()
    }
}

package com.example.repertoire

import android.os.Looper.getMainLooper
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nhaarman.mockitokotlin2.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28]) // >= 29 not supported by Android Studio right now
class SongViewHolderTest {
    private val song = Song(
        uri = "content://arbitrary/uri",
        name = "Pearl Jam - Black"
    )

    @Test
    fun bindHolderSetTextViewed() {
        val scenario = launchActivity<MainActivity>()
        scenario.onActivity { activity ->
            val holder = createSongViewHolder(activity)
            holder.bind(song)
            assertEquals(song.name, holder.getTextViewed())
        }
    }

    @Test
    fun bindHolderSetContentUri() {
        val scenario = launchActivity<MainActivity>()
        scenario.onActivity { activity ->
            val holder = createSongViewHolder(activity)
            holder.bind(song)
            assertEquals(song.uri, holder.getContentUri())
        }
    }

    @Test
    fun canSetActivated() {
        val scenario = launchActivity<MainActivity>()
        scenario.onActivity { activity ->
            val holder = createSongViewHolder(activity)

            holder.setActivated(false)
            assertFalse(holder.isViewActivated())

            holder.setActivated(true)
            assertTrue(holder.isViewActivated())
        }
    }

    @Test
    fun holderSelectionKeyIsUri() {
        val scenario = launchActivity<MainActivity>()
        scenario.onActivity { activity ->
            val holder = createSongViewHolder(activity)
            holder.bind(song)
            assertEquals(holder.getItemDetails().selectionKey, song.uri)
        }
    }

     @Test
    fun holderItemDetailsPositionIsHolderAdapterPosition() {
        val scenario = launchActivity<MainActivity>()
        scenario.onActivity { activity ->
            val holder = createSongViewHolder(activity)
            assertEquals(holder.adapterPosition, holder.getItemDetails().position)
        }
    }


    @Before
    fun allowMainThreadQueriesInDatabase() = createDatabaseAllowingMainThreadQueries()
    @After
    fun preventExceptions() {
        shadowOf(getMainLooper()).idle()
        closeDatabaseAllowingMainThreadQueries()
    }
}


@RunWith(JUnit4::class)
class SongItemCallbackTestTest {
    private val callback = SongItemCallback()
    private val song = Song(
        uri = "content://arbitrary/uri",
        name = "Pearl Jam - Black"
    )

    @Test
    fun identicalSongsAreTheSameItems() {
        assertTrue(callback.areItemsTheSame(song,song))
    }

    @Test
    fun songsWithSameUriAreTheSameItems() {
        val song2 = Song(
            uri = song.uri,
            name = "anotherName"
        )
        assertTrue(callback.areItemsTheSame(song,song2))
    }

    @Test
    fun songsWithDifferentUriAreNotTheSameItems() {
        val song2 = Song(
            uri = "content://anotherArbitrary/uri",
            name = song.name
        )
        assertFalse(callback.areItemsTheSame(song,song2))
    }

    @Test
    fun identicalSongsHaveTheSameContents() {
        assertTrue(callback.areContentsTheSame(song,song))
    }

    @Test
    fun songsWithDifferentNameHaveNotTheSameContents() {
        val song2 = Song(
            uri = song.uri,
            name = "anotherName"
        )
        assertFalse(callback.areContentsTheSame(song,song2))
    }

    @Test
    fun songsWithDifferentUriHaveNotTheSameContent() {
        val song2 = Song(
            uri = "content://anotherArbitrary/uri",
            name = song.name
        )
        assertFalse(callback.areContentsTheSame(song,song2))
    }
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28]) // >= 29 not supported by Android Studio right now
class SongAdapterTest {
    private val song = Song(
        uri = "content://arbitrary/uri",
        name = "Pearl Jam - Black"
    )

    @Test
    fun adapterOnBindViewHolder_BindsHolder() {
        val scenario = launchActivity<MainActivity>()
        scenario.onActivity { activity ->
            val holder = createSongViewHolder(activity)
            createSongAdapter(listOf(song)).onBindViewHolder(holder,0)
            assertEquals(song.name, holder.getTextViewed())
        }
    }

    @Test
    fun adapterOnBindViewHolder_SetActivatedToTrackerIsSelected() {
        val scenario = launchActivity<MainActivity>()
        scenario.onActivity { activity ->
            val holder = createSongViewHolder(activity)
            listOf(false,true).forEach { booleanValue ->
                val mockedTracker = mock<SelectionTracker<String>> {
                    on {
                        isSelected(same(song.uri))
                    } doReturn booleanValue
                }

                createSongAdapter(listOf(song), mockedTracker).onBindViewHolder(holder,0)

                assertEquals(booleanValue, holder.isViewActivated())
            }
        }
    }

    @Test
    fun adapterOnCreateViewHolder_createsSongViewHolder() {
        val scenario = launchActivity<MainActivity>()
        scenario.onActivity { activity ->
            val group = activity.findViewById<RecyclerView>(R.id.song_list_view)
            assertNotEquals(null,
                createSongAdapter(listOf(song)).onCreateViewHolder(group,0))
        }
    }


    @Before
    fun allowMainThreadQueriesInDatabase() = createDatabaseAllowingMainThreadQueries()
    @After
    fun preventExceptions() {
        shadowOf(getMainLooper()).idle()
        closeDatabaseAllowingMainThreadQueries()
    }
}


@RunWith(AndroidJUnit4::class)
@Config(sdk = [28]) // >= 29 not supported by Android Studio right now
class SongItemKeyProviderTest {
    private val songs = listOf(
        Song(uri = "content://arbitrary/uri", name = "Pearl Jam - Black"),
        Song(uri = "content://arbitrary/uri2", name = "Foo Fighters - Walk")
    )

    @Test
    fun getKey() {
        val provider = SongItemKeyProvider(createSongAdapter(songs))
        assertEquals(songs[0].uri, provider.getKey(0))
    }

    @Test
    fun getPosition() {
        val provider = SongItemKeyProvider(createSongAdapter(songs))
        assertEquals(1, provider.getPosition(songs[1].uri))
    }
}


private fun closeDatabaseAllowingMainThreadQueries() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    AppDatabase.getInstanceAllowingMainThreadQueriesForTests(context).close()
}

private fun createDatabaseAllowingMainThreadQueries() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    AppDatabase.getInstanceAllowingMainThreadQueriesForTests(context)
}

private fun createSongAdapter(songs: List<Song>, trackerRhs: SelectionTracker<String> = mock())
        : SongAdapter
{
    return SongAdapter().apply {
        submitList(songs)
        tracker = trackerRhs
    }
}

private fun createSongViewHolder(activity: MainActivity): SongViewHolder {
    val group = activity.findViewById<RecyclerView>(R.id.song_list_view)
    return SongViewHolder.create(group)
}

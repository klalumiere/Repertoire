package com.example.repertoire

import android.os.Looper.getMainLooper
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
class SongRecyclerViewTest {
    private val song = Song(
        uri = "content://arbitrary/uri",
        name = "Pearl Jam - Black"
    )

    @Test
    fun bindHolderSetTextViewed() {
        val scenario = launchActivity<MainActivity>()
        scenario.onActivity { activity ->
            val group = activity.findViewById<RecyclerView>(R.id.song_list_view)
            val holder = SongViewHolder.create(group)

            holder.bind(song)

            assertEquals(song.name, holder.getTextViewed())
        }
    }

    @Test
    fun bindHolderSetContentUri() {
        val scenario = launchActivity<MainActivity>()
        scenario.onActivity { activity ->
            val group = activity.findViewById<RecyclerView>(R.id.song_list_view)
            val holder = SongViewHolder.create(group)

            holder.bind(song)

            assertEquals(song.uri, holder.getContentUri())
        }
    }

    @Test
    fun canSetActivated() {
        val scenario = launchActivity<MainActivity>()
        scenario.onActivity { activity ->
            val group = activity.findViewById<RecyclerView>(R.id.song_list_view)
            val holder = SongViewHolder.create(group)

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
            val group = activity.findViewById<RecyclerView>(R.id.song_list_view)
            val holder = SongViewHolder.create(group)

            holder.bind(song)

            assertEquals(holder.getItemDetails().selectionKey, song.uri)
        }
    }

    @Before
    fun allowMainThreadQueriesInDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        AppDatabase.getInstanceAllowingMainThreadQueriesForTests(context)
    }
    @After
    fun preventExceptions() {
        shadowOf(getMainLooper()).idle()
        
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        AppDatabase.getInstanceAllowingMainThreadQueriesForTests(context).close()
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

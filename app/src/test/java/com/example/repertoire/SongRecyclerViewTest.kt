package com.example.repertoire

import android.os.Looper.getMainLooper
import androidx.recyclerview.widget.RecyclerView
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
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

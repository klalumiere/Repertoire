package klalumiere.repertoire

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28]) // >= 29 not supported by Android Studio right now
class SongContentAdapterTest {
    @Test
    fun convertsColorToHtml() {
        assertEquals("#26c6da", SongContentAdapter.convertColorToHtml(-14235942))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun rendersSongContent() {
        DispatchersFactory.InjectForTests(TestCoroutineDispatcher()).use {
            val content = MutableLiveData<SongContent>()
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val adapter = SongContentAdapter(content,42, context)

            val beforeRendering = adapter.renderedSongContent.value.toString()
            runBlocking {
                content.value = SongContent(listOf(
                    Verse(
                        lyrics="J'entre avec l'aube",
                        listOf(Chord(0,"F#"))
                    )
                ))
            }
            assertNotEquals(beforeRendering, adapter.renderedSongContent.getOrAwaitValue().toString())
        }
    }


    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()
}

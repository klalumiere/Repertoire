package com.example.repertoire

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
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

    @Test
    fun rendersSongContent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val adapter = SongContentAdapter(42, context)
        val songContent = SongContent(listOf(
            Verse(
                lyrics="J'entre avec l'aube",
                listOf(Chord(0,"F#"))
            )
        ))
        val lifecycleOwner = TestLifecycleOwner()

        val beforeRendering = adapter.getRenderedSongContent().value.toString()
        runBlocking { adapter.renderSongContent(songContent,lifecycleOwner.lifecycleScope) }
        assertNotEquals(beforeRendering, adapter.getRenderedSongContent().getOrAwaitValue().toString())
    }


    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()
}

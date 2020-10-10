package com.example.repertoire

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28]) // >= 29 not supported by Android Studio right now
class SongAdapterTest {
    private val adapter = SongAdapter()

    @Test
    fun hasStableIdsForItemSelection() {
        assertTrue(adapter.hasStableIds())
    }
}

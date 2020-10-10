package com.example.repertoire

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.runners.JUnit4

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

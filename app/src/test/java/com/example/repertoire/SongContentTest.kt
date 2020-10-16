package com.example.repertoire

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ChordBuilderTest {
    private val position = 4
    private val builder = Chord.Builder(position)

    @Test
    fun remembersPosition() {
        assertEquals(position, builder.position)
    }

    @Test
    fun chordBuilderStartsInLyricMode() {
        assertEquals(Chord.Builder.State.LYRIC, builder.state)
    }

    @Test
    fun appendsInChordMode() {
        builder.transitionToChordSateForTests()
        builder.append('F').append('#')
        assertEquals(Chord(position,"F#"), builder.build())
    }


    private fun Chord.Builder.transitionToChordSateForTests() {
        this.transition(Chord.Builder.CHORD_STATE_DELIMITER_0)
            .transition(Chord.Builder.CHORD_STATE_DELIMITER_1)
    }
}

@RunWith(JUnit4::class)
class VerseTest {
    @Test
    fun parseLyrics() {
        val verse = Verse.parse("There's a lady who's sure")
        assertEquals("There's a lady who's sure", verse.lyrics)
    }

    @Test
    fun parseDoesNotIncludeSpecialCharactersInLyrics() {
        val verse = Verse.parse("[J]()'entre avec l'aube")
        assertEquals("J'entre avec l'aube", verse.lyrics)
    }

    @Test
    fun parseIncludesEscapedSpecialCharactersInLyrics() {
        val escape = Chord.Builder.ESCAPE_CHAR
        val verse = Verse.parse("$escape[J]()'entre avec l'aube")
        assertEquals("[J'entre avec l'aube", verse.lyrics)
    }

    @Test
    fun parseDoesNotIncludeChordsInLyrics() {
        val verse = Verse.parse("[J](F#)'entre avec l'aube")
        assertEquals("J'entre avec l'aube", verse.lyrics)
    }

    @Test
    fun parseRemembersChords() {
        val verse = Verse.parse("[J](F#)'entre avec l'aube")
        assertEquals(
            Verse(
                lyrics="J'entre avec l'aube",
                listOf(Chord(0,"F#"))
            ),
            verse
        )
    }

    @Test
    fun parseRemembersChordsPositions() {
        val verse = Verse.parse("[A](A) million miles awa[y](E)")
        assertEquals(
            Verse(lyrics="A million miles away",
                listOf(Chord(0,"A"), Chord(19,"E"))
            ),
            verse
        )
    }
}

@RunWith(JUnit4::class)
class SongContentTest {
    @Test
    fun parse() {
        val songContent = """
            [J](F#)'entre avec l'aube
            [A](A) million miles awa[y](E)
        """.trimIndent()
        val song = SongContent.parse(songContent)
        assertEquals(
            SongContent(listOf(
                Verse(
                    lyrics="J'entre avec l'aube",
                    listOf(Chord(0,"F#"))
                ),
                Verse(lyrics="A million miles away",
                    listOf(Chord(0,"A"), Chord(19,"E"))
                )
            )),
            song
        )
    }
}

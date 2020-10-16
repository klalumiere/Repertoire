package com.example.repertoire

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ChordBuilderTest {
    private val position = 4L;
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
    fun takeOrReturn_ReturnsInLyricMode() {
        assertEquals('x', builder.takeOrReturn('x'))
    }

    @Test
    fun takeOrReturn_TakesInChordMode() {
        builder.transitionToChordSate()
        assertEquals(null, builder.takeOrReturn('F'))
    }

    @Test
    fun takeOrReturnUsesWhatItTookToBuildChord() {
        builder.transitionToChordSate()
        builder.takeOrReturn('F')
        builder.takeOrReturn('#')
        assertEquals(Chord(position,"F#"), builder.build())
    }


    private fun Chord.Builder.transitionToChordSate() {
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

//    @Test
//    fun parseDoNotIncludeChordsInLyrics() {
//        val verse = Verse.parse("[J](F#)'entre avec l'aube")
//        assertEquals("J'entre avec l'aube", verse.lyrics)
//    }
}

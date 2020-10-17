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
    fun chordBuilderStartsInLyricState() {
        assertEquals(Chord.Builder.State.LYRIC, builder.state)
    }

    @Test
    fun canTransitionToChordState() {
        builder.transition(Chord.Builder.CHORD_STATE_DELIMITER_0,
            Chord.Builder.CHORD_STATE_DELIMITER_1)
        assertEquals(Chord.Builder.State.CHORD, builder.state)
    }

    @Test
    fun doesNotAppendInLyricState() {
        builder.append('F').append('#')
        assertEquals(Chord(position,""), builder.build())
    }

    @Test
    fun appendsInChordState() {
        builder.transitionToChordSateForTests()
        builder.append('F').append('#')
        assertEquals(Chord(position,"F#"), builder.build())
    }


    private fun Chord.Builder.transitionToChordSateForTests() {
        this.transition(Chord.Builder.CHORD_STATE_DELIMITER_0,
            Chord.Builder.CHORD_STATE_DELIMITER_1)
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

    @Test
    fun parseEmptyVerse() {
        assertEquals(Verse(lyrics="", listOf()), Verse.parse(""))
    }


    @Test
    fun chordsToTextForNoChords() {
        assertEquals(" ".repeat(5), Verse.toText(listOf(),5))
    }

    @Test
    fun chordsToTextSingleChord() {
        val chords = listOf(Chord(0, "A"))
        assertEquals("A   ", Verse.toText(chords, 4))
    }

    @Test
    fun chordsToTextSingleChordNotFirstNorLastPosition() {
        val chords = listOf(Chord(2, "A"))
        assertEquals("  A ", Verse.toText(chords, 4))
    }

    @Test
    fun chordsToTextManyChords() {
        val chords = listOf(Chord(0, "A"), Chord(3, "E"))
        assertEquals("A  E", Verse.toText(chords, 4))
    }

    @Test
    fun chordsToTextForChordsWithManyChar() {
        val chords = listOf(Chord(0, "F#"), Chord(3, "E"))
        assertEquals("F# E", Verse.toText(chords, 4))
    }

    @Test
    fun chordsToTextForLyricLengthSmallerThanChordsLength() {
        val chords = listOf(Chord(0, "F#"), Chord(3, "G#"))
        assertEquals("F# G#", Verse.toText(chords, 3))
    }

    @Test
    fun chordsToTextAlwaysAddsAtLeastOneSpaceBetweenChords() {
        val chords = listOf(Chord(0, "F#"), Chord(1, "G#"))
        assertEquals("F# G#", Verse.toText(chords, 3))
    }


    @Test
    fun safeSubstringTypicalCase() {
        assertEquals("bcd",Verse.safeSubstring("abcde",1,3))
    }

    @Test
    fun safeSubstringBeginEqualsOrPassedEndOfString() {
        assertEquals("",Verse.safeSubstring("ab",2,3))
    }

    @Test
    fun safeSubstringLengthPassedEndOfString() {
        assertEquals("bcd",Verse.safeSubstring("abcd",1,100))
    }

    @Test
    fun safeSubstringOnlyFirstCharacter() { // test boundaries
        assertEquals("a",Verse.safeSubstring("abcd",0,1))
    }

    @Test
    fun safeSubstringOnlyLastCharacter() { // test boundaries
        assertEquals("d",Verse.safeSubstring("abcd",3,1))
    }


    @Test
    fun renderTextEmptyVerse() {
        assertEquals("", Verse(lyrics="", listOf()).renderText(1000))
    }

    @Test
    fun renderTextTypicalCase() {
        val verse = Verse.parse("[A](A) million miles awa[y](E)")
        val expected = """
            A                  E
            A million miles away
            
        """.trimIndent()
        assertEquals(expected, verse.renderText(1000))
    }

    @Test
    fun renderTextSmallScreen() {
        val verse = Verse.parse("[A](A) million miles awa[y](E)")
        val expected = """
            A         
            A million 
                     E
            miles away
            
        """.trimIndent()
        assertEquals(expected, verse.renderText(10))
    }

    @Test
    fun renderHtmlTextNoChords() {
        val verse = Verse.parse("A million miles away")
        val expected = """
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            A million miles away
            
        """.trimIndent()
        assertEquals(expected, verse.renderHtmlText(1000,"<b>%s</b>"))
    }

    @Test
    fun renderHtmlSingleChord() {
        val verse = Verse.parse("[J](F#)'entre avec l'aube")
        val expected = """
            <b>F#</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            J'entre avec l'aube
            
        """.trimIndent()
        assertEquals(expected, verse.renderHtmlText(1000,"<b>%s</b>"))
    }

    @Test
    fun renderHtmlTextTypicalCase() {
        val verse = Verse.parse("[A](A) million miles awa[y](E)")
        val expected = """
            <b>A</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>E</b>
            A million miles away
            
        """.trimIndent()
        assertEquals(expected, verse.renderHtmlText(1000,"<b>%s</b>"))
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

    @Test
    fun renderText() {
        val songContent = """
            [J](F#)'entre avec l'aube
            [A](A) million miles awa[y](E)
        """.trimIndent()
        val expected = """
            F#                 
            J'entre avec l'aube
            A                  E
            A million miles away
            
        """.trimIndent()
        assertEquals(expected,SongContent.parse(songContent).renderText(1000))
    }

    @Test
    fun renderHtmlText() {
        val songContent = """
            [J](F#)'entre avec l'aube
            [A](A) million miles awa[y](E)
        """.trimIndent()
        val expected = """
            <b>F#</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            J'entre avec l'aube
            <b>A</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>E</b>
            A million miles away
            
        """.trimIndent()
        assertEquals(expected,SongContent.parse(songContent).renderHtmlText(1000, "<b>%s</b>"))
    }
}

package klalumiere.repertoire

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ParseNotePrefixTest {
    @Test
    fun parsesAllNaturalNotes() {
        assertEquals(0 to 1, parseNotePrefix("C"))
        assertEquals(2 to 1, parseNotePrefix("D"))
        assertEquals(4 to 1, parseNotePrefix("E"))
        assertEquals(5 to 1, parseNotePrefix("F"))
        assertEquals(7 to 1, parseNotePrefix("G"))
        assertEquals(9 to 1, parseNotePrefix("A"))
        assertEquals(11 to 1, parseNotePrefix("B"))
    }

    @Test
    fun parsesAllSharps() {
        assertEquals(1 to 2, parseNotePrefix("C#"))
        assertEquals(3 to 2, parseNotePrefix("D#"))
        assertEquals(5 to 2, parseNotePrefix("E#"))
        assertEquals(6 to 2, parseNotePrefix("F#"))
        assertEquals(8 to 2, parseNotePrefix("G#"))
        assertEquals(10 to 2, parseNotePrefix("A#"))
        assertEquals(0 to 2, parseNotePrefix("B#"))
    }

    @Test
    fun parsesAllFlats() {
        assertEquals(11 to 2, parseNotePrefix("Cb"))
        assertEquals(1 to 2, parseNotePrefix("Db"))
        assertEquals(3 to 2, parseNotePrefix("Eb"))
        assertEquals(4 to 2, parseNotePrefix("Fb"))
        assertEquals(6 to 2, parseNotePrefix("Gb"))
        assertEquals(8 to 2, parseNotePrefix("Ab"))
        assertEquals(10 to 2, parseNotePrefix("Bb"))
    }

    @Test
    fun consumesOnlyRootWhenSuffixFollows() {
        assertEquals(5 to 1, parseNotePrefix("Fm"))
        assertEquals(0 to 1, parseNotePrefix("Cmaj7"))
        assertEquals(2 to 1, parseNotePrefix("Dsus4"))
    }

    @Test
    fun greedyTwoCharMatchWinsWhenAccidentalPresent() {
        assertEquals(6 to 2, parseNotePrefix("F#m"))
        assertEquals(10 to 2, parseNotePrefix("Bb7"))
    }

    @Test
    fun returnsNullForUnknownLeadingChar() {
        assertNull(parseNotePrefix("H"))
        assertNull(parseNotePrefix("N.C."))
        assertNull(parseNotePrefix("add9"))
        assertNull(parseNotePrefix(""))
        assertNull(parseNotePrefix(" C"))
        assertNull(parseNotePrefix("f#m"))
    }
}

@RunWith(JUnit4::class)
class TransposePitchTest {
    @Test
    fun zeroIsIdentity() {
        for (pitch in 0..11) {
            assertEquals(pitch, transposePitch(pitch, 0))
        }
    }

    @Test
    fun wrapsAroundOctave() {
        assertEquals(0, transposePitch(11, 1))
        assertEquals(0, transposePitch(10, 2))
        assertEquals(2, transposePitch(11, 3))
    }

    @Test
    fun handlesNegativeSemitones() {
        assertEquals(11, transposePitch(0, -1))
        assertEquals(0, transposePitch(11, -11))
        assertEquals(0, transposePitch(0, -12))
    }

    @Test
    fun handlesSemitonesLargerThanOctave() {
        assertEquals(0, transposePitch(0, 12))
        assertEquals(1, transposePitch(0, 13))
        assertEquals(0, transposePitch(0, 24))
    }
}

@RunWith(JUnit4::class)
class TransposeChordValueTest {
    @Test
    fun zeroIsIdentityForSharps() {
        assertEquals("C#", transposeChordValue("C#", 0))
        assertEquals("F#m", transposeChordValue("F#m", 0))
    }

    @Test
    fun zeroPreservesFlatsAsWritten() {
        assertEquals("Db", transposeChordValue("Db", 0))
        assertEquals("Bb", transposeChordValue("Bb", 0))
        assertEquals("Eb7", transposeChordValue("Eb7", 0))
    }

    @Test
    fun twelveIsAlsoIdentity() {
        assertEquals("Db", transposeChordValue("Db", 12))
        assertEquals("Bb", transposeChordValue("Bb", -12))
    }

    @Test
    fun transposesNaturalRoot() {
        assertEquals("G", transposeChordValue("F", 2))
        assertEquals("F#", transposeChordValue("F", 1))
        assertEquals("C", transposeChordValue("B", 1))
    }

    @Test
    fun transposesMinorChord() {
        assertEquals("F#m", transposeChordValue("Fm", 1))
        assertEquals("Gm", transposeChordValue("F#m", 1))
        assertEquals("Cm", transposeChordValue("Bm", 1))
    }

    @Test
    fun preservesQualitySuffixes() {
        assertEquals("Gmaj7", transposeChordValue("Fmaj7", 2))
        assertEquals("Gm7", transposeChordValue("Fm7", 2))
        assertEquals("Gsus", transposeChordValue("Fsus", 2))
        assertEquals("Gsus2", transposeChordValue("Fsus2", 2))
        assertEquals("Gsus4", transposeChordValue("Fsus4", 2))
        assertEquals("Gdim", transposeChordValue("Fdim", 2))
        assertEquals("Gaug", transposeChordValue("Faug", 2))
        assertEquals("G7", transposeChordValue("F7", 2))
        assertEquals("Gadd9", transposeChordValue("Fadd9", 2))
    }

    @Test
    fun normalisesFlatsToSharpsWhenTransposed() {
        assertEquals("D", transposeChordValue("Db", 1))
        assertEquals("B", transposeChordValue("Bb", 1))
        assertEquals("E", transposeChordValue("Eb", 1))
        assertEquals("Cm", transposeChordValue("Bbm", 2))
    }

    @Test
    fun wrapsRootAroundOctave() {
        assertEquals("C", transposeChordValue("B", 1))
        assertEquals("C", transposeChordValue("A#", 2))
        assertEquals("C#m", transposeChordValue("Bm", 2))
    }

    @Test
    fun transposesSlashChord() {
        assertEquals("Dsus/F#", transposeChordValue("Csus/E", 2))
        assertEquals("Gm7/A#", transposeChordValue("F#m7/A", 1))
        assertEquals("F/C", transposeChordValue("C/G", 5))
    }

    @Test
    fun transposesSlashChordWithBassQuality() {
        assertEquals("D/A7", transposeChordValue("C/G7", 2))
    }

    @Test
    fun leavesUnparseableChordUnchanged() {
        assertEquals("N.C.", transposeChordValue("N.C.", 3))
        assertEquals("H", transposeChordValue("H", 3))
        assertEquals("add9", transposeChordValue("add9", 3))
        assertEquals("", transposeChordValue("", 3))
        assertEquals("f#m", transposeChordValue("f#m", 3))
    }

    @Test
    fun leavesSlashChordWithUnparseableBassUnchangedAfterRoot() {
        assertEquals("D/Xyz", transposeChordValue("C/Xyz", 2))
    }

    @Test
    fun roundTripFromSharpFormRecoversOriginal() {
        val sharpInputs = listOf("C", "C#m", "D", "D#m7", "E", "F", "F#m", "G", "G#7", "A", "A#sus", "B", "C/G", "F#m7/A")
        for (input in sharpInputs) {
            for (n in 1..11) {
                val there = transposeChordValue(input, n)
                val back = transposeChordValue(there, 12 - n)
                assertEquals("round-trip $input @ +$n", input, back)
            }
        }
    }

    @Test
    fun returnsByteIdenticalStringAtZero() {
        val input = "F#m7/A"
        val output = transposeChordValue(input, 0)
        assertEquals(input, output)
        @Suppress("KotlinConstantConditions")
        assertEquals(true, input === output)
    }
}

@RunWith(JUnit4::class)
class ChordExtensionTest {
    @Test
    fun transposedPreservesPosition() {
        assertEquals(Chord(4, "G"), Chord(4, "F").transposed(2))
    }

    @Test
    fun transposedWithZeroIsIdentity() {
        val chord = Chord(7, "Bb")
        assertEquals(chord, chord.transposed(0))
    }
}

@RunWith(JUnit4::class)
class VerseExtensionTest {
    @Test
    fun transposesEveryChordAndLeavesLyricsAlone() {
        val verse = Verse.parse("[A](A) million miles awa[y](E)")
        val transposed = verse.transposed(2)
        assertEquals(
            Verse(lyrics = "A million miles away", listOf(Chord(0, "B"), Chord(19, "F#"))),
            transposed
        )
    }

    @Test
    fun emptyVerseTransposedIsEmpty() {
        assertEquals(Verse(lyrics = "", listOf()), Verse(lyrics = "", listOf()).transposed(3))
    }
}

@RunWith(JUnit4::class)
class SongContentExtensionTest {
    @Test
    fun transposesEveryVerse() {
        val songContent = """
            [J](F#)'entre avec l'aube
            [A](A) million miles awa[y](E)
        """.trimIndent()
        val transposed = SongContent.parse(songContent).transposed(1)
        assertEquals(
            SongContent(listOf(
                Verse(lyrics = "J'entre avec l'aube", listOf(Chord(0, "G"))),
                Verse(lyrics = "A million miles away", listOf(Chord(0, "A#"), Chord(19, "F")))
            )),
            transposed
        )
    }

    @Test
    fun renderedTextReflectsTransposition() {
        val songContent = SongContent.parse("[A](A) million miles awa[y](E)").transposed(2)
        val expected = """
            B                  F#
            A million miles away

        """.trimIndent()
        assertEquals(expected, songContent.renderText(1000))
    }
}

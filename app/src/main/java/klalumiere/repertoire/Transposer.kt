package klalumiere.repertoire

internal val SHARP_NAMES = listOf(
    "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
)

internal val NAME_TO_PITCH: Map<String, Int> = mapOf(
    "C" to 0, "B#" to 0,
    "C#" to 1, "Db" to 1,
    "D" to 2,
    "D#" to 3, "Eb" to 3,
    "E" to 4, "Fb" to 4,
    "F" to 5, "E#" to 5,
    "F#" to 6, "Gb" to 6,
    "G" to 7,
    "G#" to 8, "Ab" to 8,
    "A" to 9,
    "A#" to 10, "Bb" to 10,
    "B" to 11, "Cb" to 11
)

internal fun parseNotePrefix(s: String): Pair<Int, Int>? {
    if (s.length >= 2) {
        NAME_TO_PITCH[s.substring(0, 2)]?.let { return it to 2 }
    }
    if (s.isNotEmpty()) {
        NAME_TO_PITCH[s.substring(0, 1)]?.let { return it to 1 }
    }
    return null
}

internal fun transposePitch(pitch: Int, semitones: Int): Int =
    ((pitch + semitones) % 12 + 12) % 12

fun transposeChordValue(chordValue: String, semitones: Int): String {
    if (semitones % 12 == 0) return chordValue
    val (rootPitch, rootLen) = parseNotePrefix(chordValue) ?: return chordValue
    val afterRoot = chordValue.substring(rootLen)
    val transposedRoot = SHARP_NAMES[transposePitch(rootPitch, semitones)]
    val slashIdx = afterRoot.indexOf('/')
    if (slashIdx < 0) return "$transposedRoot$afterRoot"

    val quality = afterRoot.substring(0, slashIdx)
    val bassPart = afterRoot.substring(slashIdx + 1)
    val bassMatch = parseNotePrefix(bassPart)
        ?: return "$transposedRoot$afterRoot"
    val (bassPitch, bassLen) = bassMatch
    val transposedBass = SHARP_NAMES[transposePitch(bassPitch, semitones)]
    return "$transposedRoot$quality/$transposedBass${bassPart.substring(bassLen)}"
}

fun Chord.transposed(semitones: Int): Chord =
    copy(value = transposeChordValue(value, semitones))

fun Verse.transposed(semitones: Int): Verse =
    copy(chords = chords.map { it.transposed(semitones) })

fun SongContent.transposed(semitones: Int): SongContent =
    copy(verses = verses.map { it.transposed(semitones) })

package com.example.repertoire

import java.util.*
import kotlin.text.StringBuilder

data class Chord(
    val position: Long,
    val value: String
) {
    class Builder(val position: Long) {
        enum class Mode {
            LYRIC, CHORD
        }

        companion object {
            const val CREATE_DELIMITER = '['
            const val CHORD_MODE_DELIMITER_0 = ']'
            const val CHORD_MODE_DELIMITER_1 = '('
            const val COMPLETED_DELIMITER = ')'
            const val ESCAPE_CHAR = '\\'

            val RESERVED_CHARS = listOf(
                CREATE_DELIMITER,
                CHORD_MODE_DELIMITER_0,
                CHORD_MODE_DELIMITER_1,
                COMPLETED_DELIMITER,
                ESCAPE_CHAR
            )
        }

        var mode = Mode.LYRIC

        fun build(): Chord {
            return Chord(position, builder.toString())
        }

        fun takeOrReturn(x: Char): Char? {
            if(mode == Mode.LYRIC) return x
            builder.append(x)
            return null
        }

        private val builder = StringBuilder()
    }
}

data class Verse(
        val lyrics: String
    )
{
    companion object {
        fun parse(line: String): Verse {
            val chordBuilders = ArrayDeque<Chord.Builder>()
            val lyricsBuilder = StringBuilder()
            var lookbehind = 'x'
            for(char in line) {
                if(Chord.Builder.RESERVED_CHARS.contains(char)
                    && lookbehind != Chord.Builder.ESCAPE_CHAR)
                {

                }
                else {
                    lyricsBuilder.append(char)
                }
                lookbehind = char
            }
            return Verse(lyricsBuilder.toString())
        }
    }
}

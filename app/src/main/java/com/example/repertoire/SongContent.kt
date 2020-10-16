package com.example.repertoire

import java.util.*
import kotlin.text.StringBuilder

data class Chord(
    val position: Long,
    val value: String
) {
    class Builder(val position: Long) {
        enum class State {
            LYRIC, CHORD
        }

        companion object {
            const val CREATE_DELIMITER = '['
            const val CHORD_STATE_DELIMITER_0 = ']'
            const val CHORD_STATE_DELIMITER_1 = '('
            const val COMPLETED_DELIMITER = ')'
            const val ESCAPE_CHAR = '\\'

            val RESERVED_CHARS = listOf(
                CREATE_DELIMITER,
                CHORD_STATE_DELIMITER_0,
                CHORD_STATE_DELIMITER_1,
                COMPLETED_DELIMITER,
                ESCAPE_CHAR
            )
        }

        var state = State.LYRIC
            private set

        fun build(): Chord {
            return Chord(position, builder.toString())
        }

        fun takeOrReturn(x: Char): Char? {
            if(state == State.LYRIC) return x
            builder.append(x)
            return null
        }

        fun transition(x: Char): Builder {
            if(lookbehind == CHORD_STATE_DELIMITER_0 && x == CHORD_STATE_DELIMITER_1) {
                state = State.CHORD
            }
            lookbehind = x
            return this
        }

        private val builder = StringBuilder()
        private var lookbehind = 'x'
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
                if(char in Chord.Builder.RESERVED_CHARS
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

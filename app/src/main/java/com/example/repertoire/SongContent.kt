package com.example.repertoire

import java.util.*
import kotlin.text.StringBuilder

data class Chord(
    val position: Int,
    val value: String
) {
    class Builder(val position: Int) {
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

        fun append(x: Char): Builder {
            if (BuildConfig.DEBUG && state != State.CHORD) {
                error("Assertion `state != State.CHORD` failed")
            }
            builder.append(x)
            return this
        }

        fun build(): Chord {
            return Chord(position, builder.toString())
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
        val lyrics: String,
        val chords: List<Chord>
) {
    companion object {
        fun parse(line: String): Verse {
            val chordBuilders = ArrayDeque<Chord.Builder>()
            val chords = mutableListOf<Chord>()
            val lyricsBuilder = StringBuilder()
            var lookbehind = 'x'
            for(char in line) {
                if(char in Chord.Builder.RESERVED_CHARS
                    && lookbehind != Chord.Builder.ESCAPE_CHAR)
                {
                    when (char) {
                        Chord.Builder.CREATE_DELIMITER -> {
                            chordBuilders.push(Chord.Builder(lyricsBuilder.length))
                        }
                        Chord.Builder.COMPLETED_DELIMITER -> {
                            if(chordBuilders.isNotEmpty()) {
                                chords.add(chordBuilders.peek()!!.build())
                                chordBuilders.pop()
                            }
                        }
                        else -> {
                            chordBuilders.peek()?.transition(char)
                        }
                    }
                }
                else {
                    when (chordBuilders.peek()?.state) {
                        Chord.Builder.State.CHORD -> { chordBuilders.peek()?.append(char) }
                        else -> { lyricsBuilder.append(char) }
                    }
                }
                lookbehind = char
            }
            return Verse(lyricsBuilder.toString(), chords)
        }
    }
}

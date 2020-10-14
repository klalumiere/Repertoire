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
                if(char == Chord.Builder.CREATE_DELIMITER) {
                    chordBuilders.push(Chord.Builder(0))
                }
                else if(chordBuilders.peek()?.mode == Chord.Builder.Mode.CHORD) {
                    if(char == Chord.Builder.COMPLETED_DELIMITER) chordBuilders.pop()
                }
                else {
                    if(lookbehind == Chord.Builder.CHORD_MODE_DELIMITER_0) {
                        if(char == Chord.Builder.CHORD_MODE_DELIMITER_1) {
                            chordBuilders.peek()?.mode = Chord.Builder.Mode.CHORD
                        }
                        else {
                            lyricsBuilder.append(lookbehind)
                        }
                    }
                    else if(char != Chord.Builder.CHORD_MODE_DELIMITER_0) {
                        lyricsBuilder.append(char)
                    }
                }
                lookbehind = char
            }
            return Verse(lyricsBuilder.toString())
        }
    }
}

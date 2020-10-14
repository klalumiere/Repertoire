package com.example.repertoire

import java.lang.StringBuilder

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
            return Verse(line)
        }
    }
}

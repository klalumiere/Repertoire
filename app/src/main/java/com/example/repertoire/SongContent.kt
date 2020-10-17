package com.example.repertoire

import kotlin.math.min
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

        fun append(char: Char): Builder {
            if(state == State.CHORD) builder.append(char)
            return this
        }

        fun build(): Chord {
            return Chord(position, builder.toString())
        }

        fun transition(lookbehind: Char, char: Char): Builder {
            if(lookbehind == CHORD_STATE_DELIMITER_0 && char == CHORD_STATE_DELIMITER_1) {
                state = State.CHORD
            }
            return this
        }

        private val builder = StringBuilder()
    }
}

data class Verse(
        val lyrics: String,
        val chords: List<Chord>
) {
    companion object {
        fun parse(line: String): Verse {
            return VerseParser().parse(line)
        }

        fun toText(chords: List<Chord>, lyricsLength: Int): String {
            val builder =  StringBuilder()
            val addSpaces = { count: Int ->
                if(count > 0) builder.append(" ".repeat(count))
            }
            for(chord in chords) {
                addSpaces(chord.position - builder.length)
                builder.append(chord.value)
                if(chord !== chords.last()) addSpaces(1)
            }
            addSpaces(lyricsLength - builder.length)
            return builder.toString()
        }

        fun safeSubstring(string: String, begin: Int, length: Int): String {
            if(begin >= string.length) return ""
            return string.substring(begin, min(string.length, begin + length))
        }
    }

    fun renderText(screenWidth: Int, chordLineDecorator: (String) -> String = { x -> x },
                   newline: String = "\n"): String
    {
        val builder =  StringBuilder()
        val chordsAsText = toText(chords, lyrics.length)
        for(i in lyrics.indices step screenWidth) {
            builder.append(chordLineDecorator(safeSubstring(chordsAsText, i, screenWidth)))
            builder.append(newline)
            builder.append(safeSubstring(lyrics, i, screenWidth))
            builder.append(newline)
        }
        return builder.toString()
    }

    fun renderHtmlText(screenWidth: Int, chordFormat: String): String {
        val chordLineDecorator = { line: String ->
            val result = StringBuilder()
            var chordBuffer = StringBuilder()
            for(char in line) {
                when(char) {
                    ' ' -> {
                        if(chordBuffer.isNotEmpty()) {
                            result.append(chordFormat.format(chordBuffer))
                            chordBuffer = StringBuilder()
                        }
                        result.append("&nbsp;")
                    }
                    else -> chordBuffer.append(char)
                }
            }
            if(chordBuffer.isNotEmpty()) result.append(chordFormat.format(chordBuffer))
            result.toString()
        }
        return renderText(screenWidth, chordLineDecorator=chordLineDecorator, newline="<br>\n")
    }


    private class VerseParser {
        fun parse(line: String): Verse {
            for(char in line) {
                if(isReserved(char)) parseReserved(char)
                else parseRegular(char)
                lookbehind = char
            }
            return Verse(lyricsBuilder.toString(), chords)
        }


        private fun isReserved(char: Char): Boolean {
            return char in Chord.Builder.RESERVED_CHARS && lookbehind != Chord.Builder.ESCAPE_CHAR
        }

        private fun parseReserved(char: Char) {
            when (char) {
                Chord.Builder.CREATE_DELIMITER -> {
                    chordBuilder = Chord.Builder(lyricsBuilder.length)
                }
                Chord.Builder.COMPLETED_DELIMITER -> {
                    chordBuilder?.build().also {
                        if(it != null) chords.add(it)
                    }
                    chordBuilder = null
                }
                else -> {
                    chordBuilder?.transition(lookbehind, char)
                }
            }
        }

        private fun parseRegular(char: Char) {
            when (chordBuilder?.state) {
                Chord.Builder.State.CHORD -> { chordBuilder?.append(char) }
                else -> { lyricsBuilder.append(char) }
            }
        }

        private var chordBuilder: Chord.Builder? = null
        private var lookbehind = 'x'
        private val chords = mutableListOf<Chord>()
        private val lyricsBuilder = StringBuilder()
    }
}

data class SongContent(val verses: List<Verse>) {
    companion object {
        fun parse(songContent: String): SongContent {
            return SongContent(songContent.lines().map { Verse.parse(it) })
        }
    }

    fun renderText(screenWidth: Int, chordLineDecorator: (String) -> String = { x -> x },
                   newline: String = "\n"): String
    {
        val builder =  StringBuilder()
        verses.forEach {
            builder.append(it.renderText(screenWidth, chordLineDecorator=chordLineDecorator,
                newline=newline))
        }
        return builder.toString()
    }

    fun renderHtmlText(screenWidth: Int, chordFormat: String): String {
        val builder =  StringBuilder()
        verses.forEach { builder.append(it.renderHtmlText(screenWidth, chordFormat)) }
        return builder.toString()
    }
}

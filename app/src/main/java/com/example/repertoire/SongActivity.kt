package com.example.repertoire

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_song.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class SongActivity : AppCompatActivity() {
    companion object {
        const val SONG_NAME = "SongActivity::SONG_NAME"
        const val SONG_URI_AS_STRING = "SongActivity::SONG_URI_AS_STRING"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song)
        supportActionBar?.hide()

        val bundle = intent.extras!!
        val songName = bundle[SONG_NAME] as String
        val songUriAsString = bundle[SONG_URI_AS_STRING] as String
        val songContent = readSongFile(Uri.parse(songUriAsString)) // TODO: This should not be on the main thread
        song_text_view.text = "$songName $songContent"
    }


    private fun readSongFile(uri: Uri): String {
        try {
            return readSongFileUnsafe(uri)
        }
        catch(e: IOException) // Includes FileNotFoundException
        { }
        return "Cannot read file \uD83D\uDE1E" // sad emoji
    }

    private fun readSongFileUnsafe(uri: Uri): String {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }
}
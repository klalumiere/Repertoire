package com.example.repertoire

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_song.*

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
        song = Song (
            name = bundle[SONG_NAME] as String,
            uri = bundle[SONG_URI_AS_STRING] as String
        )

        song_text_view.viewTreeObserver.addOnGlobalLayoutListener { onGlobalLayoutListener() }

        songViewModel.setSongContent(Uri.parse(song.uri))
    }


    private fun convertToHtml(htmlText: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(htmlText)
        }
    }

    // Need to be called in or after `addOnGlobalLayoutListener` to call `paint` and `measuredWidth`
    private fun getScreenWidthInChar(): Int {
        val widthOfM = song_text_view.paint.measureText("M")
        return if (widthOfM > 0) {
            // Assumes monospace.
            // Moreover, could be problematic with non extended ascii (e.g. arabic char)
            (song_text_view.measuredWidth/widthOfM).toInt()
        } else {
            Log.w("SongActivity","The width of `M` is 0.")
            30
        }
    }

    private fun onGlobalLayoutListener() {
        if(songContentObserver != null) return
        // Need to be called in or after `addOnGlobalLayoutListener`
        val screenWidthInChar = getScreenWidthInChar()
        songContentObserver = Observer<SongContent> { content ->
            val htmlText = content.renderHtmlText(screenWidthInChar,
                "<b>%s</b>")
            song_text_view.text = convertToHtml(htmlText)
        }
        songViewModel.songContent.observe(this, songContentObserver!!)
    }


    private lateinit var song: Song
    private var songContentObserver: Observer<SongContent>? = null
    private val songViewModel: SongViewModel by viewModels()
}

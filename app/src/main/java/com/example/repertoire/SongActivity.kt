package com.example.repertoire

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
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

        val observer = Observer<SongContent> { content ->
            val htmlText = content.renderHtmlText(song_text_view.maxWidth,"<b>%s</b>")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                song_text_view.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT)
            } else {
                song_text_view.text = Html.fromHtml(htmlText)
            }
        }
        songViewModel.songContent.observe(this, observer)
        songViewModel.setSongContent(Uri.parse(song.uri))
    }


    private lateinit var song: Song
    private val songViewModel: SongViewModel by viewModels()
}

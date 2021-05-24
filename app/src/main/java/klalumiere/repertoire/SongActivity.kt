package klalumiere.repertoire

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import klalumiere.repertoire.databinding.ActivitySongBinding

class SongActivity : AppCompatActivity() {
    companion object {
        const val SONG_NAME = "SongActivity::SONG_NAME"
        const val SONG_URI_AS_STRING = "SongActivity::SONG_URI_AS_STRING"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()

        val bundle = intent.extras!!
        song = Song (
            name = bundle[SONG_NAME] as String,
            uri = bundle[SONG_URI_AS_STRING] as String
        )

        binding.songTitleTextView.text = song.name
        binding.songTextView.viewTreeObserver.addOnGlobalLayoutListener { onGlobalLayoutListener() }
    }


    // Need to be called in or after `addOnGlobalLayoutListener` to call `paint` and `measuredWidth`
    private fun getScreenWidthInChar(): Int {
        val widthOfM = binding.songTextView.paint.measureText("M")
        return if (widthOfM > 0) {
            // Assumes monospace.
            // Moreover, could be problematic with non extended ascii (e.g. arabic char)
            (binding.songTextView.measuredWidth/widthOfM).toInt()
        } else {
            Log.w("SongActivity", "The width of `M` is 0.")
            30
        }
    }

    private fun onGlobalLayoutListener() {
        if(songContentAdapter != null) return
        // Need to be called in or after `addOnGlobalLayoutListener`
        songContentAdapter = SongContentAdapter(
            songViewModel.getSongContent(Uri.parse(song.uri)),
            getScreenWidthInChar(),
            this
        )
        songContentAdapter?.renderedSongContent?.observe(this, { content ->
            binding.songTextView.text = content
        })
    }

    private lateinit var binding: ActivitySongBinding
    private lateinit var song: Song
    private var songContentAdapter: SongContentAdapter? = null
    private val songViewModel: SongViewModel by viewModels()
}

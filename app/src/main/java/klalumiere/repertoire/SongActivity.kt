package klalumiere.repertoire

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import klalumiere.repertoire.databinding.ActivitySongBinding

class SongActivity : AppCompatActivity() {
    companion object {
        const val SONG_NAME = "SongActivity::SONG_NAME"
        const val SONG_URI_AS_STRING = "SongActivity::SONG_URI_AS_STRING"
        private const val OVERLAP_LINES = 3
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }
        supportActionBar?.hide()

        val bundle = intent.extras!!
        song = Song (
            name = bundle.getString(SONG_NAME).toString(),
            uri = bundle.getString(SONG_URI_AS_STRING).toString(),
            content = null
        )

        binding.songTitleTextView.text = song.name
        binding.songTextView.viewTreeObserver.addOnGlobalLayoutListener { onGlobalLayoutListener() }

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val lineHeight = binding.songTextView.lineHeight
                val pageScroll = (binding.songScrollView.height - OVERLAP_LINES * lineHeight)
                        .coerceAtLeast(lineHeight)
                if (e.x < binding.songScrollView.width / 2f) {
                    binding.songScrollView.smoothScrollBy(0, -pageScroll)
                } else {
                    binding.songScrollView.smoothScrollBy(0, pageScroll)
                }
                return true
            }
        })
        binding.songScrollView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
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

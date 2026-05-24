package klalumiere.repertoire

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import klalumiere.repertoire.databinding.ActivitySongBinding

class SongActivity : AppCompatActivity() {
    companion object {
        const val SONG_NAME = "SongActivity::SONG_NAME"
        const val SONG_URI_AS_STRING = "SongActivity::SONG_URI_AS_STRING"
        private const val MIN_TEXT_SCALE = 0.5f
        private const val MAX_TEXT_SCALE = 5.0f
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

        defaultSongTextSizePx = binding.songTextView.textSize
        defaultTitleTextSizePx = binding.songTitleTextView.textSize

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                textScale = 1f
                applyTextScale()
                reflowSongContent()
                return true
            }
        })
        val scaleGestureDetector = ScaleGestureDetector(
            this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    textScale = (textScale * detector.scaleFactor)
                            .coerceIn(MIN_TEXT_SCALE, MAX_TEXT_SCALE)
                    applyTextScale()
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    reflowSongContent()
                }
            }
        )
        binding.songScrollView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            scaleGestureDetector.isInProgress
        }
    }

    private fun applyTextScale() {
        binding.songTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultSongTextSizePx * textScale)
        binding.songTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTitleTextSizePx * textScale)
    }

    private fun reflowSongContent() {
        songContentAdapter?.setScreenWidthInChar(getScreenWidthInChar())
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
    private var defaultSongTextSizePx: Float = 0f
    private var defaultTitleTextSizePx: Float = 0f
    private var textScale: Float = 1f
}

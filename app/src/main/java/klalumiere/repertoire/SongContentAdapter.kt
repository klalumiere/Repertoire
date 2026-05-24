package klalumiere.repertoire

import android.content.Context
import android.text.Spanned
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineDispatcher

class SongContentAdapter(
    val content: LiveData<SongContent>,
    initialScreenWidthInChar: Int,
    context: Context,
    private val semitones: Int = 0
) {
    companion object {
        fun convertColorToHtml(color: Int): String {
            val x = Integer.toHexString(color).substring(2) // strip alpha value
            return "#$x"
        }
    }

    fun setScreenWidthInChar(value: Int) {
        if (screenWidthInChar.value != value) screenWidthInChar.value = value
    }

    private val screenWidthInChar = MutableLiveData(initialScreenWidthInChar)

    val renderedSongContent: LiveData<Spanned> = screenWidthInChar.switchMap { width ->
        content.switchMap { songContent ->
            liveData(cpuDispatcher) {
                emit(renderSongContent(songContent.transposed(semitones), width))
            }
        }
    }


    private fun renderSongContent(content: SongContent, screenWidthInChar: Int): Spanned {
        val htmlText = content.renderHtmlText(screenWidthInChar,
            "<font color='${convertColorToHtml(chordColor)}'><b>%s</b></font>")
        return HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

    private val chordColor = ContextCompat.getColor(context, R.color.colorSecondary)
    private val cpuDispatcher: CoroutineDispatcher = DispatchersFactory.createDefaultDispatcher()
}

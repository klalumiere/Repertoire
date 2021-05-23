package klalumiere.repertoire

import android.content.Context
import android.text.Spanned
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class SongContentAdapter(
    val content: LiveData<SongContent>,
    private val screenWidthInChar: Int,
    context: Context,
    private val cpuDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    companion object {
        fun convertColorToHtml(color: Int): String {
            val x = Integer.toHexString(color).substring(2) // strip alpha value
            return "#$x"
        }
    }

    val renderedSongContent = content.switchMap {
        liveData(cpuDispatcher) {
            emit(renderSongContent(it))
        }
    }


    private fun renderSongContent(content: SongContent): Spanned {
        val htmlText = content.renderHtmlText(screenWidthInChar,
            "<font color='${convertColorToHtml(chordColor)}'><b>%s</b></font>")
        return HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

    private val chordColor = ContextCompat.getColor(context, R.color.colorSecondary)
}

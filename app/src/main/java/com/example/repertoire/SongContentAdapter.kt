package klalumiere.repertoire

import android.content.Context
import android.text.Spanned
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.*

class SongContentAdapter(
    val content: LiveData<SongContent>,
    private val screenWidthInChar: Int,
    context: Context
) {
    companion object {
        fun convertColorToHtml(color: Int): String {
            val x = Integer.toHexString(color).substring(2) // strip alpha value
            return "#$x"
        }
    }

    val renderedSongContent = content.switchMap {
        // TODO: add withContext to do this in another thread?
        liveData {
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

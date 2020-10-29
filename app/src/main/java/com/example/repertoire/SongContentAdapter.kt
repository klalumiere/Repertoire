package klalumiere.repertoire

import android.content.Context
import android.text.Spanned
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch

class SongContentAdapter(
    private val screenWidthInChar: Int,
    context: Context
) {
    companion object {
        fun convertColorToHtml(color: Int): String {
            val x = Integer.toHexString(color).substring(2) // strip alpha value
            return "#$x"
        }
    }

    fun getRenderedSongContent(): LiveData<Spanned> {
        return renderedSongContent
    }

    fun renderSongContent(content: SongContent, scope: LifecycleCoroutineScope) = scope.launch {
        val htmlText = content.renderHtmlText(screenWidthInChar,
            "<font color='${convertColorToHtml(chordColor)}'><b>%s</b></font>")
        renderedSongContent.value = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }


    private val renderedSongContent = MutableLiveData<Spanned>()
    private val chordColor = ContextCompat.getColor(context, R.color.colorSecondary)
}

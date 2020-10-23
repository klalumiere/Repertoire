package com.example.repertoire

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
    fun getRenderedSongContent(): LiveData<Spanned> {
        return renderedSongContent
    }

    fun renderSongContent(content: SongContent, scope: LifecycleCoroutineScope) = scope.launch {
        val colorString = Integer.toHexString(chordColor).substring(2) // strip alpha value
        val htmlText = content.renderHtmlText(screenWidthInChar,
            "<font color='#$colorString'><b style='color:'>%s</b></font>")
        renderedSongContent.value = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

    private val renderedSongContent = MutableLiveData<Spanned>()
    private val chordColor = ContextCompat.getColor(context, R.color.colorSecondary)
}

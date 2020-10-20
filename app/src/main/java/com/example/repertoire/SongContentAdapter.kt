package com.example.repertoire

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch

class SongContentAdapter(
    private val screenWidthInChar: Int,
//    context: Context
) {
    fun getRenderedSongContent(): LiveData<Spanned> {
        return renderedSongContent
    }

    fun renderSongContent(content: SongContent, scope: LifecycleCoroutineScope) = scope.launch {
        val htmlText = content.renderHtmlText(screenWidthInChar,
            "<b>%s</b>")
        renderedSongContent.value = convertToHtml(htmlText)
    }


    private fun convertToHtml(htmlText: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(htmlText)
        }
    }

    private val renderedSongContent = MutableLiveData<Spanned>()
//    private val chordColor = context.resources.getColor(R.color.colorSecondary)
}

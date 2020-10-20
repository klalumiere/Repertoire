package com.example.repertoire

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SongViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SongRepository(application)
    val songList = repository.getAllSongsLive()
    val songContent = repository.getSongContentLive()

    fun add(uris: List<Uri>) = viewModelScope.launch {
        uris.forEach { uri -> repository.add(uri) }
    }

    fun remove(uris: List<Uri>) = viewModelScope.launch {
        uris.forEach { uri -> repository.remove(uri) }
    }

    fun setSongContent(uri: Uri, scope: LifecycleCoroutineScope) = scope.launch {
        repository.setSongContent(uri)
    }
}

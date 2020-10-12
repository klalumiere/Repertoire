package com.example.repertoire

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SongViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SongRepository(application)
    val songList = repository.getAllSongsLive()

    fun add(uris: List<Uri>) = viewModelScope.launch {
        uris.forEach { uri -> repository.add(uri) }
    }
}

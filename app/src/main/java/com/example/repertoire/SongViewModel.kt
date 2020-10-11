package com.example.repertoire

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel

class SongViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SongRepository(application)
    val songList = repository.getAllSongsLive()

    fun add(uris: List<Uri>) {
        uris.forEach { uri -> repository.add(uri) }
    }
}


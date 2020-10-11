package com.example.repertoire

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class SongViewModel(application: Application) : AndroidViewModel(application) {
    private val songDao = AppDatabase.getInstance(application).songDao()
    val songList = songDao.getAllLive()
}


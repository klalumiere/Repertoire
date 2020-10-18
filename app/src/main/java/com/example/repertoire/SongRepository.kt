package com.example.repertoire

import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.io.IOException

class SongRepository(
    private val resolver: ContentResolver,
    private val db: AppDatabase
)
{
    constructor(application: Application)
            : this(application.contentResolver, AppDatabase.getInstance(application))

    suspend fun add(uri: Uri) {
        add(uri, resolveName(uri))
    }

    suspend fun add(uri: Uri, name: String) {
        resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        songDao.insert(Song(uri = uri.toString(), name = File(name).nameWithoutExtension))
    }

    fun getAllSongsLive(): LiveData<List<Song>> {
        return songDao.getAllLive()
    }

    fun getSongContentLive(): LiveData<SongContent> {
        return songContent
    }

    suspend fun remove(uri: Uri) {
        resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        songDao.delete(uri.toString())
    }

    fun setSongContent(uri: Uri) {
        songContent.value = SongContent.parse(readSongFile(uri))
    }


    private fun resolveName(uri: Uri): String {
        var name = "Name Not Found"
        val cursor: Cursor? = resolver.query(uri,
            arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(index >= 0) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }

    private fun readSongFile(uri: Uri): String {
        try {
            resolver.openInputStream(uri)?.use { stream ->
                return stream.readBytes().toString(Charsets.UTF_8)
            }
        }
        catch(e: IOException)
        { }
        val sadEmoji = "\uD83D\uDE1E"
        return "Cannot read file $sadEmoji"
    }

    private val songDao = db.songDao()
    private val songContent = MutableLiveData<SongContent>()
}

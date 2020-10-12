package com.example.repertoire

import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

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

    fun getSongContentLive(): LiveData<String> {
        return songContent
    }

    suspend fun remove(uri: Uri) {
        resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        songDao.delete(uri.toString())
    }

    fun setSongContent(uri: Uri) {
        songContent.value = readSongFile(uri)
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
            return readSongFileUnsafe(uri)
        }
        catch(e: IOException) // Includes FileNotFoundException
        { }
        return "Cannot read file \uD83D\uDE1E" // sad emoji
    }

    private fun readSongFileUnsafe(uri: Uri): String {
        val stringBuilder = StringBuilder()
        resolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }

    private val songDao = db.songDao()
    private val songContent = MutableLiveData<String>()
}

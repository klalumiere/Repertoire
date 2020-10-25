package com.example.repertoire

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.io.IOException

class SongRepository(context: Context) {
    suspend fun add(uri: Uri) {
        add(uri, resolveName(uri))
    }

    suspend fun add(uri: Uri, name: String) {
        resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        songDao.insert(Song(uri = uri.toString(), name = File(name).nameWithoutExtension))
    }

    fun getAllSongs(): LiveData<List<Song>> {
        return songDao.getAll()
    }

    fun getSongContent(): LiveData<SongContent> {
        return songContent
    }

    suspend fun remove(uri: Uri) {
        resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        songDao.delete(uri.toString())
    }

    fun setSongContent(uri: Uri) {
        songContent.value = SongContent.parse(readSongFile(uri))
    }


    fun injectContentResolverForTests(resolverRhs: ContentResolver) {
        resolver = resolverRhs
    }

    fun injectDatabaseForTests(db: AppDatabase) {
        songDao = db.songDao()
    }


    private fun resolveName(uri: Uri): String {
        var name = nameNotFoundErrorMessage
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
        return cannotReadFileErrorMessage
    }

    private val cannotReadFileErrorMessage =
        context.resources.getString(R.string.cannot_read_file_error_message)
    private val nameNotFoundErrorMessage =
        context.resources.getString(R.string.name_not_found_error_message)

    private var resolver = context.contentResolver
    private var songDao = AppDatabase.getInstance(context).songDao()
    private val songContent = MutableLiveData<SongContent>()
}

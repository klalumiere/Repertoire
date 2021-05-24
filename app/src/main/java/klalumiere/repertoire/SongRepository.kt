package klalumiere.repertoire

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class SongRepository(
    context: Context
) {
    suspend fun add(uri: Uri) = withContext(ioDispatcher) {
        add(uri, resolveName(uri))
    }

    suspend fun add(uri: Uri, name: String) = withContext(ioDispatcher) {
        resolver.takePersistableUriPermission(uri)
        songDao.insert(Song(uri = uri.toString(), name = File(name).nameWithoutExtension))
    }

    fun getAllSongs(): LiveData<List<Song>> {
        return songDao.getAll()
    }

    fun getSongContent(uri: Uri): LiveData<SongContent> = liveData(ioDispatcher) {
        emit(SongContent.parse(readSongFile(uri)))
    }

    suspend fun remove(uri: Uri) = withContext(ioDispatcher) {
        resolver.releasePersistableUriPermission(uri)
        songDao.delete(uri.toString())
    }


    fun injectDatabaseForTests(db: AppDatabase) {
        songDao = db.songDao()
    }


    private fun resolveName(uri: Uri): String {
        return resolver.resolveName(uri)
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

    private var resolver: RepertoireContentResolver = RepertoireContentResolverFactory.create(context)
    private var songDao = AppDatabase.getInstance(context).songDao()
    private val ioDispatcher: CoroutineDispatcher = DispatchersFactory.createIODispatcher()
}

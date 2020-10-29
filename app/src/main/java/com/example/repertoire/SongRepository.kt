package klalumiere.repertoire

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.io.IOException

class SongRepository(context: Context) {
    suspend fun add(uri: Uri) {
        add(uri, resolveName(uri))
    }

    suspend fun add(uri: Uri, name: String) {
        resolver.takePersistableUriPermission(uri)
        songDao.insert(Song(uri = uri.toString(), name = File(name).nameWithoutExtension))
    }

    fun getAllSongs(): LiveData<List<Song>> {
        return songDao.getAll()
    }

    fun getSongContent(): LiveData<SongContent> {
        return songContent
    }

    suspend fun remove(uri: Uri) {
        resolver.releasePersistableUriPermission(uri)
        songDao.delete(uri.toString())
    }

    fun setSongContent(content: String) {
        songContent.value = SongContent.parse(content)
    }

    fun setSongContent(uri: Uri) {
        setSongContent(readSongFile(uri))
    }


    fun injectContentResolverForTests(resolverRhs: RepertoireContentResolver) {
        resolver = resolverRhs
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

    private var resolver: RepertoireContentResolver = NativeContentResolver(context)
    private var songDao = AppDatabase.getInstance(context).songDao()
    private val songContent = MutableLiveData<SongContent>()
}

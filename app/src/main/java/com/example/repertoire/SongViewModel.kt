package klalumiere.repertoire

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SongViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SongRepository(application)
    val songList = repository.getAllSongs()

    fun add(uris: List<Uri>) = viewModelScope.launch {
        uris.forEach { uri -> repository.add(uri) }
    }

    fun getSongContent(uri: Uri): LiveData<SongContent> {
        return repository.getSongContent(uri)
    }

    fun remove(uris: List<Uri>) = viewModelScope.launch {
        uris.forEach { uri -> repository.remove(uri) }
    }
}

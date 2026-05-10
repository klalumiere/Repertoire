package klalumiere.repertoire

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SongViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SongRepository(application)
    private val query = MutableLiveData("")
    val songList: LiveData<List<Song>> = query.switchMap { q ->
        if (q.isBlank()) repository.getAllSongs() else repository.getMatching(q)
    }

    fun add(uris: List<Uri>) = viewModelScope.launch {
        uris.forEach { uri -> repository.add(uri) }
    }

    fun getSongContent(uri: Uri): LiveData<SongContent> {
        return repository.getSongContent(uri)
    }

    fun remove(uris: List<Uri>) = viewModelScope.launch {
        uris.forEach { uri -> repository.remove(uri) }
    }

    fun setQuery(q: String) {
        if (query.value != q) query.value = q
    }
}

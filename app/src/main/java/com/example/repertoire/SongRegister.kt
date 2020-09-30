import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import com.example.repertoire.AppDatabase
import com.example.repertoire.Song

class SongRegister(
    private val resolver: ContentResolver,
    private val db: AppDatabase
)
{
    fun add(uri: Uri, name: String) {
        resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        db.songDao().insert(Song(uri = uri.toString(), name = name))
    }

    fun remove(uri: Uri) {
        resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        db.songDao().delete(uri.toString())
    }
}

import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.example.repertoire.AppDatabase
import com.example.repertoire.Song

class SongRegister(
    private val resolver: ContentResolver,
    private val db: AppDatabase
)
{
    fun add(uri: Uri) {
        add(uri, resolveName(uri))
    }

    fun add(uri: Uri, name: String) {
        resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        db.songDao().insert(Song(uri = uri.toString(), name = name))
    }

    fun remove(uri: Uri) {
        resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        db.songDao().delete(uri.toString())
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
}

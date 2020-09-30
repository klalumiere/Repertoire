import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.launchActivity
import com.example.repertoire.MainActivity
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28]) // 29 not supported right now by Android Studio
class RegisterSongTest {
    private val contentUri = Uri.parse("content://arbitrary/uri")

    @Test
    fun registerSongTakesPersistableUriPermission() {
        val contentResolver = mock<ContentResolver>()

        val scenario = launchActivity<MainActivity>()
        scenario.onActivity { activity ->
            activity.registerSong(contentUri, resolver = contentResolver)
        }

        verify(contentResolver).takePersistableUriPermission(contentUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}

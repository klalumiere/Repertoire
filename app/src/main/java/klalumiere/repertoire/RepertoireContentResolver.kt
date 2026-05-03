package klalumiere.repertoire

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.InputStream

interface RepertoireContentResolver {
    fun openInputStream(uri: Uri): InputStream?
    fun resolveName(uri: Uri): String
}

class NativeContentResolver(context: Context): RepertoireContentResolver {
    override fun openInputStream(uri: Uri): InputStream? {
        return resolver.openInputStream(uri)
    }

    override fun resolveName(uri: Uri): String {
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


    fun injectContentResolverForTests(resolverRhs: ContentResolver) {
        resolver = resolverRhs
    }


    private val nameNotFoundErrorMessage =
        context.resources.getString(R.string.name_not_found_error_message)

    private var resolver = context.contentResolver
}

class AssetContentResolver(context: Context): RepertoireContentResolver {
    override fun openInputStream(uri: Uri): InputStream? {
        return assets.open(convertToPath(uri))
    }

    override fun resolveName(uri: Uri): String {
        return convertToPath(uri)
    }


    private fun convertToPath(uri: Uri): String {
        return uri.toString().removePrefix("file:///android_asset/").replace("%20", " ")
    }

    private val assets = context.resources.assets
}

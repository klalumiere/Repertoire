package com.example.repertoire

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.addSongsFAB).setOnClickListener {
            addSongsContract.launch(arrayOf("text/*"))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun registerSong(uri: Uri, name: String, resolver: ContentResolver = contentResolver,
        db: AppDatabase = AppDatabase.getInstance(this))
    {
        resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        db.songDao().insert(Song(uri = uri.toString(), name = name))
    }

    fun unregisterSong(uri: Uri, resolver: ContentResolver = contentResolver,
        db: AppDatabase = AppDatabase.getInstance(this))
    {
        resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        db.songDao().delete(uri.toString())
    }


    class OpenableMultipleDocuments : ActivityResultContracts.OpenMultipleDocuments() {
        override fun createIntent(context: Context, input: Array<out String>): Intent {
            return super.createIntent(context, input).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        }
    }
    private val addSongsContract = registerForActivityResult(OpenableMultipleDocuments())
    { uris: List<Uri> ->
        uris.forEach() { uri -> registerSong(uri, "") }
    }
}

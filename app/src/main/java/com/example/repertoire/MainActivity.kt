package com.example.repertoire

import SongRegister
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.addSongsFAB).setOnClickListener {
            addSongsContract.launch(arrayOf("text/*"))
        }


        val viewManager = LinearLayoutManager(this)
        val viewModel: SongViewModel by viewModels()
        val songAdapter = SongAdapter()
        val observer = Observer<List<Song>> { list -> songAdapter.submitList(list) }
        viewModel.songList.observe(this, observer)

        songListView = findViewById<RecyclerView>(R.id.song_list_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = songAdapter
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

    class OpenableMultipleDocuments : ActivityResultContracts.OpenMultipleDocuments() {
        override fun createIntent(context: Context, input: Array<out String>): Intent {
            return super.createIntent(context, input).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        }
    }
    private val addSongsContract = registerForActivityResult(OpenableMultipleDocuments())
    { uris: List<Uri> ->
        val register = SongRegister(contentResolver, AppDatabase.getInstance(this))
        Thread(Runnable {
            uris.forEach() { uri -> register.add(uri) }
        }).start()
    }

    private lateinit var songListView: RecyclerView
}

package com.example.repertoire

import SongRegister
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        addSongsFAB.setOnClickListener { addSongsLauncher.launch(arrayOf("text/*")) }

        linearLayoutManager = LinearLayoutManager(this)
        songAdapter = SongAdapter().apply {
            tracker = createTracker(song_list_view, this)
        }
        val observer = Observer<List<Song>> { list -> songAdapter.submitList(list) }
        songViewModel.songList.observe(this, observer)
        song_list_view.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
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

    private fun createTracker(view: RecyclerView, adapter: SongAdapter)
            : SelectionTracker<String>
    {
        view.adapter = adapter // Required, otherwise throws
        return SelectionTracker
            .Builder<String>(
                "SongSelection",
                view,
                SongItemKeyProvider(adapter),
                SongItemDetailsLookup(view),
                StorageStrategy.createStringStorage())
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()
    }


    private val contract = object : ActivityResultContracts.OpenMultipleDocuments() {
        override fun createIntent(context: Context, input: Array<out String>): Intent {
            return super.createIntent(context, input).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        }
    }
    private val addSongsLauncher = registerForActivityResult(contract) { uris: List<Uri> ->
        val context = this.applicationContext
        val register = SongRegister(contentResolver, AppDatabase.getInstance(context))
        Thread {
            uris.forEach { uri -> register.add(uri) }
        }.start()
    }
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var songAdapter: SongAdapter
    private val songViewModel: SongViewModel by viewModels()
}

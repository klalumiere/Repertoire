package klalumiere.repertoire

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import klalumiere.repertoire.databinding.ActivityMainBinding
import klalumiere.repertoire.databinding.ContentMainBinding

open class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        addSongsLauncher = createAddSongsLauncher()
        binding.addSongsFAB.setOnClickListener { addSongsLauncher.launch(arrayOf("text/*")) }

        linearLayoutManager = LinearLayoutManager(this)
        songAdapter = SongAdapter().apply {
            tracker = createTracker(binding.contentMain.songListView, this)
        }
        val observer = Observer<List<Song>> { list -> songAdapter.submitList(list) }
        songViewModel.songList.observe(this, observer)
        binding.contentMain.songListView.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = songAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        deleteAction = menu.findItem(R.id.action_delete)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> onDeleteOptionItemSelected()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        songAdapter.tracker?.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        songAdapter.tracker?.onSaveInstanceState(outState)
    }

    private fun createAddSongsLauncher(): ActivityResultLauncher<Array<String>> {
        val registry = AddSongsActivityResultRegistryFactory.create()
        return if (registry == null) {
            registerForActivityResult(contract) { uris: List<Uri> ->
                songViewModel.add(uris)
            }
        } else {
            registerForActivityResult(contract, registry) { uris: List<Uri> ->
                songViewModel.add(uris)
            }
        }
    }

    private fun createSelectionObserver(tracker: SelectionTracker<String>)
            : SelectionTracker.SelectionObserver<String>
    {
        return object : SelectionTracker.SelectionObserver<String>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                deleteAction.isEnabled = tracker.selection.size() > 0
            }
        }
    }

    private fun createTracker(view: RecyclerView, adapter: SongAdapter)
            : SelectionTracker<String>
    {
        view.adapter = adapter // Required, otherwise throws
        val tracker = SelectionTracker
            .Builder(
                "SongSelection",
                view,
                SongItemKeyProvider(adapter),
                SongItemDetailsLookup(view),
                StorageStrategy.createStringStorage())
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()
        tracker.addObserver(createSelectionObserver(tracker))
        return tracker
    }

    private fun onDeleteOptionItemSelected(): Boolean {
        val uris = songAdapter.tracker!!.selection.map { Uri.parse(it) }
        songAdapter.tracker?.clearSelection() // Important, otherwise, added songs might be selected
        songViewModel.remove(uris)
        return true
    }

    private lateinit var binding: ActivityMainBinding
    private val contract = object : ActivityResultContracts.OpenMultipleDocuments() {
        override fun createIntent(context: Context, input: Array<String>): Intent {
            return super.createIntent(context, input).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        }
    }
    private lateinit var addSongsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var deleteAction: MenuItem
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var songAdapter: SongAdapter
    private val songViewModel: SongViewModel by viewModels()
}

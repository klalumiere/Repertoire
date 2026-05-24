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
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import klalumiere.repertoire.databinding.ActivityMainBinding
import klalumiere.repertoire.databinding.ContentMainBinding

private val TRANSPOSE_ITEM_IDS: Map<Int, Int> = mapOf(
    R.id.transpose_0 to 0,
    R.id.transpose_1 to 1,
    R.id.transpose_2 to 2,
    R.id.transpose_3 to 3,
    R.id.transpose_4 to 4,
    R.id.transpose_5 to 5,
    R.id.transpose_6 to 6,
    R.id.transpose_7 to 7,
    R.id.transpose_8 to 8,
    R.id.transpose_9 to 9,
    R.id.transpose_10 to 10,
    R.id.transpose_11 to 11
)

open class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }
        setSupportActionBar(binding.toolbar)

        addSongsLauncher = createAddSongsLauncher()
        binding.addSongsFAB.setOnClickListener { addSongsLauncher.launch(arrayOf("text/*")) }

        linearLayoutManager = LinearLayoutManager(this)
        songAdapter = SongAdapter()
        songCountAdapter = SongCountAdapter()
        binding.contentMain.songListView.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = ConcatAdapter(songAdapter, songCountAdapter)
        }
        songAdapter.tracker = createTracker(binding.contentMain.songListView, songAdapter)
        val observer = Observer<List<Song>> { list ->
            songAdapter.submitList(list)
            songCountAdapter.setCount(list.size)
        }
        songViewModel.songList.observe(this, observer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        deleteAction = menu.findItem(R.id.action_delete)
        configureSearchAction(menu.findItem(R.id.action_search))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        TRANSPOSE_ITEM_IDS[item.itemId]?.let { semitones ->
            TranspositionPreference.set(this, semitones)
            invalidateOptionsMenu()
            return true
        }
        return when (item.itemId) {
            R.id.action_delete -> onDeleteOptionItemSelected()
            R.id.action_random -> onRandomOptionItemSelected()
            R.id.action_select_all -> onSelectAllOptionItemSelected()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val action = menu.findItem(R.id.action_select_all)
        val total = songAdapter.currentList.size
        val selected = songAdapter.tracker?.selection?.size() ?: 0
        val allSelected = total > 0 && selected == total
        action.setTitle(if (allSelected) R.string.action_deselect_all else R.string.action_select_all)
        action.isEnabled = total > 0

        val current = TranspositionPreference.get(this)
        val transposeItem = menu.findItem(R.id.action_transpose)
        transposeItem.title = getString(R.string.action_transpose_with_value, current)
        TRANSPOSE_ITEM_IDS.forEach { (id, semitones) ->
            menu.findItem(id)?.isChecked = (semitones == current)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        songAdapter.tracker?.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        songAdapter.tracker?.onSaveInstanceState(outState)
    }

    private fun configureSearchAction(searchItem: MenuItem) {
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.action_search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                songViewModel.setQuery(newText ?: "")
                return true
            }
        })
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean = true
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                songViewModel.setQuery("")
                return true
            }
        })
        searchView.clearFocus()
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

    private fun onSelectAllOptionItemSelected(): Boolean {
        val tracker = songAdapter.tracker ?: return true
        val keys = songAdapter.currentList.map { it.uri }
        if (keys.isNotEmpty() && tracker.selection.size() == keys.size) {
            tracker.clearSelection()
        } else {
            tracker.setItemsSelected(keys, true)
        }
        return true
    }

    private fun onRandomOptionItemSelected(): Boolean {
        val song = songAdapter.currentList.randomOrNull() ?: return true
        val intent = Intent(this, SongActivity::class.java).apply {
            putExtra(SongActivity.SONG_NAME, song.name)
            putExtra(SongActivity.SONG_URI_AS_STRING, song.uri)
        }
        startActivity(intent)
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
    private lateinit var songCountAdapter: SongCountAdapter
    private val songViewModel: SongViewModel by viewModels()
}

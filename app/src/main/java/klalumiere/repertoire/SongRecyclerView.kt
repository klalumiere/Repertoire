package klalumiere.repertoire

import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SongViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    companion object {
        fun create(parent: ViewGroup): SongViewHolder {
            val resourceId = android.R.layout.simple_list_item_activated_1
            val itemLayout = LayoutInflater
                .from(parent.context)
                .inflate(resourceId, parent, false)
            return SongViewHolder(itemLayout)
        }
    }

    fun bind(songRhs: Song) {
        song = songRhs
        nameView.text = song!!.name
        view.setOnClickListener { onClick() }
    }
    fun setActivated(isActivated: Boolean) {
        view.isActivated = isActivated
    }

    fun getContentUri(): String {
        return song!!.uri
    }
    fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> {
        return object : ItemDetailsLookup.ItemDetails<String>() {
            override fun getPosition(): Int = bindingAdapterPosition
            override fun getSelectionKey(): String? = song?.uri
        }
    }
    fun getTextViewed(): CharSequence {
        return nameView.text
    }
    fun isViewActivated(): Boolean {
        return view.isActivated
    }


    private fun onClick() {
        val context = view.context
        val intent = Intent(context, SongActivity::class.java).apply {
            putExtra(SongActivity.SONG_NAME, song!!.name)
            putExtra(SongActivity.SONG_URI_AS_STRING, song!!.uri)
        }
        context.startActivity(intent)
    }

    private var song: Song? = null
    private val nameView = view.findViewById(android.R.id.text1) as TextView
}

class SongCountViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    companion object {
        fun create(parent: ViewGroup): SongCountViewHolder {
            val itemLayout = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.view_song_count, parent, false)
            return SongCountViewHolder(itemLayout)
        }
    }

    fun bind(count: Int) {
        countView.text = view.resources.getQuantityString(R.plurals.song_count, count, count)
    }

    fun getCountText(): CharSequence {
        return countView.text
    }

    private val countView = view.findViewById<TextView>(R.id.song_count_view)
}

class SongItemCallback : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(old: Song, new: Song): Boolean  {
        return old.uri == new.uri
    }
    override fun areContentsTheSame(old: Song, new: Song): Boolean {
        return old == new
    }
}

class SongAdapter : ListAdapter<Song, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
    companion object {
        val DIFF_CALLBACK = SongItemCallback()
        const val VIEW_TYPE_SONG = 0
        const val VIEW_TYPE_FOOTER = 1
    }

    override fun getItemCount(): Int {
        val songCount = currentList.size
        return if (songCount == 0) 0 else songCount + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == currentList.size) VIEW_TYPE_FOOTER else VIEW_TYPE_SONG
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SongViewHolder -> {
                val song = getItem(position)
                holder.bind(song)
                tracker?.let {
                    holder.setActivated(it.isSelected(song.uri))
                }
            }
            is SongCountViewHolder -> holder.bind(currentList.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_FOOTER -> SongCountViewHolder.create(parent)
            else -> SongViewHolder.create(parent)
        }
    }


    var tracker: SelectionTracker<String>? = null
}

class SongItemKeyProvider(private val adapter: SongAdapter)
    : ItemKeyProvider<String>(SCOPE_CACHED)
{
    override fun getKey(position: Int): String? {
        return adapter.currentList.getOrNull(position)?.uri
    }
    override fun getPosition(key: String): Int {
        return adapter.currentList.indexOfFirst { it.uri == key }
    }
}

class SongItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>()
{
    override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y) ?: return null
        val holder = recyclerView.getChildViewHolder(view)
        return (holder as? SongViewHolder)?.getItemDetails()
    }
}

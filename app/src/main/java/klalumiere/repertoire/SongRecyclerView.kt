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

class SongItemCallback : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(old: Song, new: Song): Boolean  {
        return old.uri == new.uri
    }
    override fun areContentsTheSame(old: Song, new: Song): Boolean {
        return old == new
    }
}

class SongAdapter : ListAdapter<Song, SongViewHolder>(DIFF_CALLBACK) {
    companion object {
        val DIFF_CALLBACK = SongItemCallback()
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song)
        tracker?.let {
            holder.setActivated(it.isSelected(song.uri))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder.create(parent)
    }


    var tracker: SelectionTracker<String>? = null
}

class SongItemKeyProvider(private val adapter: SongAdapter)
    : ItemKeyProvider<String>(SCOPE_CACHED)
{
    override fun getKey(position: Int): String {
        return adapter.currentList[position].uri
    }
    override fun getPosition(key: String): Int {
        return adapter.currentList.indexOfFirst { it.uri == key }
    }
}

class SongItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>()
{
    override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y) ?: return null
        return (recyclerView.getChildViewHolder(view) as SongViewHolder).getItemDetails()
    }
}

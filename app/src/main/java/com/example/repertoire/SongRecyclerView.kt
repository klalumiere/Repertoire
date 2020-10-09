package com.example.repertoire

import android.app.Application
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SongViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    companion object {
        fun create(parent: ViewGroup): SongViewHolder {
            val resourceId = android.R.layout.simple_list_item_1
            val itemLayout = LayoutInflater
                .from(parent.context)
                .inflate(resourceId, parent, false)
            return SongViewHolder(itemLayout)
        }
    }

    fun bind(songRhs: Song) {
        song = songRhs
        nameView.text = song.name
        view.setOnClickListener() { Log.i("Click", song.uri ) }
    }
    fun getContentUri(): String {
        return song.uri
    }
    fun getTextViewed(): CharSequence {
        return nameView.text
    }

    private lateinit var song: Song
    private val nameView = view.findViewById(android.R.id.text1) as TextView
}

class SongItemCallback() : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(old: Song, new: Song): Boolean  {
        return old.uri == new.uri;
    }
    override fun areContentsTheSame(old: Song, new: Song): Boolean {
        return old == new;
    }
}

class SongAdapter : ListAdapter<Song, SongViewHolder>(SongAdapter.DIFF_CALLBACK) {
    companion object {
        val DIFF_CALLBACK = SongItemCallback()
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder.create(parent)
    }
}

class SongViewModel(application: Application) : AndroidViewModel(application) {
    private val songDao = AppDatabase.getInstance(application).songDao()
    val songList = songDao.getAllLive()
}

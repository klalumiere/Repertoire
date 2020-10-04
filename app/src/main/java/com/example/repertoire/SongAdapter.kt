package com.example.repertoire

import android.app.Application
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SongViewModel(application: Application) : AndroidViewModel(application) {
    private val songDao = AppDatabase.getInstance(application).songDao()
    val songList = songDao.getAllLive()
}

class SongViewHolder(val view: TextView) : RecyclerView.ViewHolder(view)

class SongAdapter : ListAdapter<Song, SongViewHolder>(SongAdapter.DIFF_CALLBACK) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Song>() {
            override fun areItemsTheSame(old: Song, new: Song): Boolean  {
                return old.uri == new.uri;
            }
            override fun areContentsTheSame(old: Song, new: Song): Boolean {
                return old == new;
            }
        }
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.view.text = getItem(position).name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(TextView(parent.context))
    }
}

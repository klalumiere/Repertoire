package com.example.repertoire

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class SongViewModel(application: Application) : AndroidViewModel(application) {
    private val songDao = AppDatabase.getInstance(application).songDao()
    val songList = songDao.getAllLive()
}

class SongViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    private val view = v

    companion object {
        const val resourceId = android.R.layout.simple_list_item_1
    }
    val nameView = view.findViewById(android.R.id.text1) as TextView
}

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
        holder.nameView.text = File(getItem(position).name).nameWithoutExtension
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val itemLayout = LayoutInflater
            .from(parent.context)
            .inflate(SongViewHolder.resourceId, parent, false)
        return SongViewHolder(itemLayout)
    }
}

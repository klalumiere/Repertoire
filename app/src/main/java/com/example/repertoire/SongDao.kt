package com.example.repertoire

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SongDao {
    @Query("SELECT * FROM song ORDER BY name")
    fun getAll(): List<Song>

    @Insert
    fun insert(song: Song)

    @Delete
    fun delete(song: Song)
}

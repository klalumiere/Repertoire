package com.example.repertoire

import androidx.room.*

@Dao
interface SongDao {
    @Query("SELECT * FROM song ORDER BY name")
    fun getAll(): List<Song>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(song: Song)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg songs: Song)

    @Delete
    fun delete(song: Song)
}

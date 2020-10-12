package com.example.repertoire

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SongDao {
    @Query("SELECT * FROM song ORDER BY name")
    fun getAll(): List<Song>
    @Query("SELECT * FROM song ORDER BY name")
    fun getAllLive(): LiveData<List<Song>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(song: Song)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg songs: Song)

    @Delete
    fun delete(song: Song)

    @Query("DELETE FROM song WHERE uri = :uri")
    fun delete(uri: String)
}

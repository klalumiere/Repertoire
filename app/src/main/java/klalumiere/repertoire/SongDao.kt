package klalumiere.repertoire

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SongDao {
    @Delete
    suspend fun delete(song: Song)

    @Query("DELETE FROM song WHERE uri = :uri")
    suspend fun delete(uri: String)

    @Query("SELECT * FROM song WHERE uri = :uri LIMIT 1")
    suspend fun get(uri: String): Song?

    @Query("SELECT * FROM song ORDER BY name")
    fun getAll(): LiveData<List<Song>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(song: Song)


    // Introduced for tests
    @Query("DELETE FROM song")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg songs: Song)
}

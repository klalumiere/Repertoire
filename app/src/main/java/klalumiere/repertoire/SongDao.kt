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

    @Query("SELECT * FROM song WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Song?

    @Query("SELECT * FROM song ORDER BY name")
    fun getAll(): LiveData<List<Song>>

    @Query("""
        SELECT * FROM song
        WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(content) LIKE '%' || LOWER(:query) || '%'
        ORDER BY
            CASE WHEN LOWER(name) LIKE '%' || LOWER(:query) || '%' THEN 0 ELSE 1 END,
            name
    """)
    fun getMatching(query: String): LiveData<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: Song)


    // Introduced for tests
    @Query("DELETE FROM song")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg songs: Song)
}

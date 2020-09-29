package com.example.repertoire

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Song::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: createDatabase(context).also { instance = it }
            }
        }

        fun createInMemoryDatabase(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        }


        private fun createDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context,AppDatabase::class.java,
                context.getString(R.string.repertoire_db)
            ).build()
        }

        @Volatile private var instance: AppDatabase? = null
    }
}

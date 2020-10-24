package com.example.repertoire

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Song::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: createDatabaseBuilder(context).build().also { instance = it }
            }
        }


        fun createInMemoryDatabaseBuilderForTests(context: Context): Builder<AppDatabase> {
            return Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java)
        }

        fun getInstanceAllowingMainThreadQueriesForTests(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: createDatabaseBuilder(context)
                    .allowMainThreadQueries()
                    .build()
                    .also { instance = it }
            }
        }


        private fun createDatabaseBuilder(context: Context): Builder<AppDatabase> {
            return Room.databaseBuilder(context,AppDatabase::class.java,
                context.getString(R.string.repertoire_db))
        }

        @Volatile private var instance: AppDatabase? = null
    }
}

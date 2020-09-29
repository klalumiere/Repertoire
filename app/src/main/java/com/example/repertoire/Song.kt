package com.example.repertoire

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["name"])])
data class Song(
    @PrimaryKey val rowid: Int,
    val uri: String,
    val name: String
)

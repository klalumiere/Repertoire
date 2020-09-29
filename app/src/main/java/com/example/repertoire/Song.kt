package com.example.repertoire

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    primaryKeys = ["uri"],
    indices = [Index(value = ["name"])]
)
data class Song(
    val uri: String,
    val name: String
)

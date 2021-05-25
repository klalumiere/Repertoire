package klalumiere.repertoire

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = ["uri"],
    indices = [Index(value = ["name"])]
)
data class Song(
    val uri: String,
    val name: String,
    val content: String?
)

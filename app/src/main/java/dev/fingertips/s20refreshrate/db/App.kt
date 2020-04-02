package dev.fingertips.s20refreshrate.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class App(
    @PrimaryKey val packageName: String,
    var mode: Mode
)
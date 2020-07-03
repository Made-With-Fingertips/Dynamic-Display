package dev.fingertips.s20refreshrate.db

import androidx.room.TypeConverter

class DbConverters {

    @TypeConverter
    fun intFromMode(value: Mode): Int {
        return when (value) {
            Mode.SIXTY -> 1
            Mode.NINETY_SIX -> 3
            Mode.ONE_TWENTY -> 2
            else -> 0
        }
    }

    @TypeConverter
    fun modeFromInt(value: Int): Mode {
        return when (value) {
            1 -> Mode.SIXTY
            2 -> Mode.ONE_TWENTY
            3 -> Mode.NINETY_SIX
            else -> Mode.DEFAULT
        }
    }
}
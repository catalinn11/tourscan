package com.example.tourscan.utils

import androidx.room.TypeConverter
import java.util.UUID

class Converters {
    @TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @TypeConverter
    fun toUUID(string: String): UUID {
        return UUID.fromString(string)
    }
}
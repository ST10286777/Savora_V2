package vcmsa.projects.budgettracker.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/*********
Title: <Room TypeConverter for map>
Author: <Jakub Kostka>
Date: <2023>
Code version <3.1.0>
Availability:<https://stackoverflow.com/questions/51912855/room-typeconverter-for-map>
 **********/
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromMap(value: Map<String, Double>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toMap(value: String): Map<String, Double> {
        val mapType = object : TypeToken<Map<String, Double>>() {}.type
        return gson.fromJson(value, mapType)
    }
}

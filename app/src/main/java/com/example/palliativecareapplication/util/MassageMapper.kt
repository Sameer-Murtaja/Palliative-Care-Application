package com.example.palliativecareapplication.util

import com.example.palliativecareapplication.model.Message
import com.google.gson.Gson

class MassageMapper {
    fun map(jsonString: String): Array<Message> {
        val gson = Gson()
        return gson.fromJson(jsonString, Array<Message>::class.java)
    }
}
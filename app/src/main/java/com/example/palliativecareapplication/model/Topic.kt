package com.example.palliativecareapplication.model

import java.util.Calendar
import java.util.Date

data class Topic(
    var id: String,
    var title: String,
    var description: String,
    var doctorName: String,
    var image: String,
    var usersFollowing: Int,
    val date: Long
)
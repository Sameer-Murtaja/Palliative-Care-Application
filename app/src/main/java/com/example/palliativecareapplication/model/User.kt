package com.example.palliativecareapplication.model

data class User(val id:String, val name:String, var isDoctor:Boolean, var topicsFollowed:ArrayList<String>)
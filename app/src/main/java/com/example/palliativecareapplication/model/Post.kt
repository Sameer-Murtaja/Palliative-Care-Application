package com.example.palliativecareapplication.model

data class Post(var id:String, var title: String, var details: String, var  date: Long, var attachments: List<Attachment>?)
package com.example.palliativecareapplication.model

import android.net.Uri

data class Attachment(
    val uri: Uri,
    val name: String,
    val type: String
){
    companion object{
        fun fromHashmap(hashMap: HashMap<String, *>): Attachment {
            return Attachment(
                Uri.parse(hashMap["uri"] as String?) as Uri,
                hashMap["name"] as String,
                hashMap["type"] as String
            )
        }
    }

    fun isImage(): Boolean {
        return type.startsWith("image")
    }

    fun isVideo(): Boolean {
        return type.startsWith("video")
    }


}
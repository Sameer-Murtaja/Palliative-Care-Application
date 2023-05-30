package com.example.palliativecareapplication.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.palliativecareapplication.R
import com.example.palliativecareapplication.model.User
import com.google.common.reflect.TypeToken
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    companion object {

        lateinit var user: User

        fun swipeFragment(activity: FragmentActivity, fragment: Fragment) {
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment).commit()
        }

//        fun addFragment(activity: FragmentActivity, fragment: Fragment) {
//            activity.supportFragmentManager.beginTransaction()
//                .add(R.id.fragmentContainerView, fragment).commit()
//        }
//
//        fun removeFragment(activity: FragmentActivity, fragment: Fragment) {
//            activity.supportFragmentManager.beginTransaction()
//                .remove(fragment).commit()
//        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getDataFromSP()

        FirebaseApp.initializeApp(this)
    }

    private fun getDataFromSP() {
        val sharedPreferences = this.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val savedJsonString = sharedPreferences.getString("user_data", null)
        if (savedJsonString != null && savedJsonString.isNotEmpty()) {
            val jsonObject = JSONObject(savedJsonString)

            val isDoctor: Boolean = jsonObject.getBoolean("isDoctor")
            val jsonString = jsonObject.getString("topicsFollowed")
            val gson = Gson()
            val topicsFollowed: ArrayList<String> =
                gson.fromJson(jsonString, object : TypeToken<ArrayList<String>>() {}.type)

            user = User(
                jsonObject.getString("id"),
                jsonObject.getString("name"),
                isDoctor,
                topicsFollowed,
            )
        }
        if (savedJsonString != null) {
            // Use the savedObject here
            swipeFragment(this, ViewTopicsFragment())

        } else {
            // Object not found in SharedPreferences
            swipeFragment(this, LoginFragment())
        }

    }
}
package com.example.palliativecareapplication.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.palliativecareapplication.R

class MainActivity : AppCompatActivity() {

    companion object {
        var isPatient = false

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
        swipeFragment(this, ViewTopicsFragment())
    }
}
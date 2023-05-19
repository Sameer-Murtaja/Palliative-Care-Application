package com.example.palliativecareapplication.util

import androidx.fragment.app.Fragment
import com.example.palliativecareapplication.R

private fun Fragment.navigateWithReplaceFragment(fragment: Fragment) {
    requireActivity().supportFragmentManager.beginTransaction().apply {
        replace(R.id.fragmentContainerView, fragment)
        commit()
    }
}

private fun Fragment.navigateWithAddFragment(fragment: Fragment) {
    requireActivity().supportFragmentManager.beginTransaction().apply {
        add(R.id.fragmentContainerView, fragment)
        addToBackStack(fragment::class.java.name)
        commit()
    }
}
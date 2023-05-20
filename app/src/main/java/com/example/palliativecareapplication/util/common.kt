package com.example.palliativecareapplication.util

import androidx.fragment.app.Fragment
import com.example.palliativecareapplication.R

 fun Fragment.navigateWithReplaceFragment(fragment: Fragment) {
    requireActivity().supportFragmentManager.beginTransaction().apply {
        replace(R.id.fragmentContainerView, fragment)
        commit()
    }
}

 fun Fragment.navigateWithAddFragment(fragment: Fragment) {
    requireActivity().supportFragmentManager.beginTransaction().apply {
        add(R.id.fragmentContainerView, fragment)
        addToBackStack(fragment::class.java.name)
        commit()
    }
}
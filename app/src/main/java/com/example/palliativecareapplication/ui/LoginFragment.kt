package com.example.palliativecareapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.example.palliativecareapplication.R
import com.example.palliativecareapplication.databinding.FragmentLoginBinding
import com.example.palliativecareapplication.util.navigateWithAddFragment
import com.example.palliativecareapplication.util.navigateWithReplaceFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment: Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var username: String
    private lateinit var password: String
    companion object{
        var isADoctor = false
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = Firebase.auth

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addCallbacks()
    }

    private fun addCallbacks() {
        onLoginButtonPressed()
        onLoginAsADoctorButtonPressed()
        onSignupTextPressed()
        onFormTextChange()
    }



    private fun onLoginButtonPressed() {
        binding.buttonLogin.setOnClickListener {
            showLoading()
            isADoctor = false
            assignUsernameAndPassword()
            if (checkFormsNotEmpty()) {
                authLogin(email = username, password = password)
            } else {
                hideLoading()
                alertEmptyForm()
            }
        }
    }

    private fun onLoginAsADoctorButtonPressed() {
        binding.buttonLoginAsADoctor.setOnClickListener {
            showLoading()
            isADoctor = true
            assignUsernameAndPassword()
            if (checkFormsNotEmpty()) {
                hideLoading()
                authLogin(email = username, password = password)
            } else {
                hideLoading()
                alertEmptyForm()
            }
        }
    }

    private fun assignUsernameAndPassword() {
        username = binding.textInputEditUsername.text.toString().trim()
        password = binding.textInputEditPassword.text.toString().trim()
    }


    private fun checkFormsNotEmpty() = username.isNotBlank() && password.isNotBlank()

    private fun authLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    hideLoading()
                    val user = auth.currentUser
                    onLoginSuccess()
                } else {
                    onLoginFail()
                }
            }

    }

    private fun onLoginFail() {
        hideLoading()
            Snackbar.make(
                binding.loginScreen,
                getString(R.string.invalid_username_password),
                3000
            ).show()
    }

    private fun alertEmptyForm() {
        if (username.isBlank()) {
            binding.textInputLayoutUsername.helperText = getString(R.string.empty_form)
        }
        if (password.isBlank()) {
            binding.textInputLayoutPassword.helperText = getString(R.string.empty_form)
        }
    }


    private fun onSignupTextPressed() {
        binding.textViewSignUp.setOnClickListener {
            this.navigateWithAddFragment(RegisterFragment())
        }
    }

    private fun onFormTextChange() {
        binding.textInputEditUsername.doOnTextChanged { _, _, _, _ ->
            binding.textInputLayoutUsername.helperText = null
        }
        binding.textInputEditPassword.doOnTextChanged { _, _, _, _ ->
            binding.textInputLayoutPassword.helperText = null
        }
    }

    private fun onLoginSuccess() {
        this.navigateWithReplaceFragment(ViewTopicsFragment())
    }



    private fun showLoading() {
        binding.progressLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressLoading.visibility = View.INVISIBLE
    }
}
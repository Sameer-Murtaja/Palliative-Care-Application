package com.example.palliativecareapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.palliativecareapplication.R
import com.example.palliativecareapplication.databinding.FragmentRegisterBinding
import com.example.palliativecareapplication.model.FirebaseNames
import com.example.palliativecareapplication.model.User
import com.example.palliativecareapplication.util.navigateWithAddFragment
import com.example.palliativecareapplication.util.navigateWithReplaceFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterFragment: Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    private lateinit var username: String
    private lateinit var password: String
    private lateinit var confirmPassword: String
    private lateinit var fullName: String
    private lateinit var address: String
    private lateinit var phone: String
    var isADoctor = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        db = Firebase.firestore

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addCallbacks()
    }



    private fun addCallbacks() {
        onSignupButtonPressed()
        onsSignupAsADoctorButtonPressed()
        onSigninTextPressed()
    }



    private fun onSignupButtonPressed() {
        binding.buttonSignUp.setOnClickListener {
            showLoading()
            isADoctor = false
            assignUserInfo()
            if (checkFormsNotEmpty()) {
                authSignUp(email = username, password = password)
            } else {
                alertEmptyForm()
                hideLoading()
            }
        }
    }

    private fun onsSignupAsADoctorButtonPressed() {
        binding.buttonRegisterAsADoctor.setOnClickListener {
            showLoading()
            isADoctor = true
            assignUserInfo()
            if (checkFormsNotEmpty()) {
                authSignUp(email = username, password = password)
            } else {
                alertEmptyForm()
                hideLoading()
            }
        }
    }

    private fun assignUserInfo() {
        username = binding.textInputUsername.text.toString().trim()
        password = binding.textInputPassword.text.toString().trim()
        confirmPassword = binding.textInputConfirmPassword.text.toString().trim()
        fullName = binding.textInputFullName.text.toString().trim()
        address = binding.textInputAddress.text.toString().trim()
        phone = binding.textInputPhone.text.toString().trim()
    }


    private fun authSignUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    hideLoading()
                    val user = auth.currentUser
                    onSignupSuccess()
                } else {
                    onLoginFail()
                    hideLoading()
                }
            }

    }

    private fun onLoginFail() {
        Snackbar.make(
            binding.registerScreen,
            getString(R.string.invalid_username_password),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun alertEmptyForm() {
        if(checkFormsNotEmpty()){
            Snackbar.make(
                binding.registerScreen,
                getString(R.string.invalid_info),
                Snackbar.LENGTH_LONG
            ).show()
        }

        if(!chickThePasswordAndTheConfirmIsSame()){
            Snackbar.make(
                binding.registerScreen,
                getString(R.string.invalid_confirm_password),
                Snackbar.LENGTH_LONG
            ).show()
        }

    }

    private fun chickThePasswordAndTheConfirmIsSame() =
        binding.textInputPassword == binding.textInputConfirmPassword

  private fun checkFormsNotEmpty(): Boolean{
      return username.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
              && fullName.isNotBlank() && address.isNotBlank() && phone.isNotBlank()
  }


    private fun onSigninTextPressed() {
        binding.textViewLogin.setOnClickListener {
            this.navigateWithAddFragment(LoginFragment())
        }
    }

    private fun onSignupSuccess() {
        addUserToFirebase(isADoctor)
    }


    private fun addUserToFirebase(isDoctor: Boolean){

        val id = auth.currentUser!!.uid
        Log.e("tag", "authSingUp: $id")

        var user = hashMapOf(
            "id" to id,
            "name" to username,
            "isDoctor" to isDoctor,
            "topicsFollowed" to ArrayList<String>()
        )
        MainActivity.user = User(id,username,isDoctor,ArrayList())

        db.collection(FirebaseNames.COLLECTION_USERS).document(id)
            .set(user)
            .addOnSuccessListener { documentReference ->
                Log.e("tag", "Added Successfully $id")
                this.navigateWithReplaceFragment(ViewTopicsFragment())
            }
            .addOnFailureListener {
                Log.e("tag", it.message.toString())
            }

    }



    private fun showLoading() {
        binding.progressLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressLoading.visibility = View.INVISIBLE
    }
}
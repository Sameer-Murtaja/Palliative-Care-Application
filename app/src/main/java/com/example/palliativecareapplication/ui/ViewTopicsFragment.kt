package com.example.palliativecareapplication.ui

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.palliativecareapplication.ui.adapter.TopicAdapter
import com.example.palliativecareapplication.databinding.FragmentViewTopicsBinding
import com.example.palliativecareapplication.model.FirebaseNames
import com.example.palliativecareapplication.model.Topic
import com.example.palliativecareapplication.util.navigateWithReplaceFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class ViewTopicsFragment : Fragment() {
    lateinit var db: FirebaseFirestore

    lateinit var binding: FragmentViewTopicsBinding
    private var progressDialog: ProgressDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewTopicsBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Firebase.firestore
    }


    override fun onResume() {
        super.onResume()
        saveUserDataINSharedPreferences()
        viewTopics()

        if (!MainActivity.user.isDoctor) {
            binding.btnAdd.visibility = View.GONE
        }

        binding.btnAdd.setOnClickListener {
            this.navigateWithReplaceFragment(AddTopicFragment())
        }

        binding.btnSendDoctor.setOnClickListener {
            this.navigateWithReplaceFragment(ChatFragment("Doctor"))
        }
        binding.btnSendAllGroup.setOnClickListener {
            this.navigateWithReplaceFragment(ChatFragment("Patient"))
        }

        binding.textInputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                (binding.rvTopics.adapter as TopicAdapter).search(s.toString())
            }

        })

    }

    private  fun saveUserDataINSharedPreferences(){
        val sharedPreferences = requireContext().getSharedPreferences(
            "my_preferences",
            Context.MODE_PRIVATE
        )

        val gson = Gson()
        val jsonString = gson.toJson(MainActivity.user)

        val editor = sharedPreferences.edit()
        editor.putString("user_data", jsonString)
        editor.apply()
    }

    private fun viewTopics() {
        showDialog("Loading topics");
        val topicsArr = ArrayList<Topic>()
        db.collection(FirebaseNames.COLLECTION_TOPICS)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val id = document.id
                    val title = document.getString("title")!!
                    val description = document.getString("description")!!
                    val doctorName = document.getString("doctorName")!!
                    val image = document.getString("image")!!
                    val usersFollowing = document.getDouble("usersFollowing")!!.toInt()
                    val currentDate = document.getLong("date")!!
                    topicsArr.add(
                        Topic(
                            id,
                            title,
                            description,
                            doctorName,
                            image,
                            usersFollowing,
                            currentDate
                        )
                    )
                }

                val topicsAdapter = TopicAdapter(topicsArr)
                binding.rvTopics.layoutManager = LinearLayoutManager(requireContext())
                binding.rvTopics.adapter = topicsAdapter
                hideDialog()
            }.addOnFailureListener { error ->
                Log.e("hzm", error.message.toString())
                hideDialog()
                Toast.makeText(requireContext(), "Error while retrieving data", Toast.LENGTH_SHORT)
                    .show()
            }
    }


    private fun showDialog(msg: String) {
        progressDialog = ProgressDialog(requireContext())
        progressDialog!!.setMessage(msg)
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    }


    private fun hideDialog() {
        if (progressDialog!!.isShowing)
            progressDialog!!.dismiss()
    }



}

fun main() {
    println(System.currentTimeMillis())
    Thread.sleep(2000)
    println(System.currentTimeMillis())

}

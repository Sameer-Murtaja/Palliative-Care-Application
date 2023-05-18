package com.example.palliativecareapplication

import android.app.ProgressDialog
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
import com.example.palliativecareapplication.adapter.TopicAdapter
import com.example.palliativecareapplication.databinding.FragmentMainScreenBinding
import com.example.palliativecareapplication.model.FirebaseNames
import com.example.palliativecareapplication.model.Topic
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainScreenFragment : Fragment() {
    lateinit var db: FirebaseFirestore

    lateinit var binding: FragmentMainScreenBinding
    private var progressDialog: ProgressDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainScreenBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Firebase.firestore
    }


    override fun onResume() {
        super.onResume()
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
                    topicsArr.add(
                        Topic(
                            id,
                            title,
                            description,
                            doctorName,
                            image,
                            usersFollowing
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
                Toast.makeText(requireContext(), "Error while retrieving data", Toast.LENGTH_SHORT).show()
            }



        binding.btnAdd.setOnClickListener {
            MainActivity.swipeFragment(requireActivity(), AddTopicFragment())
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
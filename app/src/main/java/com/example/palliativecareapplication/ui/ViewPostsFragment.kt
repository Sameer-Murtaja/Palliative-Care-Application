package com.example.palliativecareapplication.ui

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
import com.example.palliativecareapplication.ui.adapter.PostAdapter
import com.example.palliativecareapplication.databinding.FragmentViewPostsBinding
import com.example.palliativecareapplication.model.Attachment
import com.example.palliativecareapplication.model.FirebaseNames
import com.example.palliativecareapplication.model.Post
import com.example.palliativecareapplication.model.Topic
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ViewPostsFragment(var topic: Topic) : Fragment() {
    lateinit var db: FirebaseFirestore

    lateinit var binding: FragmentViewPostsBinding
    private var progressDialog: ProgressDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewPostsBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Firebase.firestore
    }


    override fun onResume() {
        super.onResume()

        ViewPosts()

        if(MainActivity.isPatient){
            binding.btnAdd.visibility = View.GONE
        }

        binding.btnAdd.setOnClickListener {
            MainActivity.swipeFragment(requireActivity(), AddPostFragment(topic))
        }


        binding.textInputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                (binding.rvPosts.adapter as PostAdapter).search(s.toString())
            }

        })

        binding.fullscreenLayout.setOnClickListener {
            binding.fullscreenLayout.visibility = View.INVISIBLE
        }

        binding.appbar.setNavigationOnClickListener {
            MainActivity.swipeFragment(requireActivity(), TopicDetailsFragment(topic));
        }

    }

    private fun ViewPosts() {
        showDialog("Loading posts");
        val postsArr = ArrayList<Post>()
        db.collection(FirebaseNames.COLLECTION_TOPICS).document(topic.id)
            .collection(FirebaseNames.COLLECTION_POSTS).get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val id = document.id
                    val title = document.getString("title")!!
                    val details = document.getString("details")!!
                    val attachments =
                        (document.get("attachments") as ArrayList<HashMap<String, *>>).map {
                            Attachment.fromHashmap(it)
                        }
                    postsArr.add(
                        Post(
                            id,
                            title,
                            details,
                            attachments
                        )
                    )
                }
                val postsAdapter = PostAdapter(postsArr, topic, binding)
                binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
                binding.rvPosts.adapter = postsAdapter
                hideDialog()
            }.addOnFailureListener { error ->
                Log.e("tag", error.message.toString())
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
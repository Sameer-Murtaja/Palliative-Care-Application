package com.example.palliativecareapplication

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.palliativecareapplication.databinding.FragmentAddPostBinding
import com.example.palliativecareapplication.model.FirebaseNames
import com.example.palliativecareapplication.model.Topic
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class AddPostFragment(var topic: Topic) : Fragment() {

    lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentAddPostBinding
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPostBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Firebase.firestore


        val storage = Firebase.storage
        val storageRef = storage.reference
        val imageRef = storageRef.child("images")

        binding.buttonAddPost.setOnClickListener {
            val title = binding.textInputTitle.text.toString()
            val description = binding.textInputDescription.text.toString()

            if (title.isNotEmpty() && description.isNotEmpty()) {
                showDialog("Adding Post...")
                // Get the data from an ImageView as bytes
                //val data = getImageData()
                addPost(title, description)

            } else {
                Toast.makeText(requireContext(), "Please fill the data", Toast.LENGTH_SHORT).show()
            }
        }
    }//end onCreatedView


    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            //binding.imgPost.setImageURI(uri)
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

//
//    private fun getImageData(): ByteArray {
//        val bitmap = (binding.imgPost.drawable as BitmapDrawable).bitmap
//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
//        val data = baos.toByteArray()
//        return data
//    }


    private fun addPost(
        title: String, details: String
    ) {
        val post = hashMapOf(
            "title" to title,
            "details" to details,
        )
        db.collection(FirebaseNames.COLLECTION_TOPICS)
            .document(topic.id).collection(FirebaseNames.COLLECTION_POSTS)
            .add(post)
            .addOnSuccessListener {
                Log.e("TAG", "Added Successfully")
                Toast.makeText(requireContext(), "Added Successfully", Toast.LENGTH_SHORT).show()

                hideDialog()
                MainActivity.swipeFragment(requireActivity(), ViewPostsFragment(topic))
            }
            .addOnFailureListener {
                Log.e("TAG", it.message.toString())
            }
    }

}
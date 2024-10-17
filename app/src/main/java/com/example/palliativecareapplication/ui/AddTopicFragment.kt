package com.example.palliativecareapplication.ui

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.palliativecareapplication.databinding.FragmentAddTopicBinding
import com.example.palliativecareapplication.model.FirebaseNames
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime

class AddTopicFragment : Fragment() {

    lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentAddTopicBinding
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddTopicBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Firebase.firestore

        val storage = Firebase.storage
        val storageRef = storage.reference
        val imageRef = storageRef.child("images")

        binding.appbar.setNavigationOnClickListener {
            MainActivity.swipeFragment(requireActivity(), ViewTopicsFragment())
        }

        binding.imgTopic.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.buttonAddTopic.setOnClickListener {
            val title = binding.textInputTitle.text.toString()
            val description = binding.textInputDescription.text.toString()
            val doctorName = binding.textInputDoctorName.text.toString()

            if (title.isNotEmpty() && description.isNotEmpty() &&
                doctorName.isNotEmpty()
            ) {
                showDialog("Adding topic...")
                // Get the data from an ImageView as bytes
                val data = getImageData()
                uploadImageAndTopic(imageRef, data, title, description, doctorName)

            } else {
                Toast.makeText(requireContext(), "Please fill the data", Toast.LENGTH_SHORT).show()
            }
        }
    }//end onCreatedView


    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            binding.imgTopic.setImageURI(uri)
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


    private fun getImageData(): ByteArray {
        val bitmap = (binding.imgTopic.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()
        return data
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadImageAndTopic(
        imageRef: StorageReference,
        data: ByteArray,
        title: String,
        description: String,
        doctorName: String
    ) {
        val childRef = imageRef.child(System.currentTimeMillis().toString() + "_images.png")
        var uploadTask = childRef.putBytes(data)
        uploadTask.addOnFailureListener { exception ->
            Log.e("hzm", exception.message!!)
            hideDialog()
        }.addOnSuccessListener {
            Log.e("hzm", "Image Uploaded Successfully")
            childRef.downloadUrl.addOnSuccessListener { uri ->
                addTopic(
                    title,
                    description,
                    doctorName,
                    uri!!.toString(),
                )
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun addTopic(
        title: String, description: String, doctorName: String, image: String
    ) {
        val topic = hashMapOf(
            "title" to title,
            "description" to description,
            "doctorName" to doctorName,
            "image" to image,
            "usersFollowing" to 0,
            "date" to System.currentTimeMillis()
        )
        db.collection(FirebaseNames.COLLECTION_TOPICS)
            .add(topic)
            .addOnSuccessListener {
                Log.e("TAG", "Added Successfully")
                Toast.makeText(requireContext(), "Added Successfully", Toast.LENGTH_SHORT).show()

                hideDialog()
                MainActivity.swipeFragment(requireActivity(), ViewTopicsFragment())
            }
            .addOnFailureListener {
                Log.e("TAG", it.message.toString())
            }
    }

}
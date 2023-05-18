package com.example.palliativecareapplication

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.palliativecareapplication.databinding.FragmentTopicDetailsBinding
import com.example.palliativecareapplication.model.FirebaseNames
import com.example.palliativecareapplication.model.Topic
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.time.Duration

class TopicDetailsFragment(var topicData: Topic) : Fragment() {

    lateinit var db: FirebaseFirestore
    lateinit var binding: FragmentTopicDetailsBinding
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTopicDetailsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Firebase.firestore

        setTopicDataToUI()

        returnToMainOnAppBarButtonClick()

        selectImageOnClick()

        saveEditionOnButtonClick()

        binding.buttonViewPosts.setOnClickListener {
            MainActivity.addFragment(requireActivity(),ViewPostsFragment(topicData))
        }

    }


    private fun setTopicDataToUI() {
        Picasso.get().load(topicData.image).into(binding.imgTopic)
        binding.textInputTitle.setText(topicData.title)
        binding.textInputDescription.setText(topicData.description)
        binding.textInputDoctorName.setText(topicData.doctorName)
    }

    private fun returnToMainOnAppBarButtonClick() {
        binding.appbar.setNavigationOnClickListener {
            MainActivity.swipeFragment(requireActivity(), MainScreenFragment());
        }
    }

    private fun selectImageOnClick() {
        binding.imgTopic.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            binding.imgTopic.setImageURI(uri)
        }

    private fun saveEditionOnButtonClick() {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val imageRef = storageRef.child("images")

        binding.buttonSaveEdit.setOnClickListener {

            val id = topicData.id
            val title = binding.textInputTitle.text.toString()
            val description = binding.textInputDescription.text.toString()
            val doctorName = binding.textInputDoctorName.text.toString()

            if (title.isNotEmpty() && description.isNotEmpty() &&
                doctorName.isNotEmpty()
            ) {
                showDialog("saving...")
                // Get the data from an ImageView as bytes
                val data = getImageData()

                updateImageAndTopic(imageRef, data, id, title, description, doctorName)
            } else {
                Toast.makeText(requireContext(), "Please fill the data", Toast.LENGTH_SHORT).show()
            }

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


    private fun getImageData(): ByteArray {
        val bitmap = (binding.imgTopic.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()
        return data
    }


    private fun updateImageAndTopic(
        imageRef: StorageReference,
        data: ByteArray,
        id: String,
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
                updateTopic(
                    id,
                    title,
                    description,
                    doctorName,
                    uri!!.toString(),
                )
            }
        }
    }


    private fun updateTopic(
        id: String, title: String, description: String, doctorName: String, image: String
    ) {
        val topic = hashMapOf(
            "title" to title,
            "description" to description,
            "doctorName" to doctorName,
            "image" to image
        )

        db.collection(FirebaseNames.COLLECTION_TOPICS).document(id)
            .update(topic as Map<String, Any>)
            .addOnSuccessListener {
                Log.e("TAG", "Saved")
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                hideDialog()
                MainActivity.swipeFragment(requireActivity(), MainScreenFragment())
            }
            .addOnFailureListener {
                Log.e("TAG", it.message.toString())
            }
    }

}
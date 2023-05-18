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

class TopicDetailsFragment(var topicData: Topic) : Fragment() {

    lateinit var db: FirebaseFirestore
    lateinit var binding: FragmentTopicDetailsBinding
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTopicDetailsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Firebase.firestore

        setTopicDataToUI()

        returnToMainOnAppBarButtonClick()

        handlePatientUI()

        binding.btnEdit.setOnClickListener {
            binding.btnEdit.visibility = View.INVISIBLE
            binding.buttonViewPosts.visibility = View.INVISIBLE
            binding.buttonSaveEdit.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.VISIBLE

            binding.textInputTitle.isEnabled = true
            binding.textInputDescription.isEnabled = true
            binding.textInputDoctorName.isEnabled = true
            selectImageOnClick()
        }

        binding.buttonSaveEdit.setOnClickListener {
            binding.btnEdit.visibility = View.VISIBLE
            binding.buttonViewPosts.visibility = View.VISIBLE
            binding.buttonSaveEdit.visibility = View.INVISIBLE
            binding.btnDelete.visibility = View.INVISIBLE

            binding.textInputTitle.isEnabled = false
            binding.textInputDescription.isEnabled = false
            binding.textInputDoctorName.isEnabled = false
            binding.imgTopic.setOnClickListener(null)

            saveEditionOnButtonClick()
        }



        ViewTopicsOnClick()

    }

    private fun setTopicDataToUI() {
        Picasso.get().load(topicData.image).into(binding.imgTopic)
        binding.textInputTitle.setText(topicData.title)
        binding.textInputDescription.setText(topicData.description)
        binding.textInputDoctorName.setText(topicData.doctorName)
    }

    private fun returnToMainOnAppBarButtonClick() {
        binding.appbar.setNavigationOnClickListener {
            MainActivity.swipeFragment(requireActivity(), ViewTopicsFragment());
        }
    }

    private fun handlePatientUI() {
        if (MainActivity.isPatient) {
            //todo: if(is not following this topic)
            binding.btnEdit.visibility = View.GONE
            binding.buttonFollow.visibility = View.VISIBLE

            binding.buttonFollow.setOnClickListener {
                showDialog("جار الإضافة لقائمة المتابعة..")
                addFollow()
            }

            binding.buttonStopFollow.setOnClickListener {
                showDialog("جار الإزالة من قائمة المتابعة..")
                stopFollow()
            }
        }
    }

    private fun addFollow() {
        topicData.usersFollowing += 1
        val topic = hashMapOf(
            "title" to topicData.title,
            "description" to topicData.description,
            "doctorName" to topicData.doctorName,
            "image" to topicData.image,
            "usersFollowing" to topicData.usersFollowing
        )

        db.collection(FirebaseNames.COLLECTION_TOPICS).document(topicData.id)
            .update(topic as Map<String, Any>)
            .addOnSuccessListener {
                Log.e("TAG", "added Follow")
                Toast.makeText(requireContext(), "تمت إضافة الموضوع لقائمة المتابعة", Toast.LENGTH_SHORT).show()
                hideDialog()

                binding.buttonFollow.visibility = View.INVISIBLE
                binding.buttonStopFollow.visibility = View.VISIBLE

            }
            .addOnFailureListener {
                Log.e("TAG", it.message.toString())
                hideDialog()
            }
    }

    private fun stopFollow() {
        topicData.usersFollowing -= 1

        val topic = hashMapOf(
            "title" to topicData.title,
            "description" to topicData.description,
            "doctorName" to topicData.doctorName,
            "image" to topicData.image,
            "usersFollowing" to topicData.usersFollowing
        )

        db.collection(FirebaseNames.COLLECTION_TOPICS).document(topicData.id)
            .update(topic as Map<String, Any>)
            .addOnSuccessListener {
                Log.e("TAG", "removed Follow")
                Toast.makeText(requireContext(), "تمت إزالة متابعة الموضوع", Toast.LENGTH_SHORT).show()
                hideDialog()

                binding.buttonFollow.visibility = View.VISIBLE
                binding.buttonStopFollow.visibility = View.INVISIBLE
            }
            .addOnFailureListener {
                Log.e("TAG", it.message.toString())
                hideDialog()
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

    private fun ViewTopicsOnClick() {
        binding.buttonViewPosts.setOnClickListener {
            MainActivity.swipeFragment(requireActivity(), ViewPostsFragment(topicData))
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
            "image" to image,
            "usersFollowing" to topicData.usersFollowing
        )

        db.collection(FirebaseNames.COLLECTION_TOPICS).document(id)
            .update(topic as Map<String, Any>)
            .addOnSuccessListener {
                Log.e("TAG", "Saved")
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                hideDialog()
            }
            .addOnFailureListener {
                Log.e("TAG", it.message.toString())
            }
    }

}
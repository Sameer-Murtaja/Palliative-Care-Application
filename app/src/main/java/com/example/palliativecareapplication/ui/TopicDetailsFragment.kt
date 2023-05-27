package com.example.palliativecareapplication.ui

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.palliativecareapplication.databinding.FragmentTopicDetailsBinding
import com.example.palliativecareapplication.model.FirebaseNames
import com.example.palliativecareapplication.model.Topic
import com.example.palliativecareapplication.util.navigateWithReplaceFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class TopicDetailsFragment(var topic: Topic) : Fragment() {

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
        Picasso.get().load(topic.image).into(binding.imgTopic)
        binding.textInputTitle.setText(topic.title)
        binding.textInputDescription.setText(topic.description)
        binding.textInputDoctorName.setText(topic.doctorName)
    }

    private fun returnToMainOnAppBarButtonClick() {
        binding.appbar.setNavigationOnClickListener {
            MainActivity.swipeFragment(requireActivity(), ViewTopicsFragment());
        }
    }

    private fun handlePatientUI() {
        if (!MainActivity.user.isDoctor) {
            binding.btnEdit.visibility = View.GONE

            //if user is following this topic, show unfollow button
            if(MainActivity.user.topicsFollowed.contains(topic.id)){
                binding.buttonStopFollow.visibility = View.VISIBLE
            }else{
                binding.buttonFollow.visibility = View.VISIBLE
            }

            binding.buttonFollow.setOnClickListener {
                showDialog("جار الإضافة لقائمة المتابعة..")
                addTopicToUser()
                increaseFollowCount()
                subscribeToNotifications()
            }

            binding.buttonStopFollow.setOnClickListener {
                showDialog("جار الإزالة من قائمة المتابعة..")
                removeTopicFromUser()
                decreaseFollowCount()
                unsubscribeToNotifications()
            }
        }
    }

    private fun addTopicToUser(){
        MainActivity.user.topicsFollowed.add(topic.id)

        db.collection(FirebaseNames.COLLECTION_USERS).document(MainActivity.user.id)
            .set(MainActivity.user)
            .addOnSuccessListener {
                Log.e("tag", "added Topic To User Successfully $id")
            }
            .addOnFailureListener {
                Log.e("tag", it.message.toString())
            }
    }

    private fun increaseFollowCount() {
        topic.usersFollowing += 1
        val topic = hashMapOf(
            "title" to topic.title,
            "description" to topic.description,
            "doctorName" to topic.doctorName,
            "image" to topic.image,
            "usersFollowing" to topic.usersFollowing
        )

        db.collection(FirebaseNames.COLLECTION_TOPICS).document(this.topic.id)
            .update(topic as Map<String, Any>)
            .addOnSuccessListener {
                Log.e("TAG", "added Follow")
                Toast.makeText(
                    requireContext(),
                    "تمت إضافة الموضوع لقائمة المتابعة",
                    Toast.LENGTH_SHORT
                ).show()
                hideDialog()

                binding.buttonFollow.visibility = View.INVISIBLE
                binding.buttonStopFollow.visibility = View.VISIBLE

            }
            .addOnFailureListener {
                Log.e("TAG", it.message.toString())
                hideDialog()
            }
    }

    private fun subscribeToNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic(topic.title)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "Successfully subscribed to topic")
                } else {
                    Log.e("TAG", "Failed to subscribe to topic", task.exception)
                }
            }
    }

    private fun removeTopicFromUser(){
        MainActivity.user.topicsFollowed.remove(topic.id)

        db.collection(FirebaseNames.COLLECTION_USERS).document(MainActivity.user.id)
            .set(MainActivity.user)
            .addOnSuccessListener {
                Log.e("tag", "removed Topic from User Successfully $id")
            }
            .addOnFailureListener {
                Log.e("tag", it.message.toString())
            }
    }

    private fun decreaseFollowCount() {
        topic.usersFollowing -= 1

        val topic = hashMapOf(
            "title" to topic.title,
            "description" to topic.description,
            "doctorName" to topic.doctorName,
            "image" to topic.image,
            "usersFollowing" to topic.usersFollowing
        )

        db.collection(FirebaseNames.COLLECTION_TOPICS).document(this.topic.id)
            .update(topic as Map<String, Any>)
            .addOnSuccessListener {
                Log.e("TAG", "removed Follow")
                Toast.makeText(requireContext(), "تمت إزالة متابعة الموضوع", Toast.LENGTH_SHORT)
                    .show()
                hideDialog()

                binding.buttonFollow.visibility = View.VISIBLE
                binding.buttonStopFollow.visibility = View.INVISIBLE
            }
            .addOnFailureListener {
                Log.e("TAG", it.message.toString())
                hideDialog()
            }
    }

    private fun unsubscribeToNotifications() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic.title)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "Successfully unsubscribed to topic")
                } else {
                    Log.e("TAG", "Failed to unsubscribe to topic", task.exception)
                }
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


        val id = topic.id
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
            MainActivity.swipeFragment(requireActivity(), ViewPostsFragment(topic))
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
            "usersFollowing" to topic.usersFollowing
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
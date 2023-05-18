package com.example.palliativecareapplication

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.palliativecareapplication.databinding.FragmentAddPostBinding
import com.example.palliativecareapplication.model.Attachment
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

    private val attachments = ArrayList<Attachment>()

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


        binding.buttonAddAttachments.setOnClickListener {
            getContent.launch("*/*")
        }

        binding.appbar.setNavigationOnClickListener {
            MainActivity.swipeFragment(requireActivity(), ViewPostsFragment(topic));
        }


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
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
            if (uris != null) {
                Log.e("TAG", "$uris: ")
                uploadAttachments(uris)
            }
        }


    private fun uploadAttachments(attchmentsUris: List<Uri>) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val fileRef = storageRef.child(topic.title).child("files")

        showDialog("uploading...")
        var uploadedAttachmentsCount = 0
        for (attachmentUri in attchmentsUris) {
            val fileName = getFileNameFromUri(attachmentUri)
            val fileType = requireContext().contentResolver.getType(attachmentUri).toString()
            val fileExtension = fileType?.substring(fileType.lastIndexOf("/") + 1) ?: "*"


            val childRef = fileRef.child(
                fileName //System.currentTimeMillis().toString() + ".$fileExtension"
            )
            val uploadTask = childRef.putFile(attachmentUri)
            uploadTask.addOnSuccessListener { _ ->
                // Upload successful
                childRef.downloadUrl.addOnSuccessListener {
                    val attachment = Attachment(it,fileName,fileType)
                    attachments.add(attachment)
                }
                uploadedAttachmentsCount++
                binding.tvAttachmentsCount.text = "count: $uploadedAttachmentsCount"
                if (uploadedAttachmentsCount == attchmentsUris.size) {
                    hideDialog()
                }
            }.addOnFailureListener {
                // Upload failed
                Log.e("TAG", "uploadAttachments: ${it.message}")
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = ""

        val contentResolver = context?.contentResolver
        val cursor = contentResolver?.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    val displayName = it.getString(displayNameIndex)
                    fileName = displayName
                }
            }
        }

        return fileName
    }



    private fun addPost(
        title: String, details: String
    ) {
        val post = hashMapOf(
            "title" to title,
            "details" to details,
            "attachments" to attachments
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

package com.example.palliativecareapplication.ui.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.palliativecareapplication.ui.MainActivity
import com.example.palliativecareapplication.databinding.CardPostBinding
import com.example.palliativecareapplication.databinding.FragmentViewPostsBinding
import com.example.palliativecareapplication.model.FirebaseNames
import com.example.palliativecareapplication.model.Post
import com.example.palliativecareapplication.model.Topic
import com.example.palliativecareapplication.ui.LoginFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PostAdapter(var data: ArrayList<Post>, var topic: Topic, var parentBinding: FragmentViewPostsBinding) :
    RecyclerView.Adapter<PostAdapter.MyViewHolder>() {
    lateinit var context: Context
    lateinit var db: FirebaseFirestore
    private var initialData = data


    class MyViewHolder(val cardViewBinding: CardPostBinding) :
        RecyclerView.ViewHolder(cardViewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        db = Firebase.firestore

        val binding: CardPostBinding =
            CardPostBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.cardViewBinding.apply {
            tvTitle.text = data[position].title
            tvDetails.text = data[position].details
            if (data[position].attachments?.isNotEmpty() == true) {
                val attachmentsAdapter = AttachmentAdapter(data[position].attachments!!,topic, parentBinding)
                rvAttachments.layoutManager = GridLayoutManager(context, 2)
                rvAttachments.adapter = attachmentsAdapter
            }

            if(!MainActivity.user.isDoctor){
                btnDelete.visibility = View.GONE
            }
        }

        holder.cardViewBinding.btnDelete.setOnClickListener {
            showDeleteAlert(position)
        }


    }

    override fun getItemCount(): Int {
        return data.size
    }


    private fun showDeleteAlert(position: Int) {
        AlertDialog.Builder(context).apply {
            setTitle("Delete Post")
            setMessage("Are you sure that you want to delete this Post?")
            setPositiveButton("Yse") { _, _ ->
                deletePostById(position)
            }
            setCancelable(true)
            setNegativeButton("No") { dialogInterface: DialogInterface, _ ->
                dialogInterface.cancel()
            }
            create()
            show()
        }
    }


    private fun deletePostById(position: Int) {
        db.collection(FirebaseNames.COLLECTION_TOPICS)
            .document(topic.id).collection(FirebaseNames.COLLECTION_POSTS)
            .document(data[position].id)
            .delete()
            .addOnSuccessListener { _ ->
                Log.e("TAG", "Deleted Successfully")
                data.removeAt(position)
                notifyDataSetChanged()
            }.addOnFailureListener { exception ->
                Log.e("TAG", exception.message.toString())

            }
    }


    fun search(text: String) {
        val newArray = initialData.filter { post ->
            post.title.contains(text) // || post.doctorName.startsWith(text)
        }
        data = newArray as ArrayList<Post>
        notifyDataSetChanged()
    }

}
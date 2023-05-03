package com.example.palliativecareapplication.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.palliativecareapplication.databinding.CardTopicBinding
import com.example.palliativecareapplication.model.FirebaseNames
import com.example.palliativecareapplication.model.Topic
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class TopicAdapter(var data: ArrayList<Topic>): RecyclerView.Adapter<TopicAdapter.MyViewHolder>() {
    lateinit var context: Context
    lateinit var db: FirebaseFirestore
    private var initialData = data


    class MyViewHolder(val cardViewBinding: CardTopicBinding): RecyclerView.ViewHolder(cardViewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        db = Firebase.firestore

        val binding : CardTopicBinding
                = CardTopicBinding.inflate(LayoutInflater.from(context),parent,false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.cardViewBinding.apply {
            tvTitle.text = data[position].title
            tvDoctorName.text = data[position].doctorName
            Picasso.get().load(data[position].image).into(imgTopic);
        }

        holder.cardViewBinding.btnDelete.setOnClickListener {
            showDeleteAlert(position)
        }


//        holder.cardViewBinding.root.setOnClickListener {
//            ShowProductDetails.productData = data[position]
//            context.startActivity(Intent(context,ShowProductDetails::class.java))
//        }

    }

    override fun getItemCount(): Int {
        return data.size
    }



    private fun showDeleteAlert(position: Int) {
        AlertDialog.Builder(context).apply {
            setTitle("Delete topic")
            setMessage("Are you sure that you want to delete this topic?")
            setPositiveButton("Yse") { _, _ ->
                deleteProductById(position)
            }
            setCancelable(true)
            setNegativeButton("No") { dialogInterface: DialogInterface, _ ->
                dialogInterface.cancel()
            }
            create()
            show()
        }
    }


    fun deleteProductById(position: Int) {
        db.collection(FirebaseNames.COLLECTION_TOPICS).document(data[position].id)
            .delete()
            .addOnSuccessListener { _ ->
                Log.e("TAG", "Deleted Successfully")
                data.removeAt(position)
                notifyDataSetChanged()
            }.addOnFailureListener { exception ->
                Log.e("TAG", exception.message.toString())

            }
    }


    fun search(text : String){
        val newArray = initialData.filter { topic ->
            topic.title.contains(text) // || topic.doctorName.startsWith(text)
        }
        data = newArray as ArrayList<Topic>
        notifyDataSetChanged()
    }

}
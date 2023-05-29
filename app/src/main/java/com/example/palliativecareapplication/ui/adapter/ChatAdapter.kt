package com.example.palliativecareapplication.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.palliativecareapplication.databinding.MessageCardBinding
import com.example.palliativecareapplication.model.Message

class ChatAdapter (var data: List<Message>): RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {
    init {
        Log.e("bkk", "from adapter: $data ", )
    }

    class MyViewHolder(val cardViewBinding: MessageCardBinding): RecyclerView.ViewHolder(cardViewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val binding : MessageCardBinding
                = MessageCardBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.cardViewBinding.apply {
            Log.e("bkk", "onBindViewHolder: name: ${data[position].name} /// message: ${data[position].message}", )
            tvName.text = data[position].name
            tvMessage.text = data[position].message

        }

    }

    override fun getItemCount() = data.size


    fun addData(newData: List<Message>){
        if (newData.size != data.size){
            data = newData
            notifyDataSetChanged()
        }
    }


}
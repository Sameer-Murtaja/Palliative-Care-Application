package com.example.palliativecareapplication.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.palliativecareapplication.databinding.CardMessageReceivedBinding
import com.example.palliativecareapplication.databinding.CardMessageSentBinding
import com.example.palliativecareapplication.model.Message
import com.example.palliativecareapplication.ui.MainActivity

class ChatAdapter(var data: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    class SentMessageViewHolder(val cardViewBinding: CardMessageSentBinding) :
        RecyclerView.ViewHolder(cardViewBinding.root)

    class ReceivedMessageViewHolder(val cardViewBinding: CardMessageReceivedBinding) :
        RecyclerView.ViewHolder(cardViewBinding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == VIEW_TYPE_SENT) {
            val binding: CardMessageSentBinding =
                CardMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SentMessageViewHolder(binding)

        } else {
            val binding: CardMessageReceivedBinding = CardMessageReceivedBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SentMessageViewHolder) {
            holder.cardViewBinding.apply {
                tvName.text = extractUsername(data[position].name.toString())
                tvMessage.text = data[position].message
            }
        } else if (holder is ReceivedMessageViewHolder) {
            holder.cardViewBinding.apply {
                tvName.text = extractUsername(data[position].name.toString())
                tvMessage.text = data[position].message
            }
        }

    }

    override fun getItemCount() = data.size

    override fun getItemViewType(position: Int): Int {
        return if(MainActivity.user.name == data[position].name){
            VIEW_TYPE_SENT
        }else{
            VIEW_TYPE_RECEIVED
        }
    }

    fun addData(newData: List<Message>) {
        if (newData.size != data.size) {
            data = newData
            notifyDataSetChanged()
        }
    }

    private fun extractUsername(email: String): String {
        val atIndex = email.indexOf('@')
        return if (atIndex != -1) {
            email.substring(0, atIndex)
        } else {
            email
        }
    }


}

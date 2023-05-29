package com.example.palliativecareapplication.ui

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.palliativecareapplication.databinding.FragmentChatBinding
import com.example.palliativecareapplication.model.ChatJson
import com.example.palliativecareapplication.model.Message
import com.example.palliativecareapplication.ui.adapter.ChatAdapter
import com.example.palliativecareapplication.ui.adapter.PostAdapter
import com.example.palliativecareapplication.util.MassageMapper
import com.example.palliativecareapplication.util.navigateWithReplaceFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.json.JSONObject


class ChatFragment : Fragment() {
    lateinit var binding: FragmentChatBinding
    val database = Firebase.database
    val myRef = database.getReference("chat")
    var count = 0
    lateinit var chatAdapter: ChatAdapter
    lateinit var messageList: ArrayList<Message>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageList = ArrayList()

        binding.btnSend.setOnClickListener {
            var messageInput = binding.textInputMessage.text.toString()
            if (messageInput.isNotEmpty()) {
                val message = hashMapOf(
                    "message" to messageInput,
                    "name" to MainActivity.user.name,
                )
                myRef.child("Doctor").child(count.toString()).setValue(message)
                //count++
                Toast.makeText(requireContext(), "success", Toast.LENGTH_SHORT).show()
                binding.textInputMessage.setText("")
            }

            binding.textInputMessage.setText("")
        }

        myRef.child("Doctor")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val messageMapper = MassageMapper()
                    snapshot.value.let {
                        val messages = messageMapper.map(snapshot.value.toString())
                        count = messages.size
                        setupAdapter(messages.toList())
                        //chatAdapter.addData(messages.toList())
                        Log.e("bk", "onDataChange: ${messages.toList()}", )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "error", Toast.LENGTH_SHORT).show()
                }
            })



    }

    override fun onResume() {
        super.onResume()

        binding.appbar.setNavigationOnClickListener {
            this.navigateWithReplaceFragment(ViewTopicsFragment())
        }
    }

    fun setupAdapter(messages: List<Message>){
        chatAdapter = ChatAdapter(messages)
        binding.rsView.layoutManager = LinearLayoutManager(requireContext())
        binding.rsView.adapter = chatAdapter
        chatAdapter.notifyDataSetChanged()
    }

}


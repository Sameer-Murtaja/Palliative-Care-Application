package com.example.palliativecareapplication.adapter

import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.palliativecareapplication.ViewPostsFragment
import com.example.palliativecareapplication.databinding.CardAttachmentBinding
import com.example.palliativecareapplication.databinding.FragmentViewPostsBinding
import com.example.palliativecareapplication.model.Attachment
import com.example.palliativecareapplication.model.Topic
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class AttachmentAdapter(
    var data: List<Attachment>,
    var topic: Topic,
    var parentBinding: FragmentViewPostsBinding
) : RecyclerView.Adapter<AttachmentAdapter.MyViewHolder>() {
    lateinit var context: Context
    lateinit var db: FirebaseFirestore

    private var player: ExoPlayer? = null
    private var progressDialog: ProgressDialog? = null


    class MyViewHolder(val cardViewBinding: CardAttachmentBinding) :
        RecyclerView.ViewHolder(cardViewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        db = Firebase.firestore

        val binding: CardAttachmentBinding =
            CardAttachmentBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val attachment = data[position]
        holder.cardViewBinding.apply {
            if (attachment.isImage()) {
                tvAttachment.visibility = View.GONE
                playerView.visibility = View.GONE
                Picasso.get().load(attachment.uri).into(imgAttachment)

                imgAttachment.setOnClickListener {
                    parentBinding.fullscreenLayout.visibility = View.VISIBLE
                    parentBinding.fullscreenVideo.visibility = View.GONE
                    parentBinding.fullscreenImg.visibility = View.VISIBLE
                    Picasso.get().load(attachment.uri).into(parentBinding.fullscreenImg)
                }

            } else if (attachment.isVideo()) {
                tvAttachment.visibility = View.GONE
                imgAttachment.visibility = View.GONE
                initializePlayer(attachment.uri)
                playerView.player = player

                playerView.setOnClickListener {
                    parentBinding.fullscreenLayout.visibility = View.VISIBLE
                    parentBinding.fullscreenVideo.visibility = View.VISIBLE
                    parentBinding.fullscreenImg.visibility = View.GONE
                    parentBinding.fullscreenVideo.player = player
                }


            } else {
                handleOtherAttachmentUI(attachment)
            }
        }
    }


    override fun getItemCount(): Int {
        return data.size
    }


    private fun initializePlayer(uri: Uri) {
        player = ExoPlayer.Builder(context) // <- context
            .build()

        // create a media item.
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMimeType(MimeTypes.APPLICATION_MP4)
            .build()

        // Create a media source and pass the media item
        val mediaSource = ProgressiveMediaSource.Factory(
            DefaultDataSource.Factory(context) // <- context
        )
            .createMediaSource(mediaItem)

        // Finally assign this media source to the player
        player?.apply {
            setMediaSource(mediaSource)
            playWhenReady = true // start playing when the exoplayer has setup
            seekTo(0, 0L) // Start from the beginning
            prepare() // Change the state from idle.
        }
    }


    private fun CardAttachmentBinding.handleOtherAttachmentUI(
        attachment: Attachment
    ) {
        playerView.visibility = View.GONE
        imgAttachment.visibility = View.GONE

        setUnderlinedAttachmentName(attachment)

        downloadAttachmentOnClick(attachment)
    }

    private fun CardAttachmentBinding.setUnderlinedAttachmentName(
        attachment: Attachment
    ) {
        val mSpannableString = SpannableString(attachment.name)
        mSpannableString.setSpan(UnderlineSpan(), 0, mSpannableString.length, 0)
        tvAttachment.text = mSpannableString
    }

    private fun CardAttachmentBinding.downloadAttachmentOnClick(
        attachment: Attachment
    ) {
        tvAttachment.setOnClickListener {
            showDialog("Downloading..")
            val storage = Firebase.storage
            val storageRef = storage.reference
            val fileRef = storageRef.child(topic.title).child("files")
            fileRef.child(attachment.name)
                .downloadUrl
                .addOnSuccessListener {
                    downloadFile(it.toString(), attachment.name)
                    Toast.makeText(context, "Downloading..", Toast.LENGTH_SHORT)
                        .show()
                    hideDialog()
                }
                .addOnFailureListener {
                    Log.e("TAG", "onCreate: ${it.message}")
                    Toast.makeText(context, "Download failed, ${it.message}", Toast.LENGTH_SHORT)
                        .show()
                    hideDialog()
                }

        }
    }


    private fun downloadFile(downloadUrl: String, fileName: String) {
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle(fileName)
            .setDescription("Downloading..")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }


    private fun showDialog(msg: String) {
        progressDialog = ProgressDialog(context)
        progressDialog!!.setMessage(msg)
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    }


    private fun hideDialog() {
        if (progressDialog!!.isShowing)
            progressDialog!!.dismiss()
    }


}
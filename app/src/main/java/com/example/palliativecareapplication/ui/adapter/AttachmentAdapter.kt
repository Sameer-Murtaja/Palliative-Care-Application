package com.example.palliativecareapplication.ui.adapter

import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.palliativecareapplication.databinding.CardAttachmentDownloadableBinding
import com.example.palliativecareapplication.databinding.CardAttachmentImageBinding
import com.example.palliativecareapplication.databinding.CardAttachmentVideoBinding
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
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var context: Context
    lateinit var db: FirebaseFirestore

    private var player: ExoPlayer? = null
    private var progressDialog: ProgressDialog? = null

    companion object {
        private const val TYPE_IMAGE = 1
        private const val TYPE_VIDEO = 2
        private const val TYPE_DOWNLOADABLE = 3
    }

    class ImageViewHolder(val cardViewBinding: CardAttachmentImageBinding) :
        RecyclerView.ViewHolder(cardViewBinding.root)

    class VideoViewHolder(val cardViewBinding: CardAttachmentVideoBinding) :
        RecyclerView.ViewHolder(cardViewBinding.root)

    class DownloadableViewHolder(val cardViewBinding: CardAttachmentDownloadableBinding) :
        RecyclerView.ViewHolder(cardViewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        db = Firebase.firestore

        if (viewType == TYPE_IMAGE) {
            val binding: CardAttachmentImageBinding =
                CardAttachmentImageBinding.inflate(LayoutInflater.from(context), parent, false)
            return ImageViewHolder(binding)
        } else if (viewType == TYPE_VIDEO) {
            val binding: CardAttachmentVideoBinding =
                CardAttachmentVideoBinding.inflate(LayoutInflater.from(context), parent, false)
            return VideoViewHolder(binding)
        } else {
            val binding: CardAttachmentDownloadableBinding =
                CardAttachmentDownloadableBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            return DownloadableViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val attachment = data[position]
        if (holder is ImageViewHolder) {
            Picasso.get().load(attachment.uri).into(holder.cardViewBinding.imgAttachment)
            holder.cardViewBinding.imgAttachment.setOnClickListener {
                parentBinding.fullscreenLayout.visibility = View.VISIBLE
                parentBinding.fullscreenVideo.visibility = View.GONE
                parentBinding.fullscreenImg.visibility = View.VISIBLE
                Picasso.get().load(attachment.uri).into(parentBinding.fullscreenImg)
            }
        } else if (holder is VideoViewHolder) {
            initializePlayer(attachment.uri)
            holder.cardViewBinding.playerView.player = player
            holder.cardViewBinding.playerView.setOnClickListener {
                parentBinding.fullscreenLayout.visibility = View.VISIBLE
                parentBinding.fullscreenVideo.visibility = View.VISIBLE
                parentBinding.fullscreenImg.visibility = View.GONE
                parentBinding.fullscreenVideo.player = player
            }


        } else if(holder is DownloadableViewHolder) {
            holder.cardViewBinding.handleOtherAttachmentUI(attachment)
        }
    }


    override fun getItemCount(): Int {
        return data.size
    }


    override fun getItemViewType(position: Int): Int {
        return if(data[position].isImage()){
            TYPE_IMAGE
        }else if(data[position].isVideo()){
            TYPE_VIDEO
        }else{
            TYPE_DOWNLOADABLE
        }
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
            playWhenReady = false // start playing when the exoplayer has setup
            seekTo(0, 0L) // Start from the beginning
            prepare() // Change the state from idle.
        }
    }


    private fun CardAttachmentDownloadableBinding.handleOtherAttachmentUI(
        attachment: Attachment
    ) {
        setUnderlinedAttachmentName(attachment)

        downloadAttachmentOnClick(attachment)
    }

    private fun CardAttachmentDownloadableBinding.setUnderlinedAttachmentName(
        attachment: Attachment
    ) {
        val mSpannableString = SpannableString(attachment.name)
        mSpannableString.setSpan(UnderlineSpan(), 0, mSpannableString.length, 0)
        tvAttachment.text = mSpannableString
        tvAttachment.movementMethod = LinkMovementMethod.getInstance()

    }

    private fun CardAttachmentDownloadableBinding.downloadAttachmentOnClick(
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
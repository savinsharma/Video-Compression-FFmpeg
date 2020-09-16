package com.videocompression.activity

import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.videocompression.R
import com.videocompression.databinding.LayoutCompressVideoBinding
import com.videocompression.helper.GeneralHelper
import com.videocompression.model.VideoDataViewModel
import com.videocompression.model.ViewModelFactory
import nl.bravobit.ffmpeg.FFmpeg
import java.io.File


class PlayCompressedVideoActivity : AppCompatActivity() {

    private lateinit var videoDataViewModel: VideoDataViewModel
    private var layoutCompressVideoBinding: LayoutCompressVideoBinding? = null
    private val activity = this@PlayCompressedVideoActivity
    private var generalHelper: GeneralHelper? = null
    private var videoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutCompressVideoBinding =
            DataBindingUtil.setContentView(activity, R.layout.layout_compress_video)

        videoDataViewModel = activity.run {
            ViewModelProvider(
                activity,
                ViewModelFactory.getInstance()
            ).get(VideoDataViewModel::class.java)
        }

        videoDataViewModel.compressedVideoURI.observe(this, Observer {
            videoPath = videoDataViewModel.compressedVideoURI.value
            if (videoPath!!.isNotEmpty()) {
                layoutCompressVideoBinding!!.videoView.setVideoURI(Uri.parse(videoPath))
                val mediaController = MediaController(activity)
                mediaController.setAnchorView(layoutCompressVideoBinding!!.videoView)
                layoutCompressVideoBinding!!.videoView.setMediaController(mediaController)
            }
        })
        layoutCompressVideoBinding!!.videoView.setOnPreparedListener {
            layoutCompressVideoBinding!!.videoView.requestFocus()
            layoutCompressVideoBinding!!.videoView.start()
        }
        layoutCompressVideoBinding!!.linearDataEntry.visibility = View.GONE
        layoutCompressVideoBinding!!.linearSuccess.visibility = View.VISIBLE
        layoutCompressVideoBinding!!.ivSuccess.setColorFilter(Color.WHITE)
    }

    init {
        generalHelper = GeneralHelper(activity)
    }
}
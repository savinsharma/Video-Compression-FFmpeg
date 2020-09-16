package com.videocompression.activity

import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.videocompression.R
import com.videocompression.databinding.LayoutCompressVideoBinding
import com.videocompression.helper.GeneralHelper
import com.videocompression.helper.ProgressDialog
import com.videocompression.model.VideoDataViewModel
import com.videocompression.model.ViewModelFactory
import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler
import nl.bravobit.ffmpeg.FFmpeg
import nl.bravobit.ffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import java.io.File


class CompressVideoActivity : AppCompatActivity() {

    private lateinit var videoDataViewModel: VideoDataViewModel
    private var layoutCompressVideoBinding: LayoutCompressVideoBinding? = null
    private val activity = this@CompressVideoActivity
    private var generalHelper: GeneralHelper? = null
    private var videoPath: String? = null
    private var ffmpeg: FFmpeg? = null
    private var compressedFilePath: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutCompressVideoBinding =
            DataBindingUtil.setContentView(activity, R.layout.layout_compress_video)

        ffmpeg = FFmpeg.getInstance(activity)

        videoDataViewModel = activity.run {
            ViewModelProvider(
                activity,
                ViewModelFactory.getInstance()
            ).get(VideoDataViewModel::class.java)
        }

        videoDataViewModel.videoURI.observe(this, Observer {

            videoPath = videoDataViewModel.videoURI.value

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
        layoutCompressVideoBinding!!.linearDataEntry.visibility = View.VISIBLE
        layoutCompressVideoBinding!!.linearSuccess.visibility = View.GONE
    }

    fun compressVideo(view: View) {
        if (validation()) {
            if (ffmpeg != null && ffmpeg!!.isSupported) {
                val frame = layoutCompressVideoBinding!!.etFrameWidth.text.toString() + "x" + layoutCompressVideoBinding!!.etFrameheight.text.toString()
                compressUsingFFMPEG(
                    generalHelper!!.getPath(Uri.parse(videoPath), activity),
                    frame,
                    layoutCompressVideoBinding!!.etVideoBitrateValue.text.toString(),
                    layoutCompressVideoBinding!!.etAudioBitrateValue.text.toString()
                )
            }
        }
    }

    private fun compressUsingFFMPEG(
        inputVideoPath: String?,
        frame: String,
        bitRateVideo: String,
        bitRateAudio: String
    ) {
        try {
            var folderName = "CompressedVideos"
            val outputPath = activity.getExternalFilesDir(folderName)
            if (outputPath != null && !outputPath.exists()) outputPath.mkdirs()

            compressedFilePath = File.createTempFile(
                "compressed_video_" + System.currentTimeMillis(),
                ".mp4",
                outputPath
            )

            println("Something with file path - " + compressedFilePath!!.path)

            var commandArray = arrayOf(
                "-y",
                "-i",
                inputVideoPath,
                "-s",
                frame,
                "-r",
                "20",
                "-vcodec",
                "mpeg4",
                "-b:v",
                bitRateVideo + "k",
                "-b:a",
                bitRateAudio + "k",
                "-ac",
                "3",
                "-ar",
                "22050",
                compressedFilePath!!.path
            )
            val dialog = ProgressDialog.progressDialog(activity)
            ffmpeg!!.execute(commandArray, object : ExecuteBinaryResponseHandler() {
                override fun onStart() {
                    Log.e("FFmpeg", "onStart")
                    dialog.show()
                }

                override fun onProgress(message: String) {
                }

                override fun onFailure(message: String) {
                    Log.e("FFmpeg onFailure ", message)
                    Toast.makeText(
                        activity,
                        activity.resources.getText(R.string.failed_to_compress_video),
                        Toast.LENGTH_SHORT
                    ).show();
                }

                override fun onSuccess(message: String) {
                    Toast.makeText(
                        activity,
                        activity.resources.getText(R.string.video_compression_success),
                        Toast.LENGTH_SHORT
                    ).show();
                    dialog.dismiss()

                    videoDataViewModel.compressedData(Uri.fromFile(compressedFilePath!!).toString())
                    val intent = Intent(activity, PlayCompressedVideoActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun onFinish() {
                    dialog.dismiss()
                }
            })
        } catch (e: FFmpegCommandAlreadyRunningException) {
            e.printStackTrace()
        }
    }

    private fun validation(): Boolean {
        when {
            layoutCompressVideoBinding!!.etVideoBitrateValue.text.toString().trim().isEmpty() -> {
                Toast.makeText(
                    activity,
                    activity.resources.getText(R.string.please_enter_value_for_video_bitrate),
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            layoutCompressVideoBinding!!.etAudioBitrateValue.text.toString().trim().isEmpty() -> {
                Toast.makeText(
                    activity,
                    activity.resources.getText(R.string.please_enter_value_for_audio_birate),
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            layoutCompressVideoBinding!!.etFrameWidth.text.toString().trim().isEmpty() -> {
                Toast.makeText(
                    activity,
                    activity.resources.getText(R.string.please_enter_value_for_frame_width),
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            layoutCompressVideoBinding!!.etFrameheight.text.toString().trim().isEmpty() -> {
                Toast.makeText(
                    activity,
                    activity.resources.getText(R.string.please_enter_value_for_frame_height),
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            else -> {
                return true
            }
        }
    }

    init {
        generalHelper = GeneralHelper(activity)
    }
}
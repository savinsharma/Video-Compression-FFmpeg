package com.videocompression.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.ViewModelProvider
import com.videocompression.R
import com.videocompression.`interface`.DialogCallBack
import com.videocompression.databinding.LayoutSelectVideoBinding
import com.videocompression.helper.GeneralHelper
import com.videocompression.model.VideoDataViewModel
import com.videocompression.model.ViewModelFactory

class SelectVideoActivty : AppCompatActivity() {

    private lateinit var videoDataViewModel: VideoDataViewModel
    private val PICK_VIDEO = 101
    private val WRITE_EXTERNAL_STORAGE = 102
    private var layoutSelectVideoBinding: LayoutSelectVideoBinding? = null
    private var activity = this@SelectVideoActivty
    private var generalHelper: GeneralHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutSelectVideoBinding = setContentView(activity, R.layout.layout_select_video)
        videoDataViewModel = activity.run {
            ViewModelProvider(
                activity,
                ViewModelFactory.getInstance()
            ).get(VideoDataViewModel::class.java)
        }
    }

    fun selectVideo(view: View) {
        if (Build.VERSION.SDK_INT >= 23) {
            checkForStoragePermission()
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        var pickIntent: Intent? = Intent(Intent.ACTION_GET_CONTENT)
        pickIntent!!.type = "video/*"
        startActivityForResult(pickIntent, PICK_VIDEO)
    }

    private fun checkForStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) === PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE
            )
        } else {
            openGallery()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    // permission dialog
                    generalHelper!!.openAlertDialog(
                        activity,
                        resources.getString(R.string.permission),
                        resources.getString(R.string.storage_permission_for_video),
                        resources.getString(R.string.settings),
                        resources.getString(R.string.cancel),
                        object : DialogCallBack {
                            override fun onPositiveClick() {
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            }
                            override fun onNegativeClick() {}
                        })
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_VIDEO) {
            if (data != null && data.data != null) {
                val selectVideoURI: Uri? = data.data
                if (selectVideoURI != null) {
                    videoDataViewModel.data(selectVideoURI.toString())
                    val intent = Intent(activity, CompressVideoActivity::class.java)
                    startActivity(intent)
                }
            } else {

            }
        }
    }

    init {
        generalHelper = GeneralHelper(activity)
    }
}
package com.videocompression.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VideoDataViewModel : ViewModel() {

    companion object {
        private var instance: VideoDataViewModel? = null
        fun getInstance() =
            instance ?: synchronized(VideoDataViewModel::class.java) {
                instance ?: VideoDataViewModel().also { instance = it }
            }
    }

    val videoURI = MutableLiveData<String>()
    val compressedVideoURI = MutableLiveData<String>()

    fun data(item: String) {
        videoURI.value = item
    }

    fun compressedData(item: String) {
        compressedVideoURI.value = item
    }
}
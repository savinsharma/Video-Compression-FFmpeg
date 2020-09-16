package com.videocompression.helper

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.videocompression.R
import com.videocompression.`interface`.DialogCallBack


class GeneralHelper(private val activity: Activity) {

    fun openAlertDialog(
        activity: Activity,
        title: String?,
        message: String?,
        positiveBtnTxt: String?,
        negativeBtnTxt: String?,
        dailogCallBack: DialogCallBack
    ) {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        // set title
        if (title != null && title.isNotEmpty()) {
            alertDialogBuilder.setTitle(title)
        }
        // set dialog message
        alertDialogBuilder
            .setMessage(message)
            .setCancelable(false)
        if (positiveBtnTxt != null && positiveBtnTxt.isNotEmpty()) {
            alertDialogBuilder.setPositiveButton(positiveBtnTxt) { dialog, _ ->
                dailogCallBack.onPositiveClick()
                dialog.cancel()
            }
        }
        if (negativeBtnTxt != null && negativeBtnTxt.isNotEmpty()) {
            alertDialogBuilder.setNegativeButton(negativeBtnTxt) { dialog, _ ->
                dailogCallBack.onNegativeClick()
                dialog.cancel()
            }
        }

        // create alert dialog
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
        val buttonBackGround = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        buttonBackGround.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary))
        val buttonBackGround1 = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        buttonBackGround1.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary))
    }

    fun getPath(uri: Uri, activity: Activity): String? {
        var filePath: String? = ""
        var isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        if (isKitKat) {
            filePath = generateFromKitkat(uri, activity)
        }

        if (filePath != null) {
            return filePath
        }

        try {
            var cursor = activity.contentResolver.query(
                uri,
                arrayOf(MediaStore.MediaColumns.DATA),
                null,
                null,
                null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    var columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    filePath = cursor.getString(columnIndex)
                }
                cursor.close()
            }
        } catch (e: java.lang.Exception) {

        }
        return filePath ?: uri.path.toString()
    }


    @TargetApi(19)
    private fun generateFromKitkat(uri: Uri, context: Context): String {
        var filePath = ""
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                val wholeID = DocumentsContract.getDocumentId(uri)
                var id = ""
                if (wholeID.contains(":"))
                    id = wholeID.split(":")[1]

                val column = arrayOf(MediaStore.Video.Media.DATA)
                val sel = MediaStore.Video.Media._ID + "=?"

                val cursor = context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    column, sel, arrayOf(id), null
                )


                val columnIndex = cursor!!.getColumnIndex(column[0])

                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex)
                }

                cursor.close()
            }
        } catch (ex: Exception) {
            print("File Path exception " + ex.message)
        }
        return filePath
    }
}
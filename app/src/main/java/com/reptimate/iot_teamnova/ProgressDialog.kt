package com.reptimate.iot_teamnova


import android.app.Dialog
import android.content.Context
import android.view.Window

class ProgressDialog(context: Context) : Dialog(context) {

    init {
        // Make the dialog title invisible...
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_progress)
        setCancelable(false)
    }
}
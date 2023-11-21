package com.example.iot_teamnova

import com.example.iot_teamnova.BoardFragment
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.annotation.RequiresApi

class MyWebChromeClient(private val fragment: BoardFragment) : WebChromeClient() {
    private var mFilePathCallback: ValueCallback<Array<Uri>?>? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>?>?, fileChooserParams: FileChooserParams?): Boolean {
        mFilePathCallback?.onReceiveValue(null)
        mFilePathCallback = null

        mFilePathCallback = filePathCallback

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // 다중 파일 선택 허용

        fragment.startActivityForResult(intent, 0)

        return true
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                if (mFilePathCallback != null) {
                    val data = intent?.clipData
                    if (data != null) {
                        val uris = mutableListOf<Uri>()
                        for (i in 0 until data.itemCount) {
                            val uri = getResultUri(data.getItemAt(i).uri)
                            uri?.let { uris.add(it) }
                            uri?.toString()?.let { Log.d("fad", it) }
                        }
                        mFilePathCallback?.onReceiveValue(uris.toTypedArray())
                        println(uris.toTypedArray())
                    } else {
                        val result = getResultUri(intent?.data)
                        mFilePathCallback?.onReceiveValue(result?.let { arrayOf(it) })
                        println(result?.let { arrayOf(it) })
                    }
                    mFilePathCallback = null
                }
            } else {
                mFilePathCallback?.onReceiveValue(null)
                mFilePathCallback = null
            }
        }
    }

    private fun getResultUri(uri: Uri?): Uri? {
        return uri?.let {
            val filePath: String? =
                "file:" + RealPathUtil.getRealPath(fragment.requireContext(), it)

            Uri.parse(filePath)
        }
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        // 콘솔 로그 메시지를 얻어옴
        val message = consoleMessage.message()

        // 콘솔 메시지를 로그에 출력하거나 다른 방식으로 처리
        Log.d("WebViewConsole", message)

        // false를 반환하면 기본 동작을 유지
        return false
    }
}
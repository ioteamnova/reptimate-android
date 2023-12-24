package com.reptimate.iot_teamnova

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.*
import android.webkit.ConsoleMessage
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.reptimate.iot_teamnova.customAlbum.CustomAlbumActivity2

class MyWebChromeClient(private val fragment: BoardFragment) : WebChromeClient() {
    private var mFilePathCallback: ValueCallback<Array<Uri>?>? = null

//    override fun getDefaultVideoPoster() : Bitmap? {
//        return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
//    }

    private var mCustomView: View? = null
    private var mCustomViewCallback: CustomViewCallback? = null
    private var mFullscreenContainer: FullscreenHolder? = null
    private var mOriginalOrientation: Int = 0

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (mCustomView != null) {
                callback?.onCustomViewHidden()
                return
            }

            mOriginalOrientation = fragment.requireActivity().requestedOrientation
            val decor = fragment.requireActivity().window.decorView as FrameLayout
            mFullscreenContainer = FullscreenHolder(fragment.requireActivity())
            mFullscreenContainer?.addView(view, COVER_SCREEN_PARAMS)
            decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS)
            mCustomView = view
            setFullscreen(true)
            mCustomViewCallback = callback
        }

        super.onShowCustomView(view, callback)
    }

    @Suppress("DEPRECATION")
    override fun onShowCustomView(view: View?, requestedOrientation: Int, callback: CustomViewCallback?) {
        onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
        if (mCustomView == null) {
            return
        }

        setFullscreen(false)
        val decor = fragment.requireActivity().window.decorView as FrameLayout
        decor.removeView(mFullscreenContainer)
        mFullscreenContainer = null
        mCustomView = null
        mCustomViewCallback?.onCustomViewHidden()
        fragment.requireActivity().requestedOrientation = mOriginalOrientation
    }

    private fun setFullscreen(enabled: Boolean) {
        val win: Window = fragment.requireActivity().window
        val winParams: WindowManager.LayoutParams = win.attributes
        val bits: Int = WindowManager.LayoutParams.FLAG_FULLSCREEN
        if (enabled) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
            if (mCustomView != null) {
                mCustomView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
        win.attributes = winParams
    }

    private class FullscreenHolder(ctx: Context) : FrameLayout(ctx) {
        init {
            setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.black))
        }

        override fun onTouchEvent(evt: MotionEvent?): Boolean {
            return true
        }
    }

    companion object {
        private val COVER_SCREEN_PARAMS = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private val activityForResult =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val getData: Intent? = result.data
                val data: ArrayList<String>? = getData?.getStringArrayListExtra("data")
                if (data != null) {
                    val uris = mutableListOf<Uri>()
                    for (i in 0 until data.size) {
                        var uri = getResultUri(Uri.parse(data[i]))
                        uri?.let { uris.add(it) }
                        uri?.toString()?.let { Log.d("fad", it) }
                    }
                    mFilePathCallback?.onReceiveValue(uris.toTypedArray())
                } else {
//                            val result = getResultUri(Uri.parse(data[0]))
//                            mFilePathCallback?.onReceiveValue(result?.let { arrayOf(it) })
                }
                mFilePathCallback = null
            } else {
                mFilePathCallback?.onReceiveValue(null)
                mFilePathCallback = null
            }
        }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>?>?, fileChooserParams: FileChooserParams?): Boolean {
        mFilePathCallback?.onReceiveValue(null)
        mFilePathCallback = null

        mFilePathCallback = filePathCallback

        when {
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                //스토리지 읽기 권한이 허용이면 커스텀 앨범 띄워주기
                //권한 있을 경우 : PERMISSION_GRANTED
                //권한 없을 경우 : PERMISSION_DENIED
                val startCustomAlbum = Intent(fragment.requireContext(), CustomAlbumActivity2::class.java)
                activityForResult.launch(startCustomAlbum)
            }

            fragment.shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                //권한을 명시적으로 거부한 경우 : ture
                //다시 묻지 않음을 선택한 경우 : false
                //다이얼로그를 띄워 권한 팝업을 해야하는 이유 및 권한팝업을 허용하여야 접근 가능하다는 사실을 알려줌
                showPermissionAlertDialog()
            }

            else -> {
                //권한 요청
                fragment.requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    0
                )
            }
        }

        return true
    }

    private fun showPermissionAlertDialog() {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle("권한 승인이 필요합니다.")
            .setMessage("사진을 선택 하시려면 권한이 필요합니다.")
            .setPositiveButton("허용하기") { _, _ ->
                fragment.requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    0
                )
            }
            .setNegativeButton("취소하기") { _, _ -> }
            .create()
            .show()
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //권한 허용클릭
                    //TODO()앨범으로 이동시키기!
                } else if (fragment.shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //권한 처음으로 거절 했을 경우
                    //한번더 권한 요청
                    showPermissionAlertDialog()
                } else {
                    //권한 두번째로 거절 한 경우 (다시 묻지 않음)
                    //설정 -> 권한으로 이동하는 다이얼로그
                    goSettingActivityAlertDialog()
                }
            }
        }
    }
    private fun goSettingActivityAlertDialog() {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle("권한 승인이 필요합니다.")
            .setMessage("앨범에 접근 하기 위한 권한이 필요합니다.\n권한 -> 저장공간 -> 허용")
            .setPositiveButton("허용하러 가기") { _, _ ->
                val goSettingPermission = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                goSettingPermission.data = Uri.parse("package:${fragment}packageName")
                fragment.startActivity(goSettingPermission)
            }
            .setNegativeButton("취소") { _, _ -> }
            .create()
            .show()
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
                    } else {
                        val result = getResultUri(intent?.data)
                        mFilePathCallback?.onReceiveValue(result?.let { arrayOf(it) })
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
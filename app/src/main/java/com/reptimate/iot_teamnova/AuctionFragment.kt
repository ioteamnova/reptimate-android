package com.reptimate.iot_teamnova

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.reptimate.iot_teamnova.Live.LiveActivity
import org.json.JSONObject

class AuctionFragment : Fragment() {
    @SuppressLint("SetJavaScriptEnabled")
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var webView: WebView
    private lateinit var sharedPreferences: SharedPreferences // SharedPreferences 객체 추가
    private lateinit var myWebChromeClient: MyWebChromeClient2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.frag_board, container, false)

        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout)
        webView = rootView.findViewById(R.id.webview)

//        class AndroidInterface(private val context: Context) {
//            @JavascriptInterface
//            fun requestNotificationPermission() {
//                // 알림 권한 요청 처리
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    // Android 8.0 이상에서는 알림 채널을 설정해야 합니다.
//                    val channelId = "my_channel_id"
//                    val channelName = "My Channel"
//                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                    notificationManager.createNotificationChannel(NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH))
//                }
//            }
//        }
//        webView.addJavascriptInterface(AndroidInterface(requireActivity()), "Android")

        myWebChromeClient = MyWebChromeClient2(this@AuctionFragment)

        sharedPreferences = requireContext().getSharedPreferences("user_token", Context.MODE_PRIVATE)

        // WebView 설정
        webView.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true
            settings.useWideViewPort = true // Use wide viewport
            webView.webChromeClient = myWebChromeClient
        }

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        val accessToken = sharedPreferences.getString("accessToken", "")
        val idx = sharedPreferences.getString("idx", "")
        val refreshToken = sharedPreferences.getString("refreshToken", "")
        val nickname = sharedPreferences.getString("nickname", "")
        val profilePath = sharedPreferences.getString("profilePath", "")

        val cookieObject = JSONObject()
        cookieObject.put("accessToken", accessToken)
        cookieObject.put("idx", idx)
        cookieObject.put("refreshToken", refreshToken)
        cookieObject.put("nickname", nickname)
        cookieObject.put("profilePath", profilePath)

        Log.d("1 : ", "myAppCookie=$cookieObject")

        val cookieString = "myAppCookie=$cookieObject"
        cookieManager.setCookie("https://web.reptimate.store/", cookieString)

        // 동기화
        cookieManager.flush()

        webView.addJavascriptInterface(WebAppInterface(requireActivity()), "Android")

        // URL에 데이터 추가하여 로딩
        val url = "https://web.reptimate.store/auction"
        webView.loadUrl(url)

        swipeRefreshLayout.setOnRefreshListener {

            webView.reload()

            swipeRefreshLayout.isRefreshing = false
        }

        return rootView
    }

    private class WebAppInterface(private val context: Context) {
        @JavascriptInterface
        fun openNativeActivity(idx: String, streamKey: String) {
            // 네이티브 액티비티를 시작하는 코드
            Log.d("android activity start : ", "Let's go <LiveActivity!!>")
            val intent = Intent(context, LiveActivity::class.java)
            intent.putExtra("idx", idx)
            intent.putExtra("streamKey", streamKey)
            context.startActivity(intent)
        }
    }

    // onActivityResult 오버라이드하여 파일 선택 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        myWebChromeClient.handleActivityResult(requestCode, resultCode, data)
    }

    // 뒤로 가기 동작을 처리할 함수
    fun goBack(): Boolean {
        if (webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return false
    }
}
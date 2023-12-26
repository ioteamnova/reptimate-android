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
import org.json.JSONObject

class BoardFragment : Fragment() {
    @SuppressLint("SetJavaScriptEnabled")
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var webView: WebView
    private lateinit var sharedPreferences: SharedPreferences // SharedPreferences 객체 추가
    private lateinit var myWebChromeClient: MyWebChromeClient

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

        myWebChromeClient = MyWebChromeClient(this@BoardFragment) // 웹뷰 로그를 확인 및 모바일 디바이스로 웹뷰에 사진을 불러올 수 있는 기능이 추가된 메서드

        sharedPreferences = requireContext().getSharedPreferences("user_token", Context.MODE_PRIVATE) // 쉐어드에서 토큰이 저장되어 있는 파일을 불러온다.

        // WebView 설정
        webView.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true // 자바스크립트 사용 가능 (안드로이드와 웹의 통신을 위해 필수)
            settings.domStorageEnabled = true// 안드로이드에서 웹뷰 웹사이트의 스토리지에 접근할 수 있는 권한 부여
            settings.allowFileAccess = true // 파일 접근 권한 부여
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true
            settings.useWideViewPort = true // Use wide viewport
            webView.webChromeClient = myWebChromeClient // 아까 설정한 메서드 연결
        }

        val cookieManager = CookieManager.getInstance() // 웹뷰에 쿠키를 전달하기 위한 쿠키 매니저
        cookieManager.setAcceptCookie(true) // 쿠키 허용
        val accessToken = sharedPreferences.getString("accessToken", "") // 쉐어드의 액세스 토큰
        val idx = sharedPreferences.getString("idx", "") // 쉐어드의 유저 idx
        val refreshToken = sharedPreferences.getString("refreshToken", "") // 쉐어드의 리프레시 토큰
        val nickname = sharedPreferences.getString("nickname", "") // 쉐어드의 유저 닉네임
        val profilePath = sharedPreferences.getString("profilePath", "") // 쉐어드의 유저 프로필 사진 경로

        // json 형태의 쿠키 오브젝트 변수 설정 및 쿠키 오브젝트에 쉐어드 값들을 넣는다.
        val cookieObject = JSONObject()
        cookieObject.put("accessToken", accessToken)
        cookieObject.put("idx", idx)
        cookieObject.put("refreshToken", refreshToken)
        cookieObject.put("nickname", nickname)
        cookieObject.put("profilePath", profilePath)

        Log.d("1 : ", "myAppCookie=$cookieObject")

        val cookieString = "myAppCookie=$cookieObject" // 쿠키를 넣기 위한 문자열 생성 (myAppCookie는 웹내 코드에서 쿠키를 불러오기 위한 키값)
        cookieManager.setCookie("https://web.reptimate.store/", cookieString) // 쿠키를 세팅해준다. 이로써 해당 url뿐만 아니라 해당 도메인에 연결된 모든 url에 쿠키를 사용할 수 있게 된다.

        // 쿠키 동기화
        cookieManager.flush()

        webView.addJavascriptInterface(WebAppInterface(requireActivity()), "Android")

        // URL에 데이터 추가하여 로딩
        val url = "https://web.reptimate.store/community/adoption"
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
        }
    }

    // onActivityResult 오버라이드하여 파일 선택 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        myWebChromeClient.handleActivityResult(requestCode, resultCode, data)
    }

    // 뒤로 가기 동작을 처리할 함수
    // 만약 웹뷰 내에서 뒤로 갈 페이지가 있으면 그 페이지로 가고, 없으면 앱의 액티비티를 종료시긴킨다.
    fun goBack(): Boolean {
        if (webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return false
    }
}
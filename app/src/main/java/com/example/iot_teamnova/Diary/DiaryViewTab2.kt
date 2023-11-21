package com.example.iot_teamnova.Diary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iot_teamnova.PreferenceUtil
import com.example.iot_teamnova.R
import com.example.iot_teamnova.Retrofit.GetResult
import com.example.iot_teamnova.User.MainActivity
import com.example.iot_teamnova.databinding.FragDiaryViewDiaryBinding
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log

class DiaryViewTab2 : Fragment(){

    var currentPage = 1

    var itemList = ArrayList<DiaryItem>()

    lateinit var diaryAdapter: DiaryAdapter

    var existsNextPage: String = "false"

    private var _binding: FragDiaryViewDiaryBinding? = null
    private val binding get() = _binding!!
    private val api = APIS.create()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 1. 뷰 바인딩 설정
        _binding = FragDiaryViewDiaryBinding.inflate(inflater, container, false)

        val getIdx = arguments?.getString("idx")

        binding.writeBtn.setOnClickListener {
            val intent = Intent(binding.root.context, DiaryWriteActivity::class.java)
            intent.putExtra("idx", getIdx)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Fragment 레이아웃 뷰 반환
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        currentPage = 1

        itemList = mutableListOf<DiaryItem>() as ArrayList<DiaryItem>

        val getIdx = arguments?.getString("idx")

        loadItems(getIdx)
    }

    fun loadItems(getIdx : String?) {
        api.get_diary_list(getIdx,currentPage).enqueue(object : Callback<GetResult> {
            override fun onResponse(call: Call<GetResult>, response: Response<GetResult>) {
                Log.d("log",response.toString())
                Log.d("body_log", response.body().toString())
                if (response.isSuccessful) {
                    // Request was successful
                    // Handle the response body here
                    val responseBody = response.body()
                    if (responseBody != null) {
                        try {
                            val jsonObject = response.body()?.result
                            val pageSize = jsonObject?.get("pageSize").toString().replace("\"","") // 페이지 당 아이템 개수
                            val totalCount = jsonObject?.get("totalCount").toString().replace("\"","") // 총 아이템 개수
                            val totalPage = jsonObject?.get("totalPage").toString().replace("\"","") // 총 페이지
                            existsNextPage = jsonObject?.get("existsNextPage").toString().replace("\"","") // 다음 페이지 존재 여부 true/false
                            val items = jsonObject?.get("items").toString().replace("^\"|\"$".toRegex(),"") // 펫 목록 배열
                            Log.d("itemList : ", items.toString())

                            binding.diaryRv.apply {
                                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                        super.onScrolled(recyclerView, dx, dy)

                                        // 리사이클러뷰 가장 마지막 index
                                        val lastPosition =
                                            (recyclerView.layoutManager as LinearLayoutManager?)?.findLastCompletelyVisibleItemPosition()

                                        // 받아온 리사이클러 뷰 카운트
                                        val totalCount = recyclerView.adapter!!.itemCount

                                        // 스크롤을 맨 끝까지 했을 때
                                        if (lastPosition == totalCount - 1) {
                                            if(existsNextPage == "true"){
                                                loadMoreItems()
                                            }

                                        }
                                    }
                                })
                            }

                            val array = JSONArray(items)

                            //traversing through all the object
                            for (i in 0 until array.length()) {
                                val item = array.getJSONObject(i)
                                val idx = item.getString("idx")
                                val title = item.getString("title")
                                val content = item.getString("content")
                                val imagePath = item.getString("imagePath")
                                val date = item.getString("createdAt").split("T")

                                var dateStr = date[0].split("-")

                                val diary = DiaryItem(idx, title, content, imagePath, dateStr[0], dateStr[1] + "/" + dateStr[2],getIdx.toString())

                                itemList.add(diary)
                            }

                            diaryAdapter = DiaryAdapter(binding.root.context, itemList)
                            diaryAdapter.notifyDataSetChanged()

                            binding.diaryRv.adapter = diaryAdapter
                            binding.diaryRv.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)

                            if(itemList.isEmpty()) {
                                binding.emptyTextView.visibility = View.VISIBLE
                                binding.diaryRv.visibility = View.GONE
                            } else {
                                binding.emptyTextView.visibility = View.GONE
                                binding.diaryRv.visibility = View.VISIBLE
                            }

                        } catch(e: JSONException){
                            e.printStackTrace()
                        }
                    }
                    else {
                        // Handle the case where response.body() is null
                        Log.d("토큰 결과 : ", "서버와의 오류가 발생하였습니다.")
                    }
                }
                else if (response.code() == 401) {
                    Log.d("토큰 결과 : ", "401에러. 토큰 재발급 시도.")
                    PreferenceUtil(binding.root.context).checkToken { success ->
                        if (success) {
                            loadItems(getIdx)
                        } else {
                            // Handle the case where token check failed
                            Log.d("토큰 결과 : ", "끝 너 멈춰.")
                        }
                    }
                }
                else if (response.code() == 404) {
                    Log.d("토큰 결과 : ", "404에러. 토큰 재발급 시도.")
                    PreferenceUtil(binding.root.context).checkToken { success ->
                        if (success) {
                            loadItems(getIdx)
                        } else {
                            // Handle the case where token check failed
                            Log.d("토큰 결과 : ", "끝 너 멈춰.")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<GetResult>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }

    private fun loadMoreItems() {
        // Increment the current page number
        currentPage++

        val getIdx = arguments?.getString("idx")

        api.get_diary_list(getIdx, currentPage).enqueue(object : Callback<GetResult> {
            override fun onResponse(call: Call<GetResult>, response: Response<GetResult>) {
                Log.d("log",response.toString())
                Log.d("body_log", response.body().toString())
                if (response.isSuccessful) {
                    // Request was successful
                    // Handle the response body here
                    val responseBody = response.body()
                    if (responseBody != null) {
                        try {
                            val jsonObject = response.body()?.result
                            val pageSize = jsonObject?.get("pageSize").toString().replace("\"","") // 페이지 당 아이템 개수
                            val totalCount = jsonObject?.get("totalCount").toString().replace("\"","") // 총 아이템 개수
                            val totalPage = jsonObject?.get("totalPage").toString().replace("\"","") // 총 페이지
                            existsNextPage = jsonObject?.get("existsNextPage").toString().replace("\"","") // 다음 페이지 존재 여부 true/false
                            val items = jsonObject?.get("items").toString().replace("^\"|\"$".toRegex(),"") // 펫 목록 배열
                            Log.d("itemList : ", items.toString())

                            val array = JSONArray(items)

                            //traversing through all the object
                            for (i in 0 until array.length()) {
                                val item = array.getJSONObject(i)
                                val idx = item.getString("idx")
                                val title = item.getString("title")
                                val content = item.getString("content")
                                val imagePath = item.getString("imagePath")
                                val date = item.getString("createdAt").split("T")

                                var dateStr = date[0].split("-")


                                val diary = DiaryItem(idx, title, content, imagePath, dateStr[0], dateStr[1] + "/" + dateStr[2], getIdx.toString())

                                itemList.add(diary)
                            }

                            diaryAdapter.notifyDataSetChanged()

                        } catch(e: JSONException){
                            e.printStackTrace()
                        }
                    }
                    else {
                        // Handle the case where response.body() is null
                        Log.d("토큰 결과 : ", "서버와의 오류가 발생하였습니다.")
                    }
                }
                    else if (response.code() == 401) {
                    Log.d("토큰 결과 : ", "401에러. 토큰 재발급 시도.")
                    PreferenceUtil(binding.root.context).checkToken { success ->
                        if (success) {
                            loadItems(getIdx)
                        } else {
                            // Handle the case where token check failed
                            Log.d("토큰 결과 : ", "끝 너 멈춰.")
                        }
                    }
                }
                else if (response.code() == 404) {
                    Log.d("토큰 결과 : ", "404에러. 토큰 재발급 시도.")
                    PreferenceUtil(binding.root.context).checkToken { success ->
                        if (success) {
                            loadItems(getIdx)
                        } else {
                            // Handle the case where token check failed
                            Log.d("토큰 결과 : ", "끝 너 멈춰.")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<GetResult>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }
}
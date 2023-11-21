package com.reptimate.iot_teamnova.Scheduling

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.Retrofit.GetResult
import com.reptimate.iot_teamnova.databinding.ActivityScheduleScheduleBinding
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScheduleViewTab2 : Fragment(){ // 일정
    var currentPage = 1

    var itemList = ArrayList<ScheduleItem>()

    lateinit var scheduleAdapter: ScheduleAdapter

    var existsNextPage: String = "false"

    private var _binding: ActivityScheduleScheduleBinding? = null
    private val binding get() = _binding!!
    private val api = APIS.create()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 1. 뷰 바인딩 설정
        _binding = ActivityScheduleScheduleBinding.inflate(inflater, container, false)

        binding.writeBtn.setOnClickListener {
            val intent = Intent(binding.root.context, ScheduleWriteActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Fragment 레이아웃 뷰 반환
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        itemList = mutableListOf<ScheduleItem>() as ArrayList<ScheduleItem>

        api.get_schedule_list(currentPage).enqueue(object : Callback<GetResult> {
            override fun onResponse(call: Call<GetResult>, response: Response<GetResult>) {
                Log.d("log",response.toString())
                Log.d("body_log", response.body().toString())
                try {
                    val jsonObject = response.body()?.result
                    val pageSize = jsonObject?.get("pageSize").toString().replace("\"","") // 페이지 당 아이템 개수
                    val totalCount = jsonObject?.get("totalCount").toString().replace("\"","") // 총 아이템 개수
                    val totalPage = jsonObject?.get("totalPage").toString().replace("\"","") // 총 페이지
                    existsNextPage = jsonObject?.get("existsNextPage").toString().replace("\"","") // 다음 페이지 존재 여부 true/false
                    val items = jsonObject?.get("items").toString().replace("^\"|\"$".toRegex(),"") // 펫 목록 배열
                    Log.d("itemList : ", items.toString())

                    binding.scheduleRv.apply {
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
                        val alarmTime = item.getString("alarmTime")
                        val repeat = item.getString("repeatDay")
                        val memo = item.getString("memo")

                        val schedule = ScheduleItem(idx, title, alarmTime, repeat, memo)

                        itemList.add(schedule)
                    }

                    scheduleAdapter = ScheduleAdapter(binding.root.context, itemList)
                    scheduleAdapter.notifyDataSetChanged()

                    binding.scheduleRv.adapter = scheduleAdapter
                    binding.scheduleRv.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)

                    if(itemList.isEmpty()) {
                        binding.emptyTextView.visibility = View.VISIBLE
                        binding.scheduleRv.visibility = View.GONE
                    } else {
                        binding.emptyTextView.visibility = View.GONE
                        binding.scheduleRv.visibility = View.VISIBLE
                    }

                } catch(e: JSONException){
                    e.printStackTrace()
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

        api.get_schedule_list(currentPage).enqueue(object : Callback<GetResult> {
            override fun onResponse(call: Call<GetResult>, response: Response<GetResult>) {
                Log.d("log",response.toString())
                Log.d("body_log", response.body().toString())
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
                        val alarmTime = item.getString("alarmTime")
                        val repeat = item.getString("repeatDay")
                        val memo = item.getString("memo")

                        val schedule = ScheduleItem(idx, title, alarmTime, repeat, memo)

                        itemList.add(schedule)
                    }

                    scheduleAdapter.notifyDataSetChanged()

                } catch(e: JSONException){
                    e.printStackTrace()
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
package com.reptimate.iot_teamnova.Diary

import APIS
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reptimate.iot_teamnova.Cage.CommitData
import com.reptimate.iot_teamnova.PreferenceUtil
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.Retrofit.GetResult
import com.reptimate.iot_teamnova.Retrofit.GetYearResult
import com.reptimate.iot_teamnova.databinding.FragDiaryViewWeightBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class DiaryViewTab1 : Fragment(){

    var currentPage = 1

    var itemList = ArrayList<WeightItem>()

    var graphList = ArrayList<CommitData>()

    lateinit var weightAdapter: WeightAdapter

    var existsNextPage: String = "false"

    private var _binding: FragDiaryViewWeightBinding? = null
    private val binding get() = _binding!!
    private val api = APIS.create()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 1. 뷰 바인딩 설정
        _binding = FragDiaryViewWeightBinding.inflate(inflater, container, false)

        val getIdx = arguments?.getString("idx")

        binding.writeBtn.setOnClickListener {
            val intent = Intent(binding.root.context, WeightWriteActivity::class.java)
            intent.putExtra("idx", getIdx)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Fragment 레이아웃 뷰 반환
        return binding.root
    }

    fun changeDateText(dataList: List<CommitData>): List<String> {
        val dataTextList = ArrayList<String>()
        for (i in dataList.indices) {
            dataTextList.add(dataList[i].date)
        }
        return dataTextList
    }

    class XAxisCustomFormatter(val xAxisData: List<String>) : ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            return xAxisData[(value).toInt()]
        }

    }

    override fun onResume() {
        super.onResume()

        currentPage = 1

        val getIdx = arguments?.getString("idx")

        loadWeek(getIdx)

        binding.weekBtn.setOnClickListener {
            loadWeek(getIdx)
        }

        binding.monthBtn.setOnClickListener {
            loadMonth(getIdx)
        }

        binding.yearBtn.setOnClickListener {
            loadYear(getIdx)
        }

        loadItems(getIdx)
    }

    fun loadWeek(getIdx : String?) {
        binding.weekBtn.setTextColor(Color.parseColor("#6D71E6"))
        binding.weekBtn.setBackgroundResource(R.drawable.background_left_ok)

        binding.monthBtn.setTextColor(Color.parseColor("#000000"))
        binding.monthBtn.setBackgroundResource(R.drawable.background_center)

        binding.yearBtn.setTextColor(Color.parseColor("#000000"))
        binding.yearBtn.setBackgroundResource(R.drawable.background_right)

        graphList = mutableListOf<CommitData>() as ArrayList<CommitData>

        api.get_weight_list(getIdx,1, 20,"week").enqueue(object : Callback<GetResult> {
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
                                val weight = item.getInt("weight")
                                val date = item.getString("date")

                                var dateStr = date.split("-")

                                val commitData = CommitData(dateStr[1] + "/" + dateStr[2], weight)

                                graphList.add(commitData)

                            }
                            val xAxis = binding.lineChart.xAxis

                            //데이터 가공
                            //y축
                            val entries: MutableList<Entry> = mutableListOf()
                            for (i in graphList.indices) {
                                entries.add(Entry(i.toFloat(), graphList[i].commitNum.toFloat()))
                            }
                            val lineDataSet = LineDataSet(entries, "entries")
                            if(graphList.size > 0) {
                                lineDataSet.apply {
                                    color = Color.parseColor("#6D71E6")
                                    circleRadius = 8f
                                    circleHoleRadius = 4f
                                    lineWidth = 1f
                                    setCircleColor(Color.parseColor("#6D71E6"))
                                    circleHoleColor = Color.parseColor("#FFFFFF")
                                    setDrawHighlightIndicators(false)
                                    // 원에 텍스트 표시
                                    setDrawValues(false) // 숫자표시
                                    valueTextColor = Color.parseColor("#6D71E6")
                                    valueFormatter = DefaultValueFormatter(0)  // 소숫점 자릿수 설정
                                    valueTextSize = 10f
                                }

                                //차트 전체 설정
                                binding.lineChart.apply {
                                    axisRight.isEnabled = false   //y축 사용여부
                                    axisLeft.isEnabled = true
                                    axisLeft.setDrawGridLines(true)
                                    axisRight.setDrawGridLines(true)
                                    legend.isEnabled = false    //legend 사용여부
                                    description.isEnabled = false //주석
                                    isDragXEnabled = true   // x 축 드래그 여부
                                    isScaleYEnabled = false //y축 줌 사용여부
                                    isScaleXEnabled = false //x축 줌 사용여부
                                }

                                //X축 설정
                                xAxis.apply {
                                    spaceMin = (7 - entries.size).toFloat()
                                    setDrawGridLines(true)
                                    setDrawAxisLine(true)
                                    setDrawLabels(true)
                                    position = XAxis.XAxisPosition.BOTTOM
                                    valueFormatter =
                                        IndexAxisValueFormatter(changeDateText(graphList))
                                    textColor = resources.getColor(R.color.black, null)
                                    textSize = 10f
                                    labelRotationAngle = 0f
                                    setLabelCount(entries.size, false)
                                }

                                binding.lineChart.apply {
                                    data = LineData(lineDataSet)
                                    notifyDataSetChanged() //데이터 갱신
                                    invalidate() // view갱신
                                }
                            }
                            else {
                                binding.lineChart.apply {
                                    clear()
                                    setNoDataText("등록된 데이터가 없습니다.") // Set the custom message
                                    setNoDataTextColor(Color.LTGRAY)
                                    invalidate()
                                }
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
                            loadWeek(getIdx)
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
                            loadWeek(getIdx)
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

    fun loadMonth(getIdx : String?) {
        binding.weekBtn.setTextColor(Color.parseColor("#000000"))
        binding.weekBtn.setBackgroundResource(R.drawable.background_left)

        binding.monthBtn.setTextColor(Color.parseColor("#6D71E6"))
        binding.monthBtn.setBackgroundResource(R.drawable.background_center_ok)

        binding.yearBtn.setTextColor(Color.parseColor("#000000"))
        binding.yearBtn.setBackgroundResource(R.drawable.background_right)

        graphList = mutableListOf<CommitData>() as ArrayList<CommitData>

        api.get_weight_list(getIdx,1, 30,"month").enqueue(object : Callback<GetResult> {
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
                                val weight = item.getInt("weight")
                                val date = item.getString("date")

                                var dateStr = date.split("-")

                                val commitData = CommitData(dateStr[1] + "/" + dateStr[2], weight)

                                graphList.add(commitData)

                            }
                            val xAxis = binding.lineChart.xAxis

                            //데이터 가공
                            //y축
                            val entries: MutableList<Entry> = mutableListOf()
                            for (i in graphList.indices) {
                                entries.add(Entry(i.toFloat(), graphList[i].commitNum.toFloat()))
                            }
                            val lineDataSet = LineDataSet(entries, "entries")
                            if(graphList.size > 0) {
                                lineDataSet.apply {
                                    color = Color.parseColor("#6D71E6")
                                    circleRadius = 8f
                                    circleHoleRadius = 4f
                                    lineWidth = 1f
                                    setCircleColor(Color.parseColor("#6D71E6"))
                                    circleHoleColor = Color.parseColor("#FFFFFF")
                                    setDrawHighlightIndicators(false)
                                    // 원에 텍스트 표시
                                    setDrawValues(false) // 숫자표시
                                    valueTextColor = Color.parseColor("#6D71E6")
                                    valueFormatter = DefaultValueFormatter(0)  // 소숫점 자릿수 설정
                                    valueTextSize = 10f
                                }

                                //차트 전체 설정
                                binding.lineChart.apply {
                                    axisRight.isEnabled = false   //y축 사용여부
                                    axisLeft.isEnabled = true
                                    axisLeft.setDrawGridLines(true)
                                    axisRight.setDrawGridLines(true)
                                    legend.isEnabled = false    //legend 사용여부
                                    description.isEnabled = false //주석
                                    isDragXEnabled = true   // x 축 드래그 여부
                                    isScaleYEnabled = false //y축 줌 사용여부
                                    isScaleXEnabled = false //x축 줌 사용여부
                                }

                                //X축 설정
                                xAxis.apply {
                                    if(entries.size < 9) {
                                        spaceMin = (12 - entries.size).toFloat()
                                        labelCount = kotlin.math.floor(entries.size.toDouble() / 1.2).toInt()
                                    }
                                    else if(entries.size < 16) {
                                        spaceMin = (20 - entries.size).toFloat()
                                        labelCount = kotlin.math.floor(entries.size.toDouble() / 1.5).toInt()
                                    }
                                    else {
                                        spaceMin = (30 - entries.size).toFloat()
                                        labelCount = kotlin.math.floor(entries.size.toDouble() / 2.0).toInt()
                                    }
                                    setDrawGridLines(true)
                                    setDrawAxisLine(true)
                                    setDrawLabels(true)
                                    position = XAxis.XAxisPosition.BOTTOM
                                    valueFormatter =
                                        IndexAxisValueFormatter(changeDateText(graphList))
                                    textColor = resources.getColor(R.color.black, null)
                                    textSize = 10f
                                    labelRotationAngle = 0f
                                    setLabelCount(entries.size, false)
                                }

                                binding.lineChart.apply {
                                    data = LineData(lineDataSet)
                                    notifyDataSetChanged() //데이터 갱신
                                    invalidate() // view갱신
                                }
                            }
                            else {
                                binding.lineChart.apply {
                                    clear()
                                    setNoDataText("등록된 데이터가 없습니다.") // Set the custom message
                                    setNoDataTextColor(Color.LTGRAY)
                                    invalidate()
                                }
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
                            loadMonth(getIdx)
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
                            loadMonth(getIdx)
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

    fun loadYear(getIdx: String?) {
        binding.weekBtn.setTextColor(Color.parseColor("#000000"))
        binding.weekBtn.setBackgroundResource(R.drawable.background_left)

        binding.monthBtn.setTextColor(Color.parseColor("#000000"))
        binding.monthBtn.setBackgroundResource(R.drawable.background_center)

        binding.yearBtn.setTextColor(Color.parseColor("#6D71E6"))
        binding.yearBtn.setBackgroundResource(R.drawable.background_right_ok)

        graphList = mutableListOf<CommitData>() as ArrayList<CommitData>

        api.post_weight_list(getIdx,1, 20,"year").enqueue(object : Callback<GetYearResult> {
            override fun onResponse(call: Call<GetYearResult>, response: Response<GetYearResult>) {
                Log.d("log",response.toString())
                Log.d("body_log", response.body().toString())
                if (response.isSuccessful) {
                    // Request was successful
                    // Handle the response body here
                    val responseBody = response.body()
                    if (responseBody != null) {
                        try {
                            val jsonObject = response.body()?.result.toString().replace("^\"|\"$".toRegex(),"")
                            Log.d("itemList : ", jsonObject.toString())

                            val array = JSONArray(jsonObject)

                            //traversing through all the object
                            for (i in 0 until array.length()) {
                                val item = array.getJSONObject(i)
                                val month = item.getString("month")
                                val average = item.getInt("average")

                                val commitData = CommitData(month, average)

                                graphList.add(commitData)
                            }
                            val xAxis = binding.lineChart.xAxis

                            //데이터 가공
                            //y축
                            val entries: MutableList<Entry> = mutableListOf()
                            for (i in graphList.indices) {
                                entries.add(Entry(i.toFloat(), graphList[i].commitNum.toFloat()))
                            }
                            val lineDataSet = LineDataSet(entries, "entries")
                            if(graphList.size > 0) {
                                lineDataSet.apply {
                                    color = Color.parseColor("#6D71E6")
                                    circleRadius = 8f
                                    circleHoleRadius = 4f
                                    lineWidth = 1f
                                    setCircleColor(Color.parseColor("#6D71E6"))
                                    circleHoleColor = Color.parseColor("#FFFFFF")
                                    setDrawHighlightIndicators(false)
                                    // 원에 텍스트 표시
                                    setDrawValues(false) // 숫자표시
                                    valueTextColor = Color.parseColor("#6D71E6")
                                    valueFormatter = DefaultValueFormatter(0)  // 소숫점 자릿수 설정
                                    valueTextSize = 10f
                                }

                                //차트 전체 설정
                                binding.lineChart.apply {
                                    axisRight.isEnabled = false   //y축 사용여부
                                    axisLeft.isEnabled = true
                                    axisLeft.setDrawGridLines(true)
                                    axisRight.setDrawGridLines(true)
                                    legend.isEnabled = false    //legend 사용여부
                                    description.isEnabled = false //주석
                                    isDragXEnabled = true   // x 축 드래그 여부
                                    isScaleYEnabled = false //y축 줌 사용여부
                                    isScaleXEnabled = false //x축 줌 사용여부
                                }

                                //X축 설정
                                xAxis.apply {
                                    spaceMin = if(entries.size < 7) {
                                        (8 - entries.size).toFloat()
                                    } else {
                                        (12 - entries.size).toFloat()
                                    }
                                    setDrawGridLines(true)
                                    setDrawAxisLine(true)
                                    setDrawLabels(true)
                                    position = XAxis.XAxisPosition.BOTTOM
                                    valueFormatter =
                                        IndexAxisValueFormatter(changeDateText(graphList))
                                    textColor = resources.getColor(R.color.black, null)
                                    textSize = 10f
                                    labelRotationAngle = 0f
                                    setLabelCount(entries.size, false)
                                }

                                binding.lineChart.apply {
                                    data = LineData(lineDataSet)
                                    notifyDataSetChanged() //데이터 갱신
                                    invalidate() // view갱신
                                }
                            }
                            else {
                                binding.lineChart.apply {
                                    clear()
                                    setNoDataText("등록된 데이터가 없습니다.") // Set the custom message
                                    setNoDataTextColor(Color.LTGRAY)
                                    invalidate()
                                }
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
                            loadYear(getIdx)
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
                            loadYear(getIdx)
                        } else {
                            // Handle the case where token check failed
                            Log.d("토큰 결과 : ", "끝 너 멈춰.")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<GetYearResult>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }

    fun loadItems(getIdx: String?) {
        itemList = mutableListOf<WeightItem>() as ArrayList<WeightItem>

        api.get_weight_list(getIdx,currentPage, 20,"default", "DESC").enqueue(object : Callback<GetResult> {
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

                            binding.weightRv.apply {
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
                                val weight = item.getInt("weight")
                                val date = item.getString("date")
                                var weightChange = item.getString("weightChange")

                                val weightItem = WeightItem(idx, weight.toString(), convertDateFormat(date), weightChange, getIdx.toString())

                                itemList.add(weightItem)

                            }

                            if(itemList.size > 0) {
                                binding.currentWeight.text = array.getJSONObject(0).getString("weight")
                            }


                            weightAdapter = WeightAdapter(binding.root.context, itemList)
                            weightAdapter.notifyDataSetChanged()

                            binding.weightRv.adapter = weightAdapter
                            binding.weightRv.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)

                            if(itemList.isEmpty()) {
                                binding.emptyTextView.visibility = View.VISIBLE
                                binding.weightRv.visibility = View.GONE
                            } else {
                                binding.emptyTextView.visibility = View.GONE
                                binding.weightRv.visibility = View.VISIBLE
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

        api.get_weight_list(getIdx,currentPage, 20,"default", "DESC").enqueue(object : Callback<GetResult> {
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
                                val weight = item.getInt("weight")
                                val date = item.getString("date")
                                var weightChange = item.getString("weightChange")

                                val weightItem = WeightItem(idx, weight.toString(), convertDateFormat(date), weightChange, getIdx.toString())

                                itemList.add(weightItem)

                            }

                            weightAdapter.notifyDataSetChanged()

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
                    loadMoreItems()
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
                    loadMoreItems()
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

    fun convertDateFormat(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yy.MM.dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date)
    }
}
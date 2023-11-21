package com.example.iot_teamnova.Scheduling

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iot_teamnova.R
import com.example.iot_teamnova.Retrofit.GetResult
import com.example.iot_teamnova.Retrofit.GetScheduleResult
import com.example.iot_teamnova.databinding.ActivityScheduleCalendarBinding
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class   ScheduleViewTab1  : Fragment(){ // 달력
    lateinit var array : JSONArray

    var selectedDate = ""

    var itemList = ArrayList<CalendarItem>()

    var dotList = ArrayList<CalendarItem>()

    lateinit var scheduleAdapter: CalendarScheduleAdapter

    var existsNextPage: String = "false"

    private var _binding: ActivityScheduleCalendarBinding? = null
    private val binding get() = _binding!!
    private val api = APIS.create()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 1. 뷰 바인딩 설정
        _binding = ActivityScheduleCalendarBinding.inflate(inflater, container, false)

        val (year, month, day) = getCurrentDate()
        selectedDate = String.format("%04d-%02d-%02d", year, month, day)

        binding.writeBtn.setOnClickListener {
            val intent = Intent(binding.root.context, CalendarScheduleWriteActivity::class.java)
            intent.putExtra("date", selectedDate)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Fragment 레이아웃 뷰 반환
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        binding.writeBtn.visibility = View.VISIBLE

        // Calendar 인스턴스 생성
        val calendar = Calendar.getInstance()

        // 현재 날짜로 Calendar를 설정
        var currentYear = calendar.get(Calendar.YEAR)
        var currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(currentYear, currentMonth, currentDay)

        // CalendarView에 선택된 날짜 설정
        binding.calendar.setSelectedDate(calendar)
        binding.calendar.currentDate = CalendarDay.from(currentYear, currentMonth, 1)

//        Handler(Looper.getMainLooper()).postDelayed({
//            // Set the current date again with smooth scrolling to show the current month on the screen
//            binding.calendar.setCurrentDate(CalendarDay.from(currentYear, currentMonth, 1), true)
//        }, 200)

        val (year, month, day) = getCurrentDate()
        selectedDate = String.format("%04d-%02d-%02d", year, month, day)

        itemList = mutableListOf<CalendarItem>() as ArrayList<CalendarItem>

        api.get_calendar_schedule_list(selectedDate).enqueue(object : Callback<GetScheduleResult> {
            override fun onResponse(call: Call<GetScheduleResult>, response: Response<GetScheduleResult>) {
                Log.d("log",response.toString())
                Log.d("body_log", response.body().toString())
                try {
                    val jsonObject = response.body()?.result.toString()
                    Log.d("itemList : ", jsonObject.toString())

                    array = JSONArray(jsonObject)

                    //traversing through all the object
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        val idx = item.getString("idx")
                        val date = item.getString("date")
                        val title = item.getString("title")
                        val alarmTime = item.getString("alarmTime")
                        val memo = item.getString("memo")

                        val schedule = CalendarItem(idx, date, title, alarmTime, memo)

                        dotList.add(schedule)

                        if(date == selectedDate) {
                            val schedule = CalendarItem(idx, date, title, alarmTime, memo)

                            itemList.add(schedule)
                        }
                    }

                    val dateList = dotList.map { it.date }

                    val markedDates = hashSetOf<CalendarDay>()

                    dateList.forEach { dateString ->
                        val dateArray = dateString.split("-")
                        val year = dateArray[0].toInt()
                        val month = dateArray[1].toInt() -1
                        val day = dateArray[2].toInt()
                        val calendarDay = CalendarDay.from(year, month, day)
                        markedDates.add(calendarDay)
                    }

                    // Create a decorator with a DotSpan to mark the dates with dots
                    val decorator = EventDecorator(Color.parseColor("#6D71E6"), markedDates)

                    // Add the decorator to the calendar view
                    binding.calendar.addDecorator(decorator)

                    scheduleAdapter = CalendarScheduleAdapter(binding.root.context, itemList)
                    scheduleAdapter.notifyDataSetChanged()

                    binding.calendarRv.adapter = scheduleAdapter
                    binding.calendarRv.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)

                    if(itemList.isEmpty()) {
                        binding.emptyTextView.visibility = View.VISIBLE
                        binding.calendarRv.visibility = View.GONE
                    } else {
                        binding.emptyTextView.visibility = View.GONE
                        binding.calendarRv.visibility = View.VISIBLE
                    }

                } catch(e: JSONException){
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<GetScheduleResult>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })

        binding.calendar.setOnMonthChangedListener { widget, date ->
            // This callback will be triggered whenever the calendar is scrolled to another month
            val year = date?.year ?: 0
            val month = date?.month ?: 0
            println(year)
            println(month + 1)

            val selectedYear = year
            val selectedMonth = month + 1

            val getList = String.format("%04d-%02d-%02d", selectedYear, selectedMonth, 1)
            println(getList)

            println(selectedYear)
            println(currentYear)
            println(selectedMonth)
            println(currentMonth)
            if (selectedYear != currentYear || selectedMonth != currentMonth) {
                // Month changed, do something
                currentMonth = selectedMonth
                // You can update your UI or perform any necessary actions here
                itemList = mutableListOf<CalendarItem>() as ArrayList<CalendarItem>
                dotList = mutableListOf<CalendarItem>() as ArrayList<CalendarItem>

                api.get_calendar_schedule_list(getList).enqueue(object : Callback<GetScheduleResult> {
                    override fun onResponse(call: Call<GetScheduleResult>, response: Response<GetScheduleResult>) {
                        Log.d("log",response.toString())
                        Log.d("body_log", response.body().toString())
                        try {
                            val jsonObject = response.body()?.result.toString()
                            Log.d("itemList : ", jsonObject.toString())

                            array = JSONArray(jsonObject)

                            //traversing through all the object
                            for (i in 0 until array.length()) {
                                val item = array.getJSONObject(i)
                                val idx = item.getString("idx")
                                val date = item.getString("date")
                                val title = item.getString("title")
                                val alarmTime = item.getString("alarmTime")
                                val memo = item.getString("memo")

                                val schedule = CalendarItem(idx, date, title, alarmTime, memo)

                                dotList.add(schedule)

                            }

                            currentYear = date.year
                            currentMonth = date.month + 1

                            val dateList = dotList.map { it.date }

                            val markedDates = hashSetOf<CalendarDay>()

                            dateList.forEach { dateString ->
                                val dateArray = dateString.split("-")
                                val year = dateArray[0].toInt()
                                val month = dateArray[1].toInt() -1
                                val day = dateArray[2].toInt()
                                val calendarDay = CalendarDay.from(year, month, day)
                                markedDates.add(calendarDay)
                            }

                            // Create a decorator with a DotSpan to mark the dates with dots
                            val decorator = EventDecorator(Color.parseColor("#6D71E6"), markedDates)

                            // Add the decorator to the calendar view
                            binding.calendar.addDecorator(decorator)

                        } catch(e: JSONException){
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(call: Call<GetScheduleResult>, t: Throwable) {
                        // 실패
                        Log.d("log",t.message.toString())
                        Log.d("log","fail")
                    }
                })


            }

        }

        binding.calendar.setOnDateChangedListener { _, date, _ ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, date.year)
                set(Calendar.MONTH, date.month)
                set(Calendar.DAY_OF_MONTH, date.day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val currentDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (selectedCalendar.before(currentDate)) {
                // Disable the button
                binding.writeBtn.visibility = View.GONE
            } else {
                // Enable the button
                binding.writeBtn.visibility = View.VISIBLE
            }

            selectedDate = String.format("%04d-%02d-%02d", date.year, date.month + 1, date.day)

            itemList = mutableListOf<CalendarItem>() as ArrayList<CalendarItem>

            //traversing through all the object
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val idx = item.getString("idx")
                val date = item.getString("date")
                val title = item.getString("title")
                val alarmTime = item.getString("alarmTime")
                val memo = item.getString("memo")

                if(date == selectedDate) {
                    val schedule = CalendarItem(idx, date, title, alarmTime, memo)

                    itemList.add(schedule)
                } else {

                }
            }
            scheduleAdapter = CalendarScheduleAdapter(binding.root.context, itemList)
            scheduleAdapter.notifyDataSetChanged()

            binding.calendarRv.adapter = scheduleAdapter
            binding.calendarRv.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)

            if(itemList.isEmpty()) {
                binding.emptyTextView.visibility = View.VISIBLE
                binding.emptyTextView1.visibility = View.GONE
                binding.calendarRv.visibility = View.GONE
            }

            if(itemList.isEmpty() && selectedCalendar.before(currentDate)) {
                binding.emptyTextView.visibility = View.GONE
                binding.emptyTextView1.visibility = View.VISIBLE
                binding.calendarRv.visibility = View.GONE
            }

            if(itemList.isNotEmpty()) {
                binding.emptyTextView.visibility = View.GONE
                binding.emptyTextView1.visibility = View.GONE
                binding.calendarRv.visibility = View.VISIBLE
            }

        }
    }

    fun getCurrentDate(): Triple<Int, Int, Int> {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return Triple(year, month, day)
    }

    class EventDecorator(private val color: Int, private val dates: HashSet<CalendarDay>) :
        DayViewDecorator {

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            view.addSpan(DotSpan(5f, color)) // Adjust the dot size as needed
        }
    }
}
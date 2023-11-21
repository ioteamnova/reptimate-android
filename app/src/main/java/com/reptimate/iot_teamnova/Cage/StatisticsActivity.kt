package com.reptimate.iot_teamnova.Cage

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.databinding.ActivityStatisticsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.*
import kotlin.collections.ArrayList

class StatisticsActivity : AppCompatActivity() {
    var dateString = ""
    //데이터 생성
    val dataList: List<CommitData> = listOf(
        CommitData("10:22",3),
        CommitData("11:10",2),
        CommitData("11:50",5),
        CommitData("13:30",2),
        CommitData("13:56",3),
        CommitData("17:55",6),
        CommitData("18:09",7),
        CommitData("19:11",1),
        CommitData("21:18",3),
        CommitData("21:57",2)
    )

    private val binding by lazy { ActivityStatisticsBinding.inflate(layoutInflater) }
    private val api = APIS.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val intent: Intent = intent
        val getTitle = intent.getStringExtra("title")

        binding.title.text = getTitle

        binding.date.setOnClickListener {
            val cal = Calendar.getInstance()    //캘린더뷰 만들기
            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                var monthStr = (month+1).toString()
                var dayStr = dayOfMonth.toString()
                if((month+1) < 10) {
                    monthStr = "0$monthStr"
                }
                if(dayOfMonth < 10) {
                    dayStr = "0$dayStr"
                }
                dateString = "${year}-$monthStr-$dayStr"
                binding.date.text = dateString
            }
            DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(
                Calendar.DAY_OF_MONTH)).show()
        }

        val xAxis = binding.lineChart.xAxis

        //데이터 가공
        //y축
        val entries: MutableList<Entry> = mutableListOf()
        for (i in dataList.indices){
            entries.add(Entry(0f, dataList[i].commitNum.toFloat()))
        }
        val lineDataSet = LineDataSet(entries,"entries")

        lineDataSet.apply {
            color = resources.getColor(R.color.black, null)
            circleRadius = 5f
            lineWidth = 3f
            setCircleColor(resources.getColor(R.color.purple_700, null))
            circleHoleColor = resources.getColor(R.color.purple_700, null)
            setDrawHighlightIndicators(false)
            setDrawValues(true) // 숫자표시
            valueTextColor = resources.getColor(R.color.black, null)
            valueFormatter = DefaultValueFormatter(0)  // 소숫점 자릿수 설정
            valueTextSize = 10f
        }

        //차트 전체 설정
        binding.lineChart.apply {
            axisRight.isEnabled = false   //y축 사용여부
            axisLeft.isEnabled = false
            legend.isEnabled = false    //legend 사용여부
            description.isEnabled = false //주석
            isDragXEnabled = true   // x 축 드래그 여부
            isScaleYEnabled = false //y축 줌 사용여부
            isScaleXEnabled = false //x축 줌 사용여부
        }

        //X축 설정
        xAxis.apply {
            setDrawGridLines(false)
            setDrawAxisLine(true)
            setDrawLabels(true)
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = XAxisCustomFormatter(changeDateText(dataList))
            textColor = resources.getColor(R.color.black, null)
            textSize = 10f
            labelRotationAngle = 0f
            setLabelCount(10, true)
        }

        binding.horizontalScrollView.post{
            binding.horizontalScrollView.scrollTo(
                0,
                0
            )
        }

        binding.lineChart.apply {
            data = LineData(lineDataSet)
            notifyDataSetChanged() //데이터 갱신
            invalidate() // view갱신
        }

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

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}
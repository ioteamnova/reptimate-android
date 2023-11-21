package com.reptimate.iot_teamnova.Diary

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter

class GraphAxisValueFormatter(private val mValues: Array<String>) : IAxisValueFormatter {

    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        return mValues[value.toInt()]
    }
}
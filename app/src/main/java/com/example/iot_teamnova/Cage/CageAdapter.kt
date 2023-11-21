package com.example.iot_teamnova.Cage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.iot_teamnova.R


class CageAdapter(val context: Context, val itemList: ArrayList<CageItem>) :
    RecyclerView.Adapter<CageAdapter.CageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CageAdapter.CageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cage, parent, false)
        return CageViewHolder(view)
    }

    override fun onBindViewHolder(holder: CageAdapter.CageViewHolder, position: Int) {

        val displayMetrics = DisplayMetrics()
        (holder.itemView.context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        var deviceWidth = displayMetrics.widthPixels // 핸드폰의 가로 해상도를 구함.
        // int deviceHeight = displayMetrics.heightPixels;  // 핸드폰의 세로 해상도를 구함.
        deviceWidth /= 9
        deviceWidth *= 4
        val deviceHeight = (deviceWidth).toInt() // 세로의 길이를 가로의 길이의 1.5배로 하고 싶었다.

        holder.itemView.layoutParams.height = deviceHeight // 아이템 뷰의 세로 길이를 구한 길이로 변경
        holder.itemView.layoutParams.width = deviceWidth
        holder.itemView.requestLayout() // 변경 사항 적용


        val idx = itemList[position].idx
        val cageName = itemList[position].cageName
        val boardTempname = itemList[position].boardTempname
        val currentUvbLight = itemList[position].currentUvbLight
        val currentHeatingLight = itemList[position].currentHeatingLight
        val autoChkLight = itemList[position].autoChkLight
        val autoChkTemp = itemList[position].autoChkTemp
        val autoChkHumid = itemList[position].autoChkHumid
        val currentTemp = itemList[position].currentTemp
        val currentTemp2 = itemList[position].currentTemp2
        val maxTemp = itemList[position].maxTemp
        val minTemp = itemList[position].minTemp
        val currentHumid = itemList[position].currentHumid
        val currentHumid2 = itemList[position].currentHumid2
        val maxHumid = itemList[position].maxHumid
        val minHumid = itemList[position].minHumid
        val autoLightUtctimeOn = itemList[position].autoLightUtctimeOn
        val autoLightUtctimeOff = itemList[position].autoLightUtctimeOff

        holder.name.text = cageName
        holder.temperature.text = currentTemp
        holder.humidity.text = currentHumid

        if(autoChkTemp == "1") {
            holder.lamp.text = "ON"
        }
        if(autoChkTemp == "0") {
            holder.lamp.text = "OFF"
        }

        if(autoChkLight == "1") {
            holder.UVB.text = "ON"
        }
        if(autoChkLight == "0") {
            holder.UVB.text = "OFF"
        }

        holder.item.setOnClickListener {
            val i = Intent(context, CageViewActivity::class.java)
            i.putExtra("idx", idx)
            i.putExtra("cageName", cageName)
            i.putExtra("boardTempname", boardTempname)
            i.putExtra("currentUvbLight", currentUvbLight)
            i.putExtra("currentHeatingLight", currentHeatingLight)
            i.putExtra("autoChkLight", autoChkLight)
            i.putExtra("autoChkTemp", autoChkTemp)
            i.putExtra("autoChkHumid", autoChkHumid)
            i.putExtra("currentTemp", currentTemp)
            i.putExtra("currentTemp2", currentTemp2)
            i.putExtra("maxTemp", maxTemp)
            i.putExtra("minTemp", minTemp)
            i.putExtra("currentHumid", currentHumid)
            i.putExtra("currentHumid2", currentHumid2)
            i.putExtra("maxHumid", maxHumid)
            i.putExtra("minHumid", minHumid)
            i.putExtra("autoLightUtctimeOn", autoLightUtctimeOn)
            i.putExtra("autoLightUtctimeOff", autoLightUtctimeOff)
            context.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class CageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.name)
        val temperature = itemView.findViewById<TextView>(R.id.temperature)
        val humidity = itemView.findViewById<TextView>(R.id.humidity)
        val UVB = itemView.findViewById<TextView>(R.id.UVB)
        val lamp = itemView.findViewById<TextView>(R.id.lamp)

        val item = itemView.findViewById<ConstraintLayout>(R.id.item)
    }

    fun updateItemData(itemData: CageItem) {
        val index = itemList.indexOfFirst { it.boardTempname == itemData.boardTempname }
        if (index != -1) {
            itemList[index] = itemData
            notifyItemChanged(index)
        }
    }
}
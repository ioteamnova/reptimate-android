package com.reptimate.iot_teamnova.Scheduling

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reptimate.iot_teamnova.R

class ScheduleAdapter(val context: Context, val itemList: ArrayList<ScheduleItem>) :
    RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val idx = itemList[position].idx
        val title = itemList[position].title
        val alarmTime = itemList[position].alarmTime
        val repeat = itemList[position].repeat
        val memo = itemList[position].memo
        val formattedDays = getFormattedDaysOfWeek(repeat)

        holder.title.text = title // 이름
        holder.time.text = alarmTime // 시간
        holder.repeat.text = formattedDays // 요일(반복)

        holder.item.setOnClickListener {
            val i = Intent(context, ScheduleEditActivity::class.java)
            i.putExtra("idx", idx)
            i.putExtra("title", title)
            i.putExtra("alarmTime", alarmTime)
            i.putExtra("repeat", repeat)
            i.putExtra("memo", memo)
            context.startActivity(i)
        }

        holder.menu.setOnClickListener{
            val popupMenu = PopupMenu(context, it)
            popupMenu.menuInflater.inflate(R.menu.popup1, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.action_menu1) {
                    val i = Intent(context, ScheduleDeleteDialog::class.java)
                    i.putExtra("idx", idx)
                    context.startActivity(i)
                }
                false
            }
            popupMenu.show()
        }

    }

    override fun getItemCount(): Int {
        return itemList.size
    }


    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.title)
        val repeat = itemView.findViewById<TextView>(R.id.repeat)
        val time = itemView.findViewById<TextView>(R.id.time)
        val menu = itemView.findViewById<ImageView>(R.id.menu_btn)

        val item = itemView.findViewById<LinearLayout>(R.id.schedule_item)
    }

    fun getFormattedDaysOfWeek(daysOfWeek: String): String {
        val days = listOf("일", "월", "화", "수", "목", "금", "토")
        val selectedDays = mutableListOf<String>()

        // Split the input string into an array of integers
        val dayArray = daysOfWeek.split(",").map { it.toInt() }

        // Loop through the dayArray and identify selected days
        for (i in dayArray.indices) {
            if (dayArray[i] == 1) {
                selectedDays.add(days[i])
            }
        }

        return when {
            selectedDays.isEmpty() -> "안 함"
            selectedDays.size == 7 -> "매일"
            selectedDays.size == 5 && selectedDays.containsAll(listOf("월", "화", "수", "목", "금")) -> "주중"
            selectedDays.size == 2 && selectedDays.containsAll(listOf("토", "일")) -> "주말"
            selectedDays.size == 1 && selectedDays.contains("일") -> "일요일마다"
            selectedDays.size == 1 && selectedDays.contains("월") -> "월요일마다"
            selectedDays.size == 1 && selectedDays.contains("화") -> "화요일마다"
            selectedDays.size == 1 && selectedDays.contains("수") -> "수요일마다"
            selectedDays.size == 1 && selectedDays.contains("목") -> "목요일마다"
            selectedDays.size == 1 && selectedDays.contains("금") -> "금요일마다"
            selectedDays.size == 1 && selectedDays.contains("토") -> "토요일마다"
            else -> selectedDays.joinToString(separator = ", ")
        }
    }
}
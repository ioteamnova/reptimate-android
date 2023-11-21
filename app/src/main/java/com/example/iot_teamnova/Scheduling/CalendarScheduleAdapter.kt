package com.example.iot_teamnova.Scheduling

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
import com.example.iot_teamnova.R

class CalendarScheduleAdapter(val context: Context, val itemList: ArrayList<CalendarItem>) :
    RecyclerView.Adapter<CalendarScheduleAdapter.CalendarScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_schedule, parent, false)
        return CalendarScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarScheduleViewHolder, position: Int) {
        val idx = itemList[position].idx
        val date = itemList[position].date
        val title = itemList[position].title
        val alarmTime = itemList[position].alarmTime
        val memo = itemList[position].memo

        holder.title.text = title // 이름
        holder.time.text = alarmTime // 시간
//        holder.date.text = date // 날짜

        holder.item.setOnClickListener {
            val i = Intent(context, CalendarScheduleEditActivity::class.java)
            i.putExtra("idx", idx)
            i.putExtra("date", date)
            i.putExtra("title", title)
            i.putExtra("alarmTime", alarmTime)
            i.putExtra("memo", memo)
            context.startActivity(i)
        }

        holder.menu.setOnClickListener{
            val popupMenu = PopupMenu(context, it)
            popupMenu.menuInflater.inflate(R.menu.popup1, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.action_menu1) {
                    val i = Intent(context, CalendarScheduleDeleteDialog::class.java)
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


    inner class CalendarScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.title)
        val repeat = itemView.findViewById<TextView>(R.id.repeat)
        val date = itemView.findViewById<TextView>(R.id.date)
        val time = itemView.findViewById<TextView>(R.id.time)
        val menu = itemView.findViewById<ImageView>(R.id.menu_btn)

        val item = itemView.findViewById<LinearLayout>(R.id.schedule_item)
    }
}
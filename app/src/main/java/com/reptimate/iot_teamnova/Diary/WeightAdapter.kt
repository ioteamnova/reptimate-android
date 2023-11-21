package com.reptimate.iot_teamnova.Diary

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reptimate.iot_teamnova.R

class WeightAdapter(val context: Context, val itemList: ArrayList<WeightItem>) :
    RecyclerView.Adapter<WeightAdapter.WeightViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightAdapter.WeightViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weight, parent, false)
        return WeightViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeightAdapter.WeightViewHolder, position: Int) {
        val idx = itemList[position].idx
        val weight = itemList[position].weight
        val date = itemList[position].date
        val gap = itemList[position].gap
        val petIdx = itemList[position].petIdx

        holder.date.text = date

        holder.weight.text = weight + "g"

        holder.gap.text = gap + "g"

        if(gap == "0") {
            // 변화량 값이 0 일 때
        }
        if(gap != "0") {
            holder.gap.setTextColor(Color.parseColor("#FF0000"))
        }
        if(gap.startsWith("-")) {
            holder.gap.setTextColor(Color.parseColor("#0000FF"))
        }

        holder.menu_btn.setOnClickListener {
            val popupMenu = PopupMenu(context, it)
            popupMenu.menuInflater.inflate(R.menu.popup, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.action_menu1) {
                    val i = Intent(context, WeightEditActivity::class.java)
                    i.putExtra("idx", idx)
                    i.putExtra("weight", weight)
                    i.putExtra("date", date)
                    i.putExtra("gap", gap)
                    i.putExtra("petIdx", petIdx)
                    context.startActivity(i)
                } else if (menuItem.itemId == R.id.action_menu2) {
                    val i = Intent(context, WeightDeleteDialog::class.java)
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

    inner class WeightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val weight = itemView.findViewById<TextView>(R.id.weight)
        val date = itemView.findViewById<TextView>(R.id.date)
        val gap = itemView.findViewById<TextView>(R.id.gap)
        val menu_btn = itemView.findViewById<ImageView>(R.id.menu_btn)

        val item = itemView.findViewById<LinearLayout>(R.id.layout)
    }
}
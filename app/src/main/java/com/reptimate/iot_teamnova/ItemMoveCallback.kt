package com.reptimate.iot_teamnova

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.reptimate.iot_teamnova.Diary.ItemTouchHelperListener

class ItemMoveCallback(private val listener: ItemTouchHelperListener): ItemTouchHelper.Callback() {
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        // 드래그 방향
        val dragFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        // 스와이프 방향
        val swipeFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN

        // 드래그 이동을 만드는 함수
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    /** 아이템이 움직일떼 호출되는 함수 */
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        listener.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        return false
    }

    /** 아이템이 스와이프 될때 호출되는 함수 */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//        TODO("Not yet implemented")
    }

}
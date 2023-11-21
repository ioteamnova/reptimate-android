package com.reptimate.iot_teamnova.Diary

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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.reptimate.iot_teamnova.R

class PetAdapter(val context: Context, private var itemList: ArrayList<PetItem>) :
    RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetAdapter.PetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pet, parent, false)
        return PetViewHolder(view)
    }

    fun setItems(list: ArrayList<PetItem>) {
        itemList = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = itemList[position]

        with(holder) {
            name.text = pet.name
            type.text = pet.type

            when (pet.gender) {
                "MALE" -> {
                    gender.text = "수컷"
                    gender.setBackgroundResource(R.drawable.male_background)
                }
                "FEMALE" -> {
                    gender.text = "암컷"
                    gender.setBackgroundResource(R.drawable.female_background)
                }
                "NONE" -> {
                    gender.text = "미구분"
                    gender.setBackgroundResource(R.drawable.neutral_background)
                }
            }

            if (pet.profile.isNotBlank() && pet.profile != "null") {
//                holder.bind(pet.profile)
                Glide.with(context)
                    .load(pet.profile)
                    .centerCrop()
                    .override(130, 130)
                    .into(image)

                image.clipToOutline = true
            } else {
                Glide.with(context)
                    .load(R.drawable.reptimate_logo)
                    .override(130, 130)
                    .into(image)
            }

            item.setOnClickListener {
                val intent = Intent(context, PetViewActivity::class.java).apply {
                    putExtra("idx", pet.idx)
                    putExtra("name", pet.name)
                    putExtra("type", pet.type)
                    putExtra("gender", pet.gender)
                    putExtra("birthDate", pet.birthDate)
                    putExtra("adoptionDate", pet.adoptionDate)
                    putExtra("profile", pet.profile)
                }
                context.startActivity(intent)
            }

            menu_btn.setOnClickListener {
                val popupMenu = PopupMenu(context, it)
                popupMenu.menuInflater.inflate(R.menu.popup, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_menu1 -> {
                            val intent = Intent(context, PetEditActivity::class.java).apply {
                                putExtra("idx", pet.idx)
                                putExtra("name", pet.name)
                                putExtra("type", pet.type)
                                putExtra("gender", pet.gender)
                                putExtra("birthDate", pet.birthDate)
                                putExtra("adoptionDate", pet.adoptionDate)
                                putExtra("profile", pet.profile)
                            }
                            context.startActivity(intent)
                        }
                        R.id.action_menu2 -> {
                            val intent = Intent(context, PetDeleteDialog::class.java).apply {
                                putExtra("idx", pet.idx)
                            }
                            context.startActivity(intent)
                        }
                    }
                    false
                }
                popupMenu.show()
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image)
        val name: TextView = itemView.findViewById(R.id.name)
        val type: TextView = itemView.findViewById(R.id.type)
        val gender: TextView = itemView.findViewById(R.id.gender)
        val menu_btn: ImageView = itemView.findViewById(R.id.menu_btn)
        val item: LinearLayout = itemView.findViewById(R.id.pet_item)

        fun bind(imageUrl: String) {
            Glide.with(itemView)
                .load(imageUrl)
                .centerCrop()
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(image)
        }
    }
}
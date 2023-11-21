package com.reptimate.iot_teamnova.Scheduling

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.databinding.ActivityRepeatBinding

class RepeatActivity : AppCompatActivity() {
    var sunday = "0"
    var monday = "0"
    var tuesday = "0"
    var wednesday = "0"
    var thursday = "0"
    var friday = "0"
    var saturday = "0"

    var repeat = "$sunday,$monday,$tuesday,$wednesday,$thursday,$friday,$saturday"

    private val binding by lazy { ActivityRepeatBinding.inflate(layoutInflater) }
    private val api = APIS.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //뒤로가기(back) 버튼 클릭 시
        binding.backBtn.setOnClickListener{
            finish()
        }

        val intent: Intent = getIntent()
        repeat = intent.getStringExtra("repeat").toString()

        sunday = repeat.split(",")[0]
        monday = repeat.split(",")[1]
        tuesday = repeat.split(",")[2]
        wednesday = repeat.split(",")[3]
        thursday = repeat.split(",")[4]
        friday = repeat.split(",")[5]
        saturday = repeat.split(",")[6]

        binding.checkBox1.isChecked = (sunday == "1")
        binding.checkBox2.isChecked = (monday == "1")
        binding.checkBox3.isChecked = (tuesday == "1")
        binding.checkBox4.isChecked = (wednesday == "1")
        binding.checkBox5.isChecked = (thursday == "1")
        binding.checkBox6.isChecked = (friday == "1")
        binding.checkBox7.isChecked = (saturday == "1")

        binding.sunday.setOnClickListener {
            binding.checkBox1.isChecked = !binding.checkBox1.isChecked
        }
        binding.monday.setOnClickListener {
            binding.checkBox2.isChecked = !binding.checkBox2.isChecked
        }
        binding.tuesday.setOnClickListener {
            binding.checkBox3.isChecked = !binding.checkBox3.isChecked
        }
        binding.wednesday.setOnClickListener {
            binding.checkBox4.isChecked = !binding.checkBox4.isChecked
        }
        binding.thursday.setOnClickListener {
            binding.checkBox5.isChecked = !binding.checkBox5.isChecked
        }
        binding.friday.setOnClickListener {
            binding.checkBox6.isChecked = !binding.checkBox6.isChecked
        }
        binding.saturday.setOnClickListener {
            binding.checkBox7.isChecked = !binding.checkBox7.isChecked
        }

        binding.confirmBtn.setOnClickListener {
            sunday = if (binding.checkBox1.isChecked) {
                "1" // Checkbox is checked
            } else {
                "0" // Checkbox is unchecked
            }
            monday = if (binding.checkBox2.isChecked) {
                "1" // Checkbox is checked
            } else {
                "0" // Checkbox is unchecked
            }
            tuesday = if (binding.checkBox3.isChecked) {
                "1" // Checkbox is checked
            } else {
                "0" // Checkbox is unchecked
            }
            wednesday = if (binding.checkBox4.isChecked) {
                "1" // Checkbox is checked
            } else {
                "0" // Checkbox is unchecked
            }
            thursday = if (binding.checkBox5.isChecked) {
                "1" // Checkbox is checked
            } else {
                "0" // Checkbox is unchecked
            }
            friday = if (binding.checkBox6.isChecked) {
                "1" // Checkbox is checked
            } else {
                "0" // Checkbox is unchecked
            }
            saturday = if (binding.checkBox7.isChecked) {
                "1" // Checkbox is checked
            } else {
                "0" // Checkbox is unchecked
            }

            repeat = "$sunday,$monday,$tuesday,$wednesday,$thursday,$friday,$saturday"
            val resultIntent = Intent()
            resultIntent.putExtra("repeat", repeat)
            setResult(Activity.RESULT_OK, resultIntent)
            finish() // Finish Activity B
        }
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
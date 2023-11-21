package com.reptimate.iot_teamnova.Cage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.reptimate.iot_teamnova.Retrofit.GetResult
import com.reptimate.iot_teamnova.databinding.ActivityCageBinding
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CageActivity : AppCompatActivity(), MqttService.MqttCallbackListener, MqttManager.MqttManagerCallback {
    private lateinit var mqttManager: MqttManager
    private lateinit var mqttServiceConnection: MqttServiceConnection
    private lateinit var mqttService: MqttService

    var currentPage = 1
    var itemList = ArrayList<CageItem>()
    lateinit var cageAdapter: CageAdapter
    var existsNextPage: String = "false"

    private val binding by lazy { ActivityCageBinding.inflate(layoutInflater) }
    private val api = APIS.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // MqttService와 연결하기 위한 ServiceConnection 객체 생성
        mqttServiceConnection = MqttServiceConnection()

        // MqttService와 바인딩
        val intent = Intent(this, MqttService::class.java)
        bindService(intent, mqttServiceConnection, Context.BIND_AUTO_CREATE)

        // MqttService 인스턴스 가져오기
        mqttService = MqttService.getInstance()!!

        // MqttService에 MqttCallbackListener 등록
        mqttService?.setMqttCallbackListener(this)

        // MqttManager 인스턴스 생성
        mqttManager = MqttManager(applicationContext, "ssl://43.201.185.236:8883", MqttClient.generateClientId())

        // MqttManager에 콜백 설정
        mqttManager.setCallback(this)

        mqttManager.connect()

        binding.writeBtn.setOnClickListener {
            val intent = Intent(applicationContext, CageWriteActivity::class.java)
            startActivity(intent)
        }

        binding.refreshBtn.setOnClickListener {
            // 온습도 값 수동 가져오기
            val message = "{\"userIdx\":\"30\",\"boardTempname\":\"KR_B1\",\"type\":\"2\"}"
            mqttServiceConnection.getService()?.mqttManager?.publish("temphumid/getrequest/nest", message)
        }

    }

    // MqttManagerCallback의 메서드 구현
    override fun onMessageReceived(topic: String, message: String) {
        // 메시지 도착 시 UI 업데이트 수행
        println("topic $topic")
        println(message)
        println("리사이클러뷰 데이터 리세팅 매니저")
        if (topic == "30/KR_B1/temphumid/getresponse/app") {
            val messageString = message.toString() // Convert the payload to a string
            val jsonObject = JSONObject(messageString) // Convert the string to a JSONObject
            val currentTemp = jsonObject.getString("currentTemp")
            val currentHumid = jsonObject.getString("currentHumid")
            val currentTemp2 = jsonObject.getString("currentTemp2")
            val currentHumid2 = jsonObject.getString("currentHumid2")
            println(currentTemp)
            println(currentHumid)
            println(currentTemp2)
            println(currentHumid2)
            runOnUiThread {
                var itemToUpdate: CageItem? = null
                for (item in itemList) {
                    if (item.boardTempname == "KR_B1") {
                        itemToUpdate = item
                        break
                    }
                }
                if (itemToUpdate != null) {
                    val newItemData = CageItem(
                        itemToUpdate.idx,
                        itemToUpdate.cageName,
                        itemToUpdate.boardTempname,
                        itemToUpdate.currentUvbLight,
                        itemToUpdate.currentHeatingLight,
                        itemToUpdate.autoChkLight,
                        itemToUpdate.autoChkTemp,
                        itemToUpdate.autoChkHumid,
                        currentTemp,
                        currentTemp2,
                        itemToUpdate.maxTemp,
                        itemToUpdate.minTemp,
                        currentHumid,
                        currentHumid2,
                        itemToUpdate.maxHumid,
                        itemToUpdate.minHumid,
                        itemToUpdate.autoLightUtctimeOn,
                        itemToUpdate.autoLightUtctimeOff
                    )
                    cageAdapter.updateItemData(newItemData)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mqttServiceConnection)
    }

    // MqttCallbackListener의 콜백 메서드
    override fun onMqttMessageReceived(topic: String, message: MqttMessage) {
        println("topic $topic")
        println(message)
        println("리사이클러뷰 데이터 리세팅 콜백")
        if (topic == "30/KR_B1/temphumid/getresponse/app") {
            val messageString = message.toString() // Convert the payload to a string
            val jsonObject = JSONObject(messageString) // Convert the string to a JSONObject
            val currentTemp = jsonObject.getString("currentTemp")
            val currentHumid = jsonObject.getString("currentHumid")
            val currentTemp2 = jsonObject.getString("currentTemp2")
            val currentHumid2 = jsonObject.getString("currentHumid2")
            println(currentTemp)
            println(currentHumid)
            println(currentTemp2)
            println(currentHumid2)
            runOnUiThread {
                var itemToUpdate: CageItem? = null
                for (item in itemList) {
                    if (item.boardTempname == "KR_B1") {
                        itemToUpdate = item
                        break
                    }
                }
                if (itemToUpdate != null) {
                    val newItemData = CageItem(
                        itemToUpdate.idx,
                        itemToUpdate.cageName,
                        itemToUpdate.boardTempname,
                        itemToUpdate.currentUvbLight,
                        itemToUpdate.currentHeatingLight,
                        itemToUpdate.autoChkLight,
                        itemToUpdate.autoChkTemp,
                        itemToUpdate.autoChkHumid,
                        currentTemp,
                        currentTemp2,
                        itemToUpdate.maxTemp,
                        itemToUpdate.minTemp,
                        currentHumid,
                        currentHumid2,
                        itemToUpdate.maxHumid,
                        itemToUpdate.minHumid,
                        itemToUpdate.autoLightUtctimeOn,
                        itemToUpdate.autoLightUtctimeOff
                    )
                    cageAdapter.updateItemData(newItemData)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        currentPage = 1

        itemList = mutableListOf<CageItem>() as ArrayList<CageItem>

        api.get_cage_list(currentPage).enqueue(object : Callback<GetResult> {
            override fun onResponse(call: Call<GetResult>, response: Response<GetResult>) {
                Log.d("log",response.toString())
                Log.d("body_log", response.body().toString())
                try {
                    val jsonObject = response.body()?.result
                    val pageSize = jsonObject?.get("pageSize").toString().replace("\"","") // 페이지 당 아이템 개수
                    val totalCount = jsonObject?.get("totalCount").toString().replace("\"","") // 총 아이템 개수
                    val totalPage = jsonObject?.get("totalPage").toString().replace("\"","") // 총 페이지
                    existsNextPage = jsonObject?.get("existsNextPage").toString().replace("\"","") // 다음 페이지 존재 여부 true/false
                    val items = jsonObject?.get("items").toString().replace("^\"|\"$".toRegex(),"") // 펫 목록 배열
                    Log.d("itemList : ", items.toString())

//                    binding.cageRv.apply {
//                        addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                                super.onScrolled(recyclerView, dx, dy)
//
//                                // 리사이클러뷰 가장 마지막 index
//                                val lastPosition =
//                                    (recyclerView.layoutManager as LinearLayoutManager?)?.findLastCompletelyVisibleItemPosition()
//
//                                // 받아온 리사이클러 뷰 카운트
//                                val totalCount = recyclerView.adapter!!.itemCount
//
//                                // 스크롤을 맨 끝까지 했을 때
//                                if (lastPosition == totalCount - 1) {
//                                    if(existsNextPage == "true"){
//                                        loadMoreItems()
//                                    }
//
//                                }
//                            }
//                        })
//                    }

                    val array = JSONArray(items)

                    //traversing through all the object
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        val idx = item.getString("idx")
                        val cageName = item.getString("cageName")
                        val boardTempname = item.getString("boardTempname")
                        val currentUvbLight = item.getString("currentUvbLight")
                        val currentHeatingLight = item.getString("currentHeatingLight")
                        val autoChkLight = item.getString("autoChkLight")
                        val autoChkTemp = item.getString("autoChkTemp")
                        val autoChkHumid = item.getString("autoChkHumid")
                        val currentTemp = item.getString("currentTemp")
                        val currentTemp2 = item.getString("currentTemp2")
                        val maxTemp = item.getString("maxTemp")
                        val minTemp = item.getString("minTemp")
                        val currentHumid = item.getString("currentHumid")
                        val currentHumid2 = item.getString("currentHumid2")
                        val maxHumid = item.getString("maxHumid")
                        val minHumid = item.getString("minHumid")
                        val autoLightUtctimeOn = item.getString("autoLightUtctimeOn")
                        val autoLightUtctimeOff = item.getString("autoLightUtctimeOff")

                        val cage = CageItem(idx, cageName, boardTempname, currentUvbLight, currentHeatingLight,
                            autoChkLight, autoChkTemp, autoChkHumid, currentTemp, currentTemp2,
                            maxTemp, minTemp, currentHumid, currentHumid2, maxHumid, minHumid, autoLightUtctimeOn, autoLightUtctimeOff)

                        itemList.add(cage)
                    }

                    cageAdapter = CageAdapter(binding.root.context, itemList)
//                    val itemTouchHelper = ItemTouchHelper(SwipeController(petAdapter))
//                    itemTouchHelper.attachToRecyclerView(binding.petRv)
                    cageAdapter.notifyDataSetChanged()

                    binding.cageRv.adapter = cageAdapter
                    binding.cageRv.layoutManager = GridLayoutManager(binding.root.context, 2)

                    if(itemList.isEmpty()) {
                        binding.emptyTextView.visibility = View.VISIBLE
                        binding.cageRv.visibility = View.GONE
                    } else {
                        binding.emptyTextView.visibility = View.GONE
                        binding.cageRv.visibility = View.VISIBLE
                    }

                } catch(e: JSONException){
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<GetResult>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }
}
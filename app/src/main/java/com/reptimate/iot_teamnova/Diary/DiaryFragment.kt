package com.reptimate.iot_teamnova.Diary

import APIS
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reptimate.iot_teamnova.PreferenceUtil
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.Retrofit.GetResult
import com.reptimate.iot_teamnova.databinding.FragDiaryBinding
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DiaryFragment : Fragment() {
    var currentPage = 1
    var itemList = ArrayList<PetItem>()
    var searchList = ArrayList<PetItem>()
    lateinit var petAdapter: PetAdapter
    var existsNextPage: String = "false"

    var search_mode = false

    private var _binding: FragDiaryBinding? = null
    private val binding get() = _binding!!
    private val api = APIS.create()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 1. 뷰 바인딩 설정
        _binding = FragDiaryBinding.inflate(inflater, container, false)

        binding.parentLayout.setOnClickListener {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
            requireActivity().currentFocus?.clearFocus()
        }

        binding.writeBtn.setOnClickListener {
            val intent = Intent(binding.root.context, PetWriteActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Fragment 레이아웃 뷰 반환
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        currentPage = 1

        itemList = mutableListOf<PetItem>() as ArrayList<PetItem>

        petAdapter = PetAdapter(binding.root.context, itemList)
        binding.petRv.setHasFixedSize(true)
        binding.petRv.adapter = petAdapter

        binding.petRv.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)

        loadItems(currentPage)
    }

    fun loadItems(page : Int) {
        api.get_pet_list(page).enqueue(object : Callback<GetResult> {
            override fun onResponse(call: Call<GetResult>, response: Response<GetResult>) {
                Log.d("log",response.toString())
                Log.d("body_log", response.body().toString())
                if (response.isSuccessful) {
                    // Request was successful
                    // Handle the response body here
                    val responseBody = response.body()
                    if (responseBody != null) {
                        try {
                            val jsonObject = response.body()?.result
                            val pageSize = jsonObject?.get("pageSize").toString().replace("\"","") // 페이지 당 아이템 개수
                            val totalCount = jsonObject?.get("totalCount").toString().replace("\"","") // 총 아이템 개수
                            val totalPage = jsonObject?.get("totalPage").toString().replace("\"","") // 총 페이지
                            existsNextPage = jsonObject?.get("existsNextPage").toString().replace("\"","") // 다음 페이지 존재 여부 true/false
                            val items = jsonObject?.get("items").toString().replace("^\"|\"$".toRegex(),"") // 펫 목록 배열
                            Log.d("itemList : ", items.toString())

                            binding.petRv.apply {
                                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                        super.onScrolled(recyclerView, dx, dy)

                                        // 리사이클러뷰 가장 마지막 index
                                        val lastPosition =
                                            (recyclerView.layoutManager as LinearLayoutManager?)?.findLastCompletelyVisibleItemPosition()

                                        // 받아온 리사이클러 뷰 카운트
                                        val totalCount = recyclerView.adapter!!.itemCount

                                        // 스크롤을 맨 끝까지 했을 때
                                        if (lastPosition == totalCount - 1) {
                                            if(existsNextPage == "true"){
                                                loadMoreItems()
                                            }

                                        }
                                    }
                                })
                            }

                            val array = JSONArray(items)

                            //traversing through all the objects in the JSON array
                            for (i in 0 until array.length()) {
                                val item = array.getJSONObject(i)
                                val idx = item.getString("idx")
                                val name = item.getString("name")
                                val type = item.getString("type")
                                val gender = item.getString("gender")
                                val birthDate = item.getString("birthDate")
                                val adoptionDate = item.getString("adoptionDate")
                                val imagePath = item.getString("imagePath")

                                val pet = PetItem(idx, name, type, gender, birthDate, adoptionDate, imagePath)
                                itemList.add(pet)
                            }
                            petAdapter.notifyDataSetChanged()

                            if(itemList.isEmpty()) {
                                binding.emptyTextView.visibility = View.VISIBLE
                                binding.petRv.visibility = View.GONE
                            } else {
                                binding.emptyTextView.visibility = View.GONE
                                binding.petRv.visibility = View.VISIBLE
                            }

                            binding.searchBtn.setOnClickListener {
                                if(search_mode) {
                                    search_mode = false
                                    binding.searchLayout.visibility = View.GONE
                                    binding.searchEt.isEnabled = false
                                }
                                else {
                                    search_mode = true
                                    binding.searchLayout.visibility = View.VISIBLE
                                    binding.searchEt.isEnabled = true
                                    val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    imm.showSoftInput(binding.searchEt, InputMethodManager.SHOW_IMPLICIT)
                                }
                            }
                            binding.clearText.setOnClickListener {
                                binding.searchEt.setText("")
                            }

                            binding.searchEt.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                                    // 이전 텍스트 변경 전 호출되는 메서드
                                }

                                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                                    // 텍스트 변경 중 호출되는 메서드
                                }

                                override fun afterTextChanged(editable: Editable) {
                                    val searchText = binding.searchEt.text.toString()
                                    searchList.clear()

                                    if (searchText.isEmpty()) {
                                        petAdapter.setItems(itemList)
                                    } else {
                                        // 검색 단어를 포함하는지 확인
                                        for (a in 0 until itemList.size) {
                                            if (itemList[a].name.toLowerCase().contains(searchText.toLowerCase())) {
                                                searchList.add(itemList[a])
                                            }
                                            petAdapter.setItems(searchList)
                                        }
                                    }
                                }
                            })

                        } catch(e: JSONException){
                            e.printStackTrace()
                        }

                    }
                    else {
                        // Handle the case where response.body() is null
                        Log.d("토큰 결과 : ", "서버와의 오류가 발생하였습니다.")
                    }
                }
                else if (response.code() == 401) {
                    Log.d("토큰 결과 : ", "401에러. 토큰 재발급 시도.")
                    PreferenceUtil(binding.root.context).checkToken { success ->
                        if (success) {
                            currentPage = 1
                            loadItems(currentPage)
                        } else {
                            // Handle the case where token check failed
                            Log.d("토큰 결과 : ", "끝 너 멈춰.")
                        }
                    }
                }
                else if (response.code() == 404) {
                    Log.d("토큰 결과 : ", "404에러. 토큰 재발급 시도.")
                    PreferenceUtil(binding.root.context).checkToken { success ->
                        if (success) {
                            currentPage = 1
                            loadItems(currentPage)
                        } else {
                            // Handle the case where token check failed
                            Log.d("토큰 결과 : ", "끝 너 멈춰.")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<GetResult>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }

    private fun loadMoreItems() {
        // Increment the current page number
        currentPage++

        api.get_pet_list(currentPage).enqueue(object : Callback<GetResult> {
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

                    val array = JSONArray(items)

                    //traversing through all the object
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        val idx = item.getString("idx")
                        val name = item.getString("name")
                        val type = item.getString("type")
                        val gender = item.getString("gender")
                        val birthDate = item.getString("birthDate")
                        val adoptionDate = item.getString("adoptionDate")
                        val imagePath = item.getString("imagePath")

                        val pet = PetItem(idx, name, type, gender, birthDate, adoptionDate, imagePath)

                        itemList.add(pet)
                    }
                    petAdapter.notifyDataSetChanged()

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
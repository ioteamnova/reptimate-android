package com.reptimate.iot_teamnova.Diary

import APIS
import APIS.Companion.createBaseService
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.reptimate.iot_teamnova.customAlbum.CustomAlbumActivity
import com.reptimate.iot_teamnova.ItemMoveCallback
import com.reptimate.iot_teamnova.MainApplication
import com.reptimate.iot_teamnova.ProgressDialog
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.Retrofit.DiaryWriteModel
import com.reptimate.iot_teamnova.Retrofit.PostResult
import com.reptimate.iot_teamnova.databinding.FragDiaryDiaryWriteBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DiaryWriteActivity : AppCompatActivity(), DiaryWriteImageAdapter.OnItemClickListener, DiaryWriteImageAdapter.onItemMoveListener {

    lateinit var getImagePath: String
    var photoUri: Uri? = null

    var bitmap: Bitmap? = null

    val Gallery = 1

    var uriList = ArrayList<Uri>() // 이미지의 uri를 담을 ArrayList 객체
    var fileList = ArrayList<File>() // 이미지 파일 리스트

    var listSize = 0

    private lateinit var adapter: DiaryWriteImageAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    private val binding by lazy { FragDiaryDiaryWriteBinding.inflate(layoutInflater) }
    private val api = APIS.create()

    private lateinit var customProgressDialog: ProgressDialog

    private val activityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val getData: Intent? = result.data
                val data: ArrayList<String>? = getData?.getStringArrayListExtra("data")
                Log.v("main onActivityResult", "onActivityResult 호출")
                if (result.resultCode == RESULT_OK) {
                    Log.v("resultCode == RESULT_OK", "resultCode == RESULT_OK")
                    Log.d("image data", data.toString())

                        if (data == null) {   // 어떤 이미지도 선택하지 않은 경우
                            Toast.makeText(applicationContext, "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show()
                        } else {   // 이미지를 하나라도 선택한 경우
                            if (data.size == 1) {     // 이미지를 하나만 선택한 경우
                                if (listSize == 5) {
                                    Toast.makeText(
                                        applicationContext,
                                        "이미지는 최대 5장까지만 첨부 가능합니다.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Log.e("single choice: ", data.toString())
                                    var imageUri = Uri.parse(data[0])
                                    val file = File(absolutelyPath(imageUri, this@DiaryWriteActivity))
                                    uriList.add(imageUri!!)
                                    fileList.add(file)
                                    photoUri = imageUri

                                }
                            } else {      // 이미지를 여러장 선택한 경우
                                Log.e("clipData", data.size.toString())
                                if (data.size > 5 - listSize) {
                                    Toast.makeText(
                                        applicationContext,
                                        "이미지는 최대 5장까지만 첨부 가능합니다.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else if (data.size > 5) {   // 선택한 이미지가 6장 이상인 경우
                                    Toast.makeText(
                                        applicationContext,
                                        "이미지는 최대 5장까지만 선택 가능합니다.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {   // 선택한 이미지가 1장 이상 5장 이하인 경우
                                    Log.e("TAG", "multiple choice")

                                    for (i in 0 until data.size) {
                                        var imageUri = Uri.parse(data[i]) // 선택한 이미지들의 uri를 가져온다.
                                        val file = File(absolutelyPath(imageUri, this@DiaryWriteActivity))
                                        try {
                                            uriList.add(imageUri!!)
                                            fileList.add(file)
                                            photoUri = imageUri
                                        } catch (e: Exception) {
                                            Log.e("TAG", "File select error", e)
                                        }
                                    }
                                }
                            }
                        }
                        listSize = uriList.size
                        binding.imagesCount.text = "$listSize/5"

                        itemTouchHelper.attachToRecyclerView(binding.diaryImageViewRv)

                        adapter.notifyDataSetChanged()

                        binding.diaryImageViewRv.adapter = adapter
                        binding.diaryImageViewRv.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)


                }

            }
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        adapter = DiaryWriteImageAdapter(applicationContext, uriList, binding.diaryImageViewRv, this, this)

        itemTouchHelper = ItemTouchHelper(ItemMoveCallback(adapter))

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.parentLayout.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            currentFocus?.clearFocus()
        }

        val intent: Intent = getIntent()
        val getIdx = intent.getStringExtra("idx")

        getImagePath = ""

        binding.photoBtn.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    //스토리지 읽기 권한이 허용이면 커스텀 앨범 띄워주기
                    //권한 있을 경우 : PERMISSION_GRANTED
                    //권한 없을 경우 : PERMISSION_DENIED
                    val startCustomAlbum = Intent(this, CustomAlbumActivity::class.java)
                    activityForResult.launch(startCustomAlbum)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    //권한을 명시적으로 거부한 경우 : ture
                    //다시 묻지 않음을 선택한 경우 : false
                    //다이얼로그를 띄워 권한 팝업을 해야하는 이유 및 권한팝업을 허용하여야 접근 가능하다는 사실을 알려줌
                    showPermissionAlertDialog()
                }

                else -> {
                    //권한 요청
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        0
                    )
                }
            }
        }

        binding.confirmBtn.setOnClickListener {//완료 버튼 클릭 시
            binding.confirmBtn.isEnabled = false
            val title = binding.title.text.toString()
            val content = binding.content.text.toString()

            if(title == ""){
                Toast.makeText(applicationContext, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                binding.confirmBtn.isEnabled = true
            }
            else {
                customProgressDialog = ProgressDialog(this)

                customProgressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                customProgressDialog.show()
                if (photoUri == null) {
                    val data =
                        DiaryWriteModel(title, content)
                    api.post_diary_write(getIdx, data).enqueue(object : Callback<PostResult> {
                        override fun onResponse(
                            call: Call<PostResult>,
                            response: Response<PostResult>
                        ) {
                            Log.d("log", response.toString())
                            Log.d("body_log", response.body().toString())
                            if (!response.body().toString().isEmpty()) {
                                if (response.body().toString() != "null") {
                                    Toast.makeText(
                                        applicationContext,
                                        "다이어리 작성이 완료 되었습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.confirmBtn.isEnabled = true
                                    finish()
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "서버와의 오류가 발생하였습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.confirmBtn.isEnabled = true
                                    customProgressDialog.dismiss()
                                }
                            }
                        }

                        override fun onFailure(
                            call: Call<PostResult>,
                            t: Throwable
                        ) {
                            // 실패
                            Log.d("log", t.message.toString())
                            Log.d("log", "fail")
                            binding.confirmBtn.isEnabled = true
                            customProgressDialog.dismiss()
                        }
                    })
                }
                if (photoUri != null) {
                    val parts = fileList.map { file ->
                        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                        MultipartBody.Part.createFormData("files", file.name, requestBody)
                    }

                    Log.d("전송되는 값 : ", parts.toString())

                    sendImage(
                        getIdx,
                        MainApplication.prefs.token,
                        title,
                        content,
                        parts
                    )
                }
            }
        }
    }

    private fun showPermissionAlertDialog() {
        AlertDialog.Builder(this)
            .setTitle("권한 승인이 필요합니다.")
            .setMessage("사진을 선택 하시려면 권한이 필요합니다.")
            .setPositiveButton("허용하기") { _, _ ->
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    0
                )
            }
            .setNegativeButton("취소하기") { _, _ -> }
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //권한 허용클릭
                    //TODO()앨범으로 이동시키기!
                } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //권한 처음으로 거절 했을 경우
                    //한번더 권한 요청
                    showPermissionAlertDialog()
                } else {
                    //권한 두번째로 거절 한 경우 (다시 묻지 않음)
                    //설정 -> 권한으로 이동하는 다이얼로그
                    goSettingActivityAlertDialog()
                }
            }
        }
    }
    private fun goSettingActivityAlertDialog() {
        AlertDialog.Builder(this)
            .setTitle("권한 승인이 필요합니다.")
            .setMessage("앨범에 접근 하기 위한 권한이 필요합니다.\n권한 -> 저장공간 -> 허용")
            .setPositiveButton("허용하러 가기") { _, _ ->
                val goSettingPermission = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                goSettingPermission.data = Uri.parse("package:$packageName")
                startActivity(goSettingPermission)
            }
            .setNegativeButton("취소") { _, _ -> }
            .create()
            .show()
    }


    // 절대경로 변환
    fun absolutelyPath(path: Uri?, context : Context): String {
        var proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        var c: Cursor? = context.contentResolver.query(path!!, proj, null, null, null)
        var index = c?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c?.moveToFirst()

        var result = c?.getString(index!!)

        return result!!
    }

    fun convertBitmapToFile(bitmap: Bitmap): File {
        val newFile = File(applicationContext.filesDir, "picture")
        val out = FileOutputStream(newFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        return newFile
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v("main onActivityResult", "onActivityResult 호출")
        if (resultCode == RESULT_OK) {
            Log.v("resultCode == RESULT_OK", "resultCode == RESULT_OK")
            Log.d("image data", data.toString())
            if (requestCode == Gallery) { // 앨범에서 선택
                Log.v("Pick From Gallery", "requestCode == Pick From Gallery")

                if (data == null) {   // 어떤 이미지도 선택하지 않은 경우
                    Toast.makeText(applicationContext, "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show()
                } else {   // 이미지를 하나라도 선택한 경우
                    if (data.clipData == null) {     // 이미지를 하나만 선택한 경우
                        if (listSize == 5) {
                            Toast.makeText(
                                applicationContext,
                                "이미지는 최대 5장까지만 첨부 가능합니다.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Log.e("single choice: ", data.data.toString())
                            var imageUri = data.data
                            val file = File(absolutelyPath(imageUri, this@DiaryWriteActivity))
                            uriList.add(imageUri!!)
                            fileList.add(file)
                            photoUri = imageUri

                        }
                    } else {      // 이미지를 여러장 선택한 경우
                        val clipData = data.clipData
                        Log.e("clipData", clipData!!.itemCount.toString())
                        if (clipData!!.itemCount > 5 - listSize) {
                            Toast.makeText(
                                applicationContext,
                                "이미지는 최대 5장까지만 첨부 가능합니다.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else if (clipData!!.itemCount > 5) {   // 선택한 이미지가 6장 이상인 경우
                            Toast.makeText(
                                applicationContext,
                                "이미지는 최대 5장까지만 선택 가능합니다.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {   // 선택한 이미지가 1장 이상 5장 이하인 경우
                            Log.e("TAG", "multiple choice")

                            for (i in 0 until clipData!!.itemCount) {
                                var imageUri = clipData!!.getItemAt(i).uri // 선택한 이미지들의 uri를 가져온다.
                                val file = File(absolutelyPath(imageUri, this@DiaryWriteActivity))
                                try {
                                    uriList.add(imageUri!!)
                                    fileList.add(file)
                                    photoUri = imageUri
                                } catch (e: Exception) {
                                    Log.e("TAG", "File select error", e)
                                }
                            }
                        }
                    }
                }
                listSize = uriList.size
                binding.imagesCount.text = "$listSize/5"

                itemTouchHelper.attachToRecyclerView(binding.diaryImageViewRv)

                adapter.notifyDataSetChanged()

                binding.diaryImageViewRv.adapter = adapter
                binding.diaryImageViewRv.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)

//                adapter.setOnItemClickListener(object : DiaryWriteImageAdapter.OnItemClickListener {
//                    override fun onItemClick(v: View?, position: Int) {
//                        listSize = uriList.size - 1
//                        binding.imagesCount.text = "$listSize/5 장"
//                    }
//                })

            }
        }
    }

    // 카메라로 촬영한 이미지를 파일로 저장해준다
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            getImagePath = absolutePath
        }
    }

    //웹서버로 이미지전송
    fun sendImage(petIdx : String?, Authorization : String?, title : String, content : String, files : List<MultipartBody.Part>) {

        val getTitle = RequestBody.create("text/plain".toMediaTypeOrNull(), title)
        val getContent = RequestBody.create("text/plain".toMediaTypeOrNull(), content)


        val service = createBaseService(APIS.Companion.RetrofitDiaryWrite::class.java) //레트로핏 통신 설정
        val call = service.DiaryWrite(petIdx,"Bearer $Authorization", getTitle, getContent, files)!! //통신 API 패스 설정

        call.enqueue(object : Callback<PostResult> {
            override fun onResponse(call: Call<PostResult>, response: Response<PostResult>) {
                Log.d("로그 ", " : $response")
                Toast.makeText(
                    applicationContext,
                    "다이어리 작성이 완료 되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()

            }

            override fun onFailure(call: Call<PostResult>, t: Throwable) {
                Log.d("로그 ",t.message.toString())
                binding.confirmBtn.isEnabled = true
                customProgressDialog.dismiss()
            }
        })
    }

    override fun onItemClick(position: Int, mData: ArrayList<Uri>) {
        runOnUiThread {
//            println(mData)
//            println(uriList)
//            println(fileList)

            uriList = mutableListOf<Uri>() as ArrayList<Uri>
            uriList = mData

            // Remove item from the activity's ArrayList
            fileList.removeAt(position)

            // Notify the adapter that an item has been removed

            listSize = uriList.size
            binding.imagesCount.text = "$listSize/5"

//            println(uriList)
//            println(fileList)
        }
    }

    override fun onItemMove(from: Int, to: Int, mData: ArrayList<Uri>) {
        runOnUiThread {
//            println(mData)
//            println(uriList)
//            println(fileList)

            uriList = mutableListOf<Uri>() as ArrayList<Uri>
            uriList = mData

            // Remove item from the activity's ArrayList
            val item: File = fileList[from]
            fileList.removeAt(from)
            fileList.add(to, item)

//            println(uriList)
//            println(fileList)
        }
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
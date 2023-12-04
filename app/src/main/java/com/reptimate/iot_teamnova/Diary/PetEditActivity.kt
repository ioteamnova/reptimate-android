package com.reptimate.iot_teamnova.Diary

import APIS.Companion.createBaseService
import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.reptimate.iot_teamnova.MainApplication
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.Retrofit.PetWriteModel
import com.reptimate.iot_teamnova.Retrofit.PostResult
import com.reptimate.iot_teamnova.databinding.FragDiaryPetWriteBinding
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

class PetEditActivity : AppCompatActivity() {
    lateinit var gender: String
    var birthDateString = ""
    var adpotionDateString = ""

    lateinit var getProfilePath: String
    var photoUri: Uri? = null

    var bitmap: Bitmap? = null

    val Picture = 1
    val Gallery = 2

    private val binding by lazy { FragDiaryPetWriteBinding.inflate(layoutInflater) }
    private val api = APIS.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.parentLayout.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            currentFocus?.clearFocus()
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.title.text = "반려동물 정보 수정"
        binding.confirmBtn.text = "수정 완료"

        val intent: Intent = intent
        val getIdx = intent.getStringExtra("idx")
        val getName = intent.getStringExtra("name")
        val getType = intent.getStringExtra("type")
        val getGender = intent.getStringExtra("gender")
        val getBirthDate = intent.getStringExtra("birthDate")
        val getAdoptionDate = intent.getStringExtra("adoptionDate")
        val getProfile = intent.getStringExtra("profile")

        if (getProfile != null) {
            getProfilePath = getProfile
        }

        if(getProfilePath != "" && getProfilePath != "null"){ // 프로필이 존재할 때
            Glide.with(applicationContext).load(getProfilePath).override(130, 130)
                .into(binding.profile)
        } else {
            binding.profile.setImageResource(R.drawable.reptimate_logo)
        }

        if(getGender == "MALE"){
            gender = getGender
            binding.maleBtn.setBackgroundResource(R.drawable.male_background_ok)
        }
        if(getGender == "FEMALE"){
            gender = getGender
            binding.femaleBtn.setBackgroundResource(R.drawable.female_background_ok)
        }
        if(getGender == "NONE"){
            gender = getGender
            binding.neutralBtn.setBackgroundResource(R.drawable.neutral_background_ok)
        }

        binding.nameEt.setText(getName)
        binding.typeEt.setText(getType)

        //split 분리된 문자를 담을 배열 선언 실시
        var birthStr = getBirthDate?.split("T")
        var adoptStr = getAdoptionDate?.split("T")

        //for 반복문 수행 실시 (i변수는 0번 인덱스부터 str_data 문자열 길이까지 반복을 수행)
        if (birthStr != null) {
            binding.birthTv.text = birthStr.get(0)
        }
        if(adoptStr != null) {
            binding.adoptionTv.text = adoptStr.get(0)
        }

        binding.maleBtn.setOnClickListener {
            gender = "MALE"
            binding.maleBtn.setBackgroundResource(R.drawable.male_background_ok)
            binding.femaleBtn.setBackgroundResource(R.drawable.female_background)
            binding.neutralBtn.setBackgroundResource(R.drawable.neutral_background)
        }
        binding.femaleBtn.setOnClickListener {
            gender = "FEMALE"
            binding.maleBtn.setBackgroundResource(R.drawable.male_background)
            binding.femaleBtn.setBackgroundResource(R.drawable.female_background_ok)
            binding.neutralBtn.setBackgroundResource(R.drawable.neutral_background)
        }
        binding.neutralBtn.setOnClickListener {
            gender = "NONE"
            binding.maleBtn.setBackgroundResource(R.drawable.male_background)
            binding.femaleBtn.setBackgroundResource(R.drawable.female_background)
            binding.neutralBtn.setBackgroundResource(R.drawable.neutral_background_ok)
        }

        binding.birthTv.setOnClickListener {
            val cal = Calendar.getInstance()    //캘린더뷰 만들기
            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                var monthStr = (month+1).toString()
                var dayStr = dayOfMonth.toString()
                if((month+1) < 10) {
                    monthStr = "0$monthStr"
                }
                if(dayOfMonth < 10) {
                    dayStr = "0$dayStr"
                }
                birthDateString = "${year}-$monthStr-$dayStr"
                binding.birthTv.text = birthDateString
            }
            DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.adoptionTv.setOnClickListener {
            val cal = Calendar.getInstance()    //캘린더뷰 만들기
            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                var monthStr = (month+1).toString()
                var dayStr = dayOfMonth.toString()
                if((month+1) < 10) {
                    monthStr = "0$monthStr"
                }
                if(dayOfMonth < 10) {
                    dayStr = "0$dayStr"
                }
                adpotionDateString = "${year}-$monthStr-$dayStr"
                binding.adoptionTv.text = adpotionDateString
            }
            DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        val basicUri = Uri.parse("android.resource://" + packageName + "/" + R.drawable.reptimate_logo)

        if(getProfilePath != "" && getProfilePath != "null"){ // 프로필이 존재할 때
            Glide.with(applicationContext).load(getProfilePath).override(130, 130)
                .into(binding.profile)
        } else {
            binding.profile.setImageResource(R.drawable.reptimate_logo)
        }

        binding.profile.setOnClickListener{
            //사진 관련 권한 허용
            if (checkSelfPermission(Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
            ) else {
                ActivityCompat.requestPermissions(
                    this@PetEditActivity, arrayOf<String>(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), 0
                )
            }

            val info = arrayOf<CharSequence>("사진 촬영", "사진 앨범 선택", "기본 이미지로 변경")

            val builder = AlertDialog.Builder(this@PetEditActivity)
            builder.setTitle("업로드 이미지 선택")
            builder.setItems(
                info
            ) { dialog, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, Picture)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                    1 -> {
                        // 사진 앨범 선택
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = MediaStore.Images.Media.CONTENT_TYPE
                        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        startActivityForResult(
                            intent,
                            Gallery
                        )
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                    2 -> {
                        // 기본 이미지로 변경
                        photoUri = null
                        bitmap = null
                        getProfilePath = ""
                        Glide.with(this@PetEditActivity).load(R.drawable.reptimate_logo).override(130, 130)
                            .into(binding.profile)
                        Toast.makeText(
                            applicationContext,
                            "기본 이미지로 변경되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                dialog.dismiss()
            }
            builder.show()
        }

        binding.confirmBtn.setOnClickListener {//완료 버튼 클릭 시
            binding.confirmBtn.isEnabled = false

            val name = binding.nameEt.text.toString()
            val type = binding.typeEt.text.toString()
            val birthDate = binding.birthTv.text.toString()
            val adoptionDate = binding.adoptionTv.text.toString()
            val profile = getProfilePath

            Log.d("photoUri : ", photoUri.toString())
            Log.d("bitmap : ", bitmap.toString())

            if(name == ""){
                val builder = AlertDialog.Builder(this)
                builder.setMessage("이름을 입력해주세요.")
                    .setPositiveButton("OK") { dialog, _ ->
                        // OK button clicked
                        dialog.dismiss()
                    }
                val dialog = builder.create()
                dialog.show()
                binding.confirmBtn.isEnabled = true
            }
            else if(type == ""){
                val builder = AlertDialog.Builder(this)
                builder.setMessage("품종을 입력해주세요.")
                    .setPositiveButton("OK") { dialog, _ ->
                        // OK button clicked
                        dialog.dismiss()
                    }
                val dialog = builder.create()
                dialog.show()
                binding.confirmBtn.isEnabled = true
            }
            else if(birthDate == "") {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("출생일을 선택해주세요.")
                    .setPositiveButton("OK") { dialog, _ ->
                        // OK button clicked
                        dialog.dismiss()
                    }
                val dialog = builder.create()
                dialog.show()
                binding.confirmBtn.isEnabled = true
            }
            else if(adoptionDate == "") {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("입양일을 선택해주세요.")
                    .setPositiveButton("OK") { dialog, _ ->
                        // OK button clicked
                        dialog.dismiss()
                    }
                val dialog = builder.create()
                dialog.show()
                binding.confirmBtn.isEnabled = true
            }
            else {
                if (photoUri == null && bitmap == null) {
                    val data =
                        PetWriteModel(name, type, gender, birthDate, adoptionDate, profile)
                    api.PetEdit(getIdx ,data).enqueue(object : Callback<PostResult> {
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
                                        "반려동물 정보 수정이 완료되었습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                } else {
                                    val builder = AlertDialog.Builder(this@PetEditActivity)
                                    builder.setMessage("서버와의 오류가 발생하였습니다.")
                                        .setPositiveButton("OK") { dialog, _ ->
                                            // OK button clicked
                                            dialog.dismiss()
                                        }
                                    val dialog = builder.create()
                                    dialog.show()
                                    binding.confirmBtn.isEnabled = true
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
                        }
                    })
                }
                if (bitmap != null) {
                    val file = convertBitmapToFile(bitmap!!)
                    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    Log.d("TAG : ", file.name)
                    Log.d("전송되는 프로필 값 : ", body.toString())

                    sendImage(getIdx,
                        MainApplication.prefs.token,
                        name,
                        type,
                        gender,
                        birthDate,
                        adoptionDate,
                        body
                    )
                }
                if (photoUri != null) {
                    Log.d("프로필 사진 : ", "변경된 프로필 사진입니다.")
                    Log.d("프로필 사진 경로 : ", photoUri.toString())
                    val file = File(absolutelyPath(photoUri, this@PetEditActivity))
                    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    Log.d("TAG : ", file.name)
                    Log.d("전송되는 프로필 값 : ", body.toString())

                    sendImage(getIdx,
                        MainApplication.prefs.token,
                        name,
                        type,
                        gender,
                        birthDate,
                        adoptionDate,
                        body
                    )
                }
            }
        }
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
            if (requestCode == Picture) { // 사진 촬영
                Log.v("Take A Picture", "requestCode == Take A Picture")

                if (resultCode == RESULT_OK && data?.hasExtra("data")!!) {
                    bitmap = data.extras?.get("data") as Bitmap
                    binding.profile.setImageBitmap(bitmap)
                }

            }
            if (requestCode == Gallery) { // 앨범에서 선택
                Log.v("Pick From Gallery", "requestCode == Pick From Gallery")
                bitmap = null

                var ImageData: Uri? = data?.data
                if (ImageData != null) {
                    photoUri = ImageData
                } else {
                    photoUri == null
                }
                binding.profile.setImageURI(photoUri)
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
            getProfilePath = absolutePath
        }
    }

    //웹서버로 이미지전송
    fun sendImage(getIdx : String?, Authorization : String?, name : String, type : String, gender : String, birthDate : String, adpotionDate : String, file : MultipartBody.Part) {
        val getName = RequestBody.create("text/plain".toMediaTypeOrNull(), name)
        val getType = RequestBody.create("text/plain".toMediaTypeOrNull(), type)
        val getGender = RequestBody.create("text/plain".toMediaTypeOrNull(), gender)
        val getBirth = RequestBody.create("text/plain".toMediaTypeOrNull(), birthDate)
        val getAdpot = RequestBody.create("text/plain".toMediaTypeOrNull(), adpotionDate)


        val service = createBaseService(APIS.Companion.RetrofitPetEdit::class.java) //레트로핏 통신 설정
        val call = service.PetEdit(getIdx, "Bearer $Authorization", getName, getType, getGender, getBirth, getAdpot, file)!! //통신 API 패스 설정

        call.enqueue(object : Callback<PostResult> {
            override fun onResponse(call: Call<PostResult>, response: Response<PostResult>) {
                Log.d("로그 ", " : $response")
                Toast.makeText(
                    applicationContext,
                    "반려동물 정보 수정이 완료되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }

            override fun onFailure(call: Call<PostResult>, t: Throwable) {
                Log.d("로그 ",t.message.toString())
                binding.confirmBtn.isEnabled = true
            }
        })
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
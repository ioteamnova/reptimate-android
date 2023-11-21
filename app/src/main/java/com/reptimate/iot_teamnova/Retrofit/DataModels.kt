package com.reptimate.iot_teamnova.Retrofit

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class GetResult(
    var status:String? = null,
    var message:String? = null,
    var result:JsonObject? = null
)

data class GetScheduleResult(
    var status:String? = null,
    var message:String? = null,
    var result:JsonArray? = null
)

data class GetYearResult(
    var status:String? = null,
    var message:String? = null,
    var result:JsonArray? = null
)

data class GetUserResult(
    var status:String? = null,
    var message:String? = null
)

data class LoginModel( // 로그인
    var email : String? =null,
    var password : String?=null,
    var fbToken : String?=null
)

data class TokenModel( // 토큰 재발급
    var refreshToken : String? =null
)

data class KakaoLoginModel( // 회원가입 (카카오)
    var accessToken : String? =null,
    var socialType : String?=null,
    var fbToken : String?=null
)

data class GoogleLoginModel( // 회원가입 (구글)
    var nickname : String? =null,
    var email : String? =null,
    var socialType : String?=null,
    var fbToken : String?=null
)

data class JoinModel( // 회원가입
    var email : String? =null,
    var password : String?=null,
    var nickname : String? =null,
    var isPremium : Boolean? = null,
    var agreeWithMarketing : Boolean? = null
)

data class EmailModel( // 이메일 인증
    var email : String? = null,
    var type : String? = null
)

data class NickNameModel( // 닉네임 중복 확인
    var nickname: String? = null
 )

data class FindPwModel( // 비밀번호 변경
    var email : String? =null,
    var password : String? =null
)

data class UserEditModel( // 회원정보 수정
    var email : String? =null,
    var nickname : String? =null
)

data class UserEditModel2( // 회원정보수정2
    var email : String? =null
)

data class UserEditModel3( // 회원정보수정3
    var nickname : String? =null
)

data class PasswordEditModel( // 비밀번호 수정
    var currentPassword : String? = null,
    var newPassword : String? = null
)

data class UserDelModel( // 회원 탈퇴
    var password : String? =null
)
data class PostResult( // POST 리스폰스값
    var status:String? = null,
    var message:String? = null,
    var result:JsonObject? = null
)

data class PostLoginResult( // 로그인 POST 리스폰스값
    var status:String? = null,
    var message:String? = null,
    var result:String? = null
)

data class ErrorResponse(
    @SerializedName("message") val message: String,
    @SerializedName("errorCode") val errorCode: String
)

data class PetWriteModel(
    var name : String? =null,
    var type : String? =null,
    var gender: String?=null,
    var birthDate: String?=null,
    var adoptionDate: String?=null,
    var imagePath: String?=null
)

data class DiaryWriteModel(
    var title : String? =null,
    var content : String? =null
)

data class ScheduleWriteModel(
    var title : String? =null,
    var alarmTime : String? =null,
    var repeatDay: String?=null,
    var memo: String?=null,
    var type: String?="REPETITION"
)

data class CalendarScheduleWriteModel(
    var date: String?=null,
    var title : String? =null,
    var alarmTime : String? =null,
    var memo: String?=null,
    var type: String?="CALENDAR"
)

data class WeightWriteModel(
    var date : String? =null,
    var weight: Float? = null
)

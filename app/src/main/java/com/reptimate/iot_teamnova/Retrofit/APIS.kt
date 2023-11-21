import android.util.Log
import com.reptimate.iot_teamnova.HashMapConverterFactory
import com.reptimate.iot_teamnova.MainApplication
import com.reptimate.iot_teamnova.Retrofit.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import retrofit2.http.Headers


interface APIS {

    // 로그인
    @POST("/auth")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_login(
        @Body jsonparams: LoginModel
    ): Call<PostResult>

    // 카카오 로그인
    @POST("/auth/social")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_kakao_login(
        @Body jsonparams: KakaoLoginModel
    ): Call<PostResult>

    // 구글 로그인
    @POST("/auth/social")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_google_login(
        @Body jsonparams: GoogleLoginModel
    ): Call<PostResult>

    // 액세스 토큰 재발급
    @POST("/auth/token")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_token(
        @Body jsonparams: TokenModel
    ): Call<PostResult>

    // 회원가입
    @POST("/users")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_join(
        @Body jsonparams: JoinModel
    ): Call<PostLoginResult>

    //이메일 인증
    @POST("/users/email-verify")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_email(
        @Body jsonparams: EmailModel
    ): Call<PostResult>

    // 비밀번호 변경
    @POST("/users/find-password")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun find_pw(
        @Body jsonparams: FindPwModel
    ): Call<PostLoginResult>

    //회원정보 로딩
    @GET("/users/me")
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun get_users(
    ): Call<GetResult>

    //닉네임 중복 확인
    @POST("/users/nickname")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_nickName(
        @Body jsonparams: NickNameModel
    ): Call<PostLoginResult>

    //회원 정보 수정
    @HTTP(method = "PATCH", path="/users",hasBody = true)
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun user_edit(
        @Body jsonparams: UserEditModel
    ):Call<GetUserResult>

    @HTTP(method = "PATCH", path="/users",hasBody = true)
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun user_edit2(
        @Body jsonparams: UserEditModel2
    ):Call<GetUserResult>

    @HTTP(method = "PATCH", path="/users",hasBody = true)
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun user_edit3(
        @Body jsonparams: UserEditModel3
    ):Call<GetUserResult>

    //회원 정보 수정 (비밀번호)
    @HTTP(method = "PATCH", path="/users/password",hasBody = true)
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun pass_edit(
        @Body body: PasswordEditModel
    ):Call<GetUserResult>

    //회원 탈퇴
    @HTTP(method = "DELETE", path="/users",hasBody = true)
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun get_user_del(
        @Body body: UserDelModel
    ):Call<GetUserResult>

    // 반려동물 정보 작성
    @POST("/diaries/pet")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_pet_write(
        @Body jsonparams: PetWriteModel
    ): Call<PostResult>

    // 반려동물 목록 조회
    @GET("/diaries/pet")
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun get_pet_list(
        @Query("page") page: Int,
        @Query("size") size: Int = 20
    ): Call<GetResult>

    //반려동물 정보 수정
    @PATCH("/diaries/pet/{petIdx}")
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun PetEdit(
        @Path("petIdx") petIdx: String?,
        @Body jsonparams: PetWriteModel
    ): Call<PostResult>

    // 반려동물 정보 삭제
    @HTTP(method = "DELETE", path="/diaries/pet/{petIdx}",hasBody = true)
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun PetDelete(
        @Path("petIdx") petIdx: String?
    ):Call<GetUserResult>

    // 다이어리 목록 조회
    @GET("/diaries/{petIdx}")
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun get_diary_list(
        @Path("petIdx") petIdx: String?,
        @Query("page") page: Int,
        @Query("size") size: Int = 20,
        @Query("order") order: String = "DESC"
    ): Call<GetResult>

    // 다이어리 작성
    @POST("/diaries/{petIdx}")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_diary_write(
        @Path("petIdx") petIdx: String?,
        @Body jsonparams: DiaryWriteModel
    ): Call<PostResult>

    // 다이어리 수정
    @PATCH("/diaries/{diaryIdx}")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_diary_edit(
        @Path("diaryIdx") diaryIdx: String?,
        @Body jsonparams: DiaryWriteModel
    ): Call<PostResult>

    // 다이어리 상세 조회
    @GET("/diaries/{petIdx}/{diaryIdx}")
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun get_diary_view(
        @Path("petIdx") petIdx: String?,
        @Path("diaryIdx") diaryIdx: String?,
    ): Call<GetResult>

    // 다이어리 삭제
    @HTTP(method = "DELETE", path="/diaries/{diaryIdx}",hasBody = true)
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun DiaryDelete(
        @Path("diaryIdx") diaryIdx: String?
    ):Call<GetUserResult>

    // 체중 목록 조회
    @GET("/diaries/pet/{petIdx}/weight")
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun get_weight_list(
        @Path("petIdx") petIdx: String?,
        @Query("page") page: Int,
        @Query("size") size: Int = 20,
        @Query("filter") filter: String?,
        @Query("order") order: String = "ASC"
    ): Call<GetResult>

    // 체중 목록 조회(year)
    @GET("/diaries/pet/{petIdx}/weight")
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun post_weight_list(
        @Path("petIdx") petIdx: String?,
        @Query("page") page: Int,
        @Query("size") size: Int = 20,
        @Query("filter") filter: String?,
    ): Call<GetYearResult>

    // 체중 작성
    @POST("/diaries/pet/{petIdx}/weight")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_weight_write(
        @Path("petIdx") petIdx: String?,
        @Body jsonparams: WeightWriteModel
    ): Call<PostResult>

    // 체중 수정
    @POST("/diaries/weight/{weightIdx}")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_weight_edit(
        @Path("weightIdx") weightIdx: String?,
        @Body jsonparams: WeightWriteModel
    ): Call<PostResult>

    // 체중 삭제
    @HTTP(method = "DELETE", path="/diaries/weight/{weightIdx}",hasBody = true)
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun WeightDelete(
        @Path("weightIdx") weightIdx: String?
    ):Call<GetUserResult>

    // 케이지 목록 조회
    @GET("iotpersonal/boardlist")
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun get_cage_list(
        @Query("page") page: Int,
        @Query("size") size: Int = 20,
        @Query("order") order: String = "DESC"
    ): Call<GetResult>

    // 스케줄 목록 조회
    @GET("/schedules")
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun get_schedule_list(
        @Query("page") page: Int,
        @Query("size") size: Int = 20
    ): Call<GetResult>

    // 스케줄 작성
    @POST("/schedules")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_schedule_write(
        @Body jsonparams: ScheduleWriteModel
    ): Call<PostResult>

    // 스케줄 수정
    @PATCH("/schedules/{scheduleIdx}")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_schedule_edit(
        @Path("scheduleIdx") scheduleIdx: String?,
        @Body jsonparams: ScheduleWriteModel
    ): Call<PostResult>

    // 스케줄 삭제
    @HTTP(method = "DELETE", path="/schedules/{scheduleIdx}",hasBody = true)
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun ScheduleDelete(
        @Path("scheduleIdx") scheduleIdx: String?
    ):Call<GetUserResult>

    // 달력 스케줄 작성
    @POST("/schedules")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_calendar_schedule_write(
        @Body jsonparams: CalendarScheduleWriteModel
    ): Call<PostResult>

    // 달력 스케줄 목록 조회
    @GET("/schedules/{date}")
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun get_calendar_schedule_list(
        @Path("date") date: String?
    ): Call<GetScheduleResult>

    // 달력 스케줄 수정
    @PATCH("/schedules/{scheduleIdx}")
    @Headers("accept: application/json",
        "content-type: application/json")
    fun post_calendar_schedule_edit(
        @Path("scheduleIdx") scheduleIdx: String?,
        @Body jsonparams: CalendarScheduleWriteModel
    ): Call<PostResult>

    // 달력 스케줄 삭제
    @HTTP(method = "DELETE", path="/schedules/{scheduleIdx}",hasBody = true)
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun CalendarScheduleDelete(
        @Path("scheduleIdx") scheduleIdx: String?
    ):Call<GetUserResult>

    //경매 게시글 정보 로딩
    @GET("/board/{idx}?macAdress=")
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun get_live_board(
        @Path("idx") idx: String?,
    ): Call<GetResult>

    companion object { // static 처럼 공유객체로 사용가능함. 모든 인스턴스가 공유하는 객체로서 동작함.
        private const val BASE_URL = "https://api.reptimate.store/" // 주소
        val httpClient = OkHttpClient.Builder()

        val baseBuilder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(HashMapConverterFactory())

        private fun provideOkHttpClient(interceptor: AuthInterceptor): OkHttpClient
                = OkHttpClient.Builder().run {
            addInterceptor(interceptor)
            build()
        }


        fun create(): APIS {
            val gson :Gson =   GsonBuilder().setLenient().create();

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(provideOkHttpClient(AuthInterceptor()))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(HashMapConverterFactory())
                .build()
                .create(APIS::class.java)
        }

        fun <S> createBaseService(serviceClass: Class<S>?): S {
            val retrofit = baseBuilder.client(httpClient.build()).build()
            return retrofit.create(serviceClass)
        }

        class AuthInterceptor : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                var req =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer " + MainApplication.prefs.token ?: "").build()
                Log.d("token : ", MainApplication.prefs.token.toString())
                return chain.proceed(req)
            }
        }

        interface RetrofitUserEdit { // 회원 정보 수정
            @Multipart
            @PATCH("/users")
            fun profileEdit(
                @Header("Authorization") Authorization: String,
                @Part("email") type: RequestBody,
                @Part("nickname") nickName: RequestBody,
                @Part file: MultipartBody.Part
            ): Call<GetUserResult>

            @Multipart
            @PATCH("/users")
            fun profileEdit2(
                @Header("Authorization") Authorization: String,
                @Part("email") type: RequestBody,
                @Part file: MultipartBody.Part
            ): Call<GetUserResult>

            @Multipart
            @PATCH("/users")
            fun profileEdit3(
                @Header("Authorization") Authorization: String,
                @Part("nickname") nickName: RequestBody,
                @Part file: MultipartBody.Part
            ): Call<GetUserResult>

            @Multipart
            @PATCH("/users")
            fun profileEdit4(
                @Header("Authorization") Authorization: String,
                @Part file: MultipartBody.Part
            ): Call<GetUserResult>
        }

        interface RetrofitPetWrite { // 반려동물 정보 작성
            @Multipart
            @POST("/diaries/pet")
            fun PetWrite(
                @Header("Authorization") Authorization: String,
                @Part("name") name: RequestBody,
                @Part("type") type: RequestBody,
                @Part("gender") gender: RequestBody,
                @Part("birthDate") birthDate: RequestBody,
                @Part("adoptionDate") adoptionDate: RequestBody,
                @Part file: MultipartBody.Part
            ): Call<PostResult>
        }

        interface RetrofitPetEdit {
            @Multipart
            @PATCH("/diaries/pet/{petIdx}")
            fun PetEdit(
                @Path("petIdx") petIdx: String?,
                @Header("Authorization") Authorization: String,
                @Part("name") name: RequestBody,
                @Part("type") type: RequestBody,
                @Part("gender") gender: RequestBody,
                @Part("birthDate") birthDate: RequestBody,
                @Part("adoptionDate") adoptionDate: RequestBody,
                @Part file: MultipartBody.Part
            ): Call<PostResult>
        }

        interface RetrofitDiaryWrite {
            @Multipart
            @POST("/diaries/{petIdx}")
            fun DiaryWrite(
                @Path("petIdx") petIdx: String?,
                @Header("Authorization") Authorization: String,
                @Part("title") title: RequestBody,
                @Part("content") content: RequestBody,
                @Part files: List<MultipartBody.Part>
            ): Call<PostResult>

        }

        interface RetrofitDiaryEdit {
            @Multipart
            @PATCH("/diaries/{diaryIdx}")
            fun DiaryEdit(
                @Path("diaryIdx") diaryIdx: String?,
                @Header("Authorization") Authorization: String,
                @Part("title") title: RequestBody,
                @Part("content") content: RequestBody,
                @Part files: List<MultipartBody.Part>
            ): Call<PostResult>

        }
    }
}
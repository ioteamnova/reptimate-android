package com.reptimate.iot_teamnova

import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class HashMapConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, Any> {
        return Converter { responseBody ->
            responseBody.string()
                .let { json ->
                    Gson().fromJson(json, type)
                }
        }
    }
}
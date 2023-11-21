package com.example.iot_teamnova

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import okio.Okio
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
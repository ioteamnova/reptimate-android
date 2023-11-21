package com.reptimate.iot_teamnova

import com.google.gson.*
import java.lang.reflect.Type

class BooleanSerializer : JsonSerializer<Boolean> {
    override fun serialize(
        src: Boolean?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return if (src == true) {
            JsonPrimitive("true")
        } else {
            JsonPrimitive("false")
        }
    }
}
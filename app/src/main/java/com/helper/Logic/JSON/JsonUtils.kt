package com.helper.Logic.JSON

import android.util.Log
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object JsonUtils {

    // Для List<String>
    fun getStringList(t: JsonElement?): List<String> {
        return if (t != null && t is JsonArray) {
            t.map { it.jsonPrimitive.content }
        } else {
            emptyList()
        }
    }

    // Для List<List<String>>
    fun getListOfStringLists(t: JsonElement?): List<List<String>> {
        return t?.jsonArray?.map { inner ->
            inner.jsonArray.map { it.jsonPrimitive.content }
        } ?: emptyList()
    }

    fun getAnswersMap(t: JsonElement?): Map<String, List<String>> {
        if (t == null) {
            Log.d("DEBUG", "getAnswersMap: t == null")
            return emptyMap()
        }
        if (t !is JsonObject) {
            Log.d("DEBUG", "getAnswersMap: t is not JsonObject, t = $t")
            return emptyMap()
        }

        val map = t.mapValues { (_, value) ->
            if (value is JsonArray) {
                value.map { it.jsonPrimitive.content }
            } else {
                emptyList()
            }
        }

        //Log.d("DEBUG", "getAnswersMap result: $map")
        return map
    }

    fun countAnswers(t: JsonElement?): Int {
        val parsers: List<(JsonElement?) -> Int> = listOf(
            { getListOfStringLists(it).size },
            { getStringList(it).size },
        )

        for (parser in parsers) {
            try {
                val count = parser(t)
                if (count > 0) return count
            } catch (_: Exception) {
                //
            }
        }

        return 0
    }
}
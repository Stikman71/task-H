package com.helper.Logic.JSON

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
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
        return t?.jsonArray?.map { listOf(it.jsonPrimitive.content) } ?: emptyList()
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
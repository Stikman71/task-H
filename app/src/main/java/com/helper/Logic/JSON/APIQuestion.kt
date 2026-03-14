package com.helper.Logic.JSON

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.FileNotFoundException
import kotlin.collections.component1
import kotlin.collections.component2


class JSONHandler(private val tasks: Map<String, JsonObject> = emptyMap()) {

    companion object {
        private var current: JSONHandler? = null
        // Загружаем JSON-файл и создаем репозиторий
        fun loadFromJson(context: Context, path: String): JSONHandler {
            val jsonString = try {
                context.assets.open(path).bufferedReader().use { it.readText() }
            } catch (e: FileNotFoundException) {
                Log.d("PathBuilder", "Файл не найден: $path")
                throw RuntimeException("Файл не найден: $path")
            }

            val map = Json { ignoreUnknownKeys = true }
                .decodeFromString<Map<String, JsonObject>>(jsonString)

            val handler = JSONHandler(map)
            current = handler
            return handler
        }
        fun loadFromJson(): JSONHandler {
            return current ?: throw IllegalStateException("JSONHandler ещё не загружен")
        }
    }

    // Получаем конкретное задание по id и типу
    fun getTask(id: String, type: String): JsonObject? {
        val element = tasks[id]?.get(type) ?: return null
        return if (element is JsonObject) {
            element
        } else {
            Log.d("JSONHandler", "Элемент не JsonObject: $element")
            null
        }
    }

    // Возвращает все типы заданий, которые есть в репозитории
    fun getAllTaskTypes(): List<String> {
        return tasks.values.flatMap { it.keys }.distinct()
    }

    fun getAllIdTypePairs(): List<Pair<String, String>> {
        return tasks.flatMap { (id, typeMap) ->
            typeMap.keys.map { type -> id to type }
        }
    }

    // Метод подмножества: возвращает новый TaskRepository с одним заданием
    fun subset(id: String, type: String): JSONHandler? {
        val taskObj = getTask(id, type) ?: return null
        val subsetMap = mapOf(id to JsonObject(mapOf(type to taskObj)))
        return JSONHandler(subsetMap)
    }
}
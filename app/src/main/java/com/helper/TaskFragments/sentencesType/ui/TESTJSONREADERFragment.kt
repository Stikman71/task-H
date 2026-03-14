package com.helper.TaskFragments.sentencesType.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*


//@Serializable
//data class TaskWrapper(
//    val SENTENCE: Sentence? = null,
//    val FILLTHEBLANK: FillTheBlank? = null
//)
//
//@Serializable
//data class Sentence(
//    val template: String,
//    val blanks: List<String>,
//    val answers: List<String>,
//    val options: List<String>
//)
//
//@Serializable
//data class FillTheBlank(
//    val template: String,
//    val blanks: List<String>,
//    val answers: Map<String, List<String>>,
//    val options: List<String>
//)
//
//class TaskRepository(val tasks: Map<String, JsonObject>) {
//
//    companion object {
//        // Загружаем JSON-файл и создаем репозиторий
//        fun loadFromJson(context: Context, path: String): TaskRepository {
//            val jsonString = context.assets.open(path).bufferedReader().use { it.readText() }
//            val map = Json { ignoreUnknownKeys = true }
//                .decodeFromString<Map<String, JsonObject>>(jsonString)
//            return TaskRepository(map)
//        }
//    }
//
//    // Получаем конкретное задание по id и типу
//    fun getTask(id: String, type: String): JsonObject? {
//        return tasks[id]?.get(type)?.jsonObject
//    }
//
//    // Возвращает все типы заданий, которые есть в репозитории
//    fun getAllTaskTypes(): List<String> {
//        return tasks.values.flatMap { it.keys }.distinct()
//    }
//
//    // Печать всех заданий и их типов
//    fun printAll() {
//        tasks.forEach { (id, task) ->
//            Log.d("TASK_REPO", "Task id=$id -> ${task.keys}")
//        }
//    }
//
//    // Метод подмножества: возвращает новый TaskRepository с одним заданием
//    fun subset(id: String, type: String): TaskRepository? {
//        val taskObj = getTask(id, type) ?: return null
//        val subsetMap = mapOf(id to JsonObject(mapOf(type to taskObj)))
//        return TaskRepository(subsetMap)
//    }
//}
//
//class TestJsonFragment : Fragment() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Загружаем репозиторий
//        val repo = TaskRepository.loadFromJson(requireContext(), "Tasks/ru/Class_5/E_O_v_slovah/task.json")
//
//        // 1️⃣ Массив индексов (id заданий)
//        val ids = repo.tasks.keys.toList() // или твой массив id
//
//        // 2️⃣ Массив типов заданий
//        val types = repo.getAllTaskTypes()
//
//        // 3️⃣ Используем zip для проверки subset
//        ids.zip(types).forEach { (id, type) ->
//            val miniRepo = repo.subset(id, type)
//            val task = miniRepo?.getTask(id, type)
//
//            Log.d("DEBUGJSON", "Task id=$id, type=$type")
//            Log.d("DEBUGJSON", "template = ${task?.get("template")}")
//            Log.d("DEBUGJSON", "blanks = ${task?.get("blanks")}")
//            Log.d("DEBUGJSON", "answers = ${task?.get("answers")}")
//            Log.d("DEBUGJSON", "options = ${task?.get("options")}")
//        }
//    }
//
//    companion object {
//        @JvmStatic
//        fun newInstance() = TestJsonFragment()
//    }
//}
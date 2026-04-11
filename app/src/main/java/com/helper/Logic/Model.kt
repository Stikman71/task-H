package com.helper.Logic

import android.util.Log
import com.helper.TaskFragments.sentencesType.Models.DragTextView

data class SpinnerItem(
    val id: Int,
    val backend: String, // латиница для поиска/логики
    val display: String  // текст для UI
)

// Итоги задания
data class AnswerCheckResult(
    val correctCount: Int,
    val totalCount: Int,
    val percentage: Double
) {
    // true, если все ответы верны
    fun isAllCorrect(): Boolean = correctCount == totalCount

    fun log(tag: String = "Task Info") {
        Log.d(tag, "================ AnswerCheckResult ================")
        Log.d(tag, "Correct Count : $correctCount")
        Log.d(tag, "Total Count   : $totalCount")
        Log.d(tag, "Percentage    : ${"%.2f".format(percentage)}%")
        Log.d(tag, "All Correct?  : ${isAllCorrect()}")
        Log.d(tag, "===================================================")
    }
}

object GradeScale {
    private val grades = listOf(
        0f..54f to 2,
        55f..69f to 3,
        70f..84f to 4,
        85f..100f to 5
    )
    fun getGrade(score: Float): Int {
        return grades.firstOrNull { score in it.first }?.second ?: 2
    }
}

abstract class BaseTask(
    val id: String,
    val taskType: TaskType,
    val difficulty: String,
    val language: String,
    val topic: String,
    var baseUserAnswers: Any
) {

    var isCorrect: Boolean = false
    var isChecked: Boolean = false

    // Общий метод для логирования
    open fun log() {
        Log.d("Task Info", "")
        Log.d("Task Info", "=================================================")
        Log.d("Task Info", "Task Information:")
        Log.d("Task Info", "TaskType: ${javaClass.simpleName}")
        Log.d("Task Info", "ID: $id")
        Log.d("Task Info", "Type: $taskType")
        Log.d("Task Info", "Difficulty: $difficulty")
        Log.d("Task Info", "Language: $language")
        Log.d("Task Info", "Topic: $topic")
        Log.d("Task Info", "Checked: $isChecked")
        Log.d("Task Info", "Correct: $isCorrect")
    }

    // Абстрактный метод для загрузки ответов
    open fun loadAnswers(): AnswersWrapper {
        return AnswersWrapper(baseUserAnswers)
    }

    companion object {
        fun getEmptyTask(
            id: String,
            taskType: TaskType,
            difficulty: String,
            language: String,
            topic: String
        ): BaseTask {
            return when (taskType) {
                TaskType.SENTENCE -> TaskSentences(id, taskType, difficulty, language, topic)
                TaskType.FILLTHEBLANK -> TaskFillTheBlank(id, taskType, difficulty, language, topic)
                TaskType.SELECTWORD -> TaskWordSelector(id, taskType, difficulty, language, topic)
                TaskType.TEXTINPUT -> TaskTextInput(id, taskType, difficulty, language, topic)
                TaskType.SCATTER -> TaskScatterWords(id, taskType, difficulty, language, topic)
                else -> throw IllegalArgumentException("Unsupported TaskType: $taskType")
            }
        }
    }
}


class AnswersWrapper(internal val data: Any) {

    fun asList(): List<String>? =
        (data as? List<*>)?.filterIsInstance<String>()

    fun asMutableList(): MutableList<String>? =
        (data as? MutableList<*>)?.filterIsInstance<String>()?.toMutableList()

    fun asMap(): Map<String, List<String>>? =
        (data as? Map<*, *>)?.mapNotNull { (k, v) ->
            val key = k as? String
            val value = (v as? List<*>)?.filterIsInstance<String>()
            if (key != null && value != null) key to value else null
        }?.toMap()

    fun asMutableMap(): MutableMap<String, List<String>>? =
        asMap()?.toMutableMap()

    private inline fun <reified T> asType(): T? = data as? T
}


class TaskSentences(
    id: String,
    taskType: TaskType,
    difficulty: String,
    language: String,
    topic: String,
    var userAnswers: MutableList<MutableList<String>> = mutableListOf()
) : BaseTask(id, taskType, difficulty, language, topic,userAnswers) {  // Теперь нет необходимости передавать данные в конструктор родительского класса

    // Реализация загрузки ответов


    // Переопределение логирования
    override fun log() {
        super.log()
        Log.d("Task Info", "User Answers: ${if (userAnswers.isEmpty()) "None" else userAnswers.joinToString(", ")}")
        Log.d("Task Info", "=================================================")
        Log.d("Task Info", "")
    }
}

class TaskFillTheBlank(
    id: String,
    taskType: TaskType,
    difficulty: String,
    language: String,
    topic: String,
    var userAnswers: MutableList<List<String>> = mutableListOf()
) : BaseTask(id, taskType, difficulty, language, topic,userAnswers) {  // Теперь нет необходимости передавать данные в конструктор родительского класса

    // Переопределение логирования
    override fun log() {
        super.log()
        Log.d("Task Info", "User Answers: ${if (userAnswers.isEmpty()) "None" else userAnswers.joinToString(", ")}")
        Log.d("Task Info", "=================================================")
        Log.d("Task Info", "")
    }
}

class TaskWordSelector(
    id: String,
    taskType: TaskType,
    difficulty: String,
    language: String,
    topic: String,
    var userAnswers: MutableList<String> = mutableListOf()
) : BaseTask(id, taskType, difficulty, language, topic,userAnswers) {  // Теперь нет необходимости передавать данные в конструктор родительского класса


    // Переопределение логирования
    override fun log() {
        super.log()
        Log.d("Task Info", "User Answers: ${if (userAnswers.isEmpty()) "None" else userAnswers.joinToString(", ")}")
        Log.d("Task Info", "=================================================")
        Log.d("Task Info", "")
    }
}

class TaskScatterWords(
    id: String,
    taskType: TaskType,
    difficulty: String,
    language: String,
    topic: String,
    var userAnswers: MutableMap<String, List<String>> = mutableMapOf()
) : BaseTask(id, taskType, difficulty, language, topic,userAnswers) {

    init {
        // Гарантируем наличие ключей "left" и "right"
        if (!userAnswers.containsKey("left")) userAnswers["left"] = listOf()
        if (!userAnswers.containsKey("right")) userAnswers["right"] = listOf()
    }

    // Переопределение логирования
    override fun log() {
        super.log()
        Log.d(
            "Task Info",
            "User Answers: ${if (userAnswers.isEmpty()) "None" else userAnswers.entries.joinToString { "${it.key} -> ${it.value}" }}"
        )
        Log.d("Task Info", "=================================================")
        Log.d("Task Info", "")
    }
}

class TaskTextInput(
    id: String,
    taskType: TaskType,
    difficulty: String,
    language: String,
    topic: String,
    var userAnswers: MutableList<String> = mutableListOf()
) : BaseTask(id, taskType, difficulty, language, topic,userAnswers) {  // Теперь нет необходимости передавать данные в конструктор родительского класса

    // Реализация загрузки ответов


    // Переопределение логирования
    override fun log() {
        super.log()
        Log.d("Task Info", "User Answers: ${if (userAnswers.isEmpty()) "None" else userAnswers.joinToString(", ")}")
        Log.d("Task Info", "=================================================")
        Log.d("Task Info", "")
    }
}
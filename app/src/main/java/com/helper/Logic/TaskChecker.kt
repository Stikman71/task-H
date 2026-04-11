package com.helper.Logic

import android.util.Log
import com.helper.Logic.JSON.JsonUtils
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

@Suppress("UNCHECKED_CAST")
object TaskChecker {

    fun checkAnswer(taskType: TaskType, bt: BaseTask, je: JsonElement): AnswerCheckResult {
        return when (taskType) {
            TaskType.SENTENCE   ->  checkSentence(bt,je)
            TaskType.FILLTHEBLANK   ->  checkFillInTheBlank(bt,je)
            TaskType.SELECTWORD ->  checkSelectWord(bt,je)
            TaskType.SCATTER    ->  checkScatterWords(bt,je)
            TaskType.TEXTINPUT  ->  checkTextInput(bt, je)
            else -> AnswerCheckResult(-1, -1, -1.0)
        }
    }

    // Проверка для SENTENCE
    private fun checkSentence(bt: BaseTask, je: JsonElement): AnswerCheckResult {
        if (bt !is TaskSentences) return AnswerCheckResult(0, 0, 0.0)

// Правильные ответы: список списков
        val correctLists: List<List<String>> = JsonUtils.getListOfStringLists(je).takeIf { it.isNotEmpty() }
            ?: return AnswerCheckResult(-1, -1, -1.0)

// Ответы пользователя: Map<Int, Map<Int, String>> или MutableMap<Int, MutableMap<Int, DragTextView>>
        val userAnswers = bt.userAnswers

// Логируем списки целиком
        Log.d("DEBUG|AnswerCheck", "User answers: $userAnswers")
        Log.d("DEBUG|AnswerCheck", "Correct answers: $correctLists")

        var correctCount = 0
        var totalCount = 0

        for ((sentenceIndex, correctList) in correctLists.withIndex()) {
            val userMap = userAnswers[sentenceIndex] ?: continue

            for ((blankIndex, correctWord) in correctList.withIndex()) {
                val userWord = userMap[blankIndex].toString() ?: ""
                val isCorrect = userWord == correctWord

                if (isCorrect) correctCount++

                totalCount++

                // Логируем каждый ответ
                Log.d(
                    "DEBUG|AnswerCheck",
                    "Sentence $sentenceIndex, Blank $blankIndex: user='$userWord', correct='$correctWord', result=$isCorrect"
                )
            }
        }

        val percentage = if (totalCount == 0) 0.0 else correctCount.toDouble() / totalCount * 100

        return AnswerCheckResult(correctCount, totalCount, percentage)
    }


    // Проверка для FILLTHEBLANK
    private fun checkFillInTheBlank(bt: BaseTask, je: JsonElement): AnswerCheckResult {
        if (bt !is TaskFillTheBlank) return AnswerCheckResult(0, 0, 0.0)

        val correctList = JsonUtils.getListOfStringLists(je).takeIf { it.isNotEmpty() }
            ?: return AnswerCheckResult(-1, -1, -1.0)

        val userList = bt.userAnswers

        Log.d("DEBUG|AnswerCheck", "User list: $userList")
        Log.d("DEBUG|AnswerCheck", "Correct list: $correctList")

        var correctCount = 0
        var totalCount = 0

        userList.forEachIndexed { sentenceIndex, userSlots ->
            val correctSlots = correctList.getOrNull(sentenceIndex) ?: emptyList()

            userSlots.forEachIndexed { slotIndex, userInput ->

                if (correctSlots.isNotEmpty()) {
                    totalCount++

                    val isCorrect = correctSlots.any {
                        it.equals(userInput, ignoreCase = true)
                    }

                    if (isCorrect) {
                        correctCount++
                    }
                }

                Log.d(
                    "DEBUG|AnswerCheck",
                    "Sentence $sentenceIndex Slot $slotIndex: user='$userInput', correct='$correctSlots'"
                )
            }
        }

        val percentage =
            if (totalCount == 0) 0.0 else correctCount.toDouble() / totalCount * 100

        return AnswerCheckResult(correctCount, totalCount, percentage)
    }

    // Проверка для SELECTWORD
    private fun checkSelectWord(bt: BaseTask, je: JsonElement): AnswerCheckResult {
        if (bt !is TaskWordSelector) return AnswerCheckResult(0, 0, 0.0)

        val correctList = JsonUtils.getStringList(je).takeIf { it.isNotEmpty() }
            ?: return AnswerCheckResult(-1, -1, -1.0)

        val userList = bt.userAnswers

        val correctSet = correctList.toSet()

        Log.d("CHECK_SELECT_WORD", "=== Start check ===")
        Log.d("CHECK_SELECT_WORD", "Correct answers: $correctList")
        Log.d("CHECK_SELECT_WORD", "User answers (in order): $userList")

        var correctCount = 0
        var wrongCount = 0

        userList.forEach { answer ->
            if (answer in correctSet) {
                correctCount++
                Log.d("CHECK_SELECT_WORD", "✅ Correct: $answer")
            } else {
                wrongCount++
                Log.d("CHECK_SELECT_WORD", "❌ Wrong: $answer")
            }
        }

        val totalCount = correctList.size
        // процент с учётом лишних слов
        val percentage = ((correctCount - wrongCount).coerceAtLeast(0).toDouble() / totalCount) * 100

        Log.d("CHECK_SELECT_WORD", "Correct count = $correctCount / $totalCount")
        Log.d("CHECK_SELECT_WORD", "Wrong count = $wrongCount")
        Log.d("CHECK_SELECT_WORD", "Percentage = $percentage")
        Log.d("CHECK_SELECT_WORD", "=== End check ===")

        return AnswerCheckResult(correctCount, totalCount, percentage)
    }

    private fun checkScatterWords(bt: BaseTask, je: JsonElement): AnswerCheckResult {
        if (bt !is TaskScatterWords) return AnswerCheckResult(0, 0, 0.0)

        // Берём правильные ответы из JSON
        val correctMap: Map<String, List<String>> = JsonUtils.getAnswersMap(je)
            .takeIf { it.isNotEmpty() } ?: return AnswerCheckResult(-1, -1, -1.0)

        var correctCount = 0
        var totalCount = 0

        // Проходим по всем колонкам
        correctMap.forEach { (column, correctList) ->
            // Берём ответы пользователя как строки
            val userList = bt.userAnswers[column]?.map { it.trim() } ?: emptyList()

            // Для каждого правильного слова проверяем, есть ли оно среди ответов пользователя
            val perAnswer = correctList.map { correctWord ->
                val word = correctWord.trim()
                val isCorrect = userList.contains(word)

                // Логируем процесс
                Log.d(
                    "DEBUG|AnswerCheck",
                    "Column '$column': userList=$userList, correctWord='$word', result=$isCorrect"
                )

                isCorrect
            }

            correctCount += perAnswer.count { it }
            totalCount += correctList.size
        }

        val percentage = if (totalCount > 0) (correctCount.toDouble() / totalCount) * 100 else 0.0
        return AnswerCheckResult(correctCount, totalCount, percentage)
    }

    private fun checkTextInput(bt: BaseTask, je: JsonElement): AnswerCheckResult {
        if (bt !is TaskTextInput) return AnswerCheckResult(0, 0, 0.0)

        val answersArray = je as? JsonArray ?: return AnswerCheckResult(-1, -1, -1.0)
        val userAnswers = bt.userAnswers.map { it.trim() }

        var correctCount = 0

        Log.d("DEBUG_CHECK", "Проверяем ответы пользователя: $userAnswers")
        Log.d("DEBUG_CHECK", "Эталонные ответы: $answersArray")

        for ((index, item) in answersArray.withIndex()) {
            val userAnswer = userAnswers.getOrNull(index) ?: ""

            val isCorrect = when (item) {
                is JsonPrimitive -> {
                    val match = userAnswer == item.content
                    Log.d("DEBUG_CHECK", "Позиция $index: пользователь='$userAnswer', эталон='${item.content}', правильно=$match")
                    match
                }
                is JsonArray -> {
                    val correctOptions = item.mapNotNull { (it as? JsonPrimitive)?.content }
                    val match = userAnswer in correctOptions
                    Log.d("DEBUG_CHECK", "Позиция $index: пользователь='$userAnswer', эталон=$correctOptions, правильно=$match")
                    match
                }
                else -> {
                    Log.d("DEBUG_CHECK", "Позиция $index: неподдерживаемый тип")
                    false
                }
            }

            if (isCorrect) correctCount++
        }

        val totalCount = answersArray.size
        val percentage = if (totalCount > 0) correctCount.toDouble() / totalCount * 100 else 0.0

        Log.d("DEBUG_CHECK", "Итог: $correctCount из $totalCount, процент = $percentage")

        return AnswerCheckResult(correctCount, totalCount, percentage)
    }
}
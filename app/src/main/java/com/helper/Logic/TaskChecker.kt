package com.helper.Logic

import android.util.Log
import com.helper.Logic.JSON.JsonUtils
import kotlinx.serialization.json.JsonElement

@Suppress("UNCHECKED_CAST")
object TaskChecker {

    fun checkAnswer(taskType: TaskType, bt: BaseTask, je: JsonElement): AnswerCheckResult {
        return when (taskType) {
            TaskType.SENTENCE -> checkSentence(bt,je)
            TaskType.FILLTHEBLANK -> checkFillInTheBlank(bt,je)
            TaskType.SELECTWORD -> checkSelectWord(bt,je)
            else -> AnswerCheckResult(-1, -1, -1.0)
        }
    }

    // Проверка для SENTENCE
    private fun checkSentence(bt: BaseTask,je: JsonElement): AnswerCheckResult {
        if (bt !is TaskSentences) return AnswerCheckResult(0, 0, 0.0)
        val correctList = JsonUtils.getStringList(je).takeIf { it.isNotEmpty() }
            ?: return AnswerCheckResult(-1, -1, -1.0)
        val userList = bt.userAnswers

        val perAnswer = correctList.mapIndexed { index, correctWord ->
            val userWord = userList.getOrNull(index) ?: ""
            userWord == correctWord
        }

        val correctCount = perAnswer.count { it }
        val totalCount = correctList.size
        val percentage = if (totalCount == 0) 0.0 else correctCount.toDouble() / totalCount * 100
        return AnswerCheckResult(correctCount, totalCount, percentage)
    }

    // Проверка для FILLTHEBLANK
    private fun checkFillInTheBlank(bt: BaseTask,je: JsonElement): AnswerCheckResult {
        if (bt !is TaskFillTheBlank) return AnswerCheckResult(0, 0, 0.0)
        val correctList = JsonUtils.getListOfStringLists(je).takeIf { it.isNotEmpty() }
            ?: return AnswerCheckResult(-1, -1, -1.0)
        val userList = bt.userAnswers

        val correctCount = userList.mapIndexed { index, userWord ->
            val correctOptions = correctList.getOrNull(index) ?: emptyList()
            userWord in correctOptions
        }.count { it }

        val totalCount = correctList.size
        val percentage = if (totalCount == 0) 0.0 else correctCount.toDouble() / totalCount * 100
        return AnswerCheckResult(correctCount, totalCount, percentage)
    }

    // Проверка для SELECTWORD
    private fun checkSelectWord(bt: BaseTask,je: JsonElement): AnswerCheckResult {
        if (bt !is TaskWordSelector) return AnswerCheckResult(0, 0, 0.0)
        val correctList = JsonUtils.getStringList(je).takeIf { it.isNotEmpty() }
            ?: return AnswerCheckResult(-1, -1, -1.0)
        val userList = bt.userAnswers

        val correctCount = userList.count { it in correctList }
        val totalCount = correctList.size
        val percentage = if (totalCount > 0) correctCount.toDouble() / totalCount * 100 else 0.0
        return AnswerCheckResult(correctCount, totalCount, percentage)
    }
}
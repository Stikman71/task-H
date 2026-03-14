package com.helper.TaskFragments.SelectWord

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import com.helper.DataManager.ClientTaskSession
import com.helper.Logic.AnswerCheckResult
import com.helper.Logic.BaseTask
import com.helper.Logic.JSON.JsonUtils
import com.helper.Logic.TaskFillTheBlank
import com.helper.Logic.TaskType
import com.helper.Logic.TaskWordSelector
import com.helper.R
import com.helper.TaskFragment
import com.helper.TaskFragments.TaskFragmentBase
import com.helper.databinding.FragmentWordSelectionBinding
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.collections.map
import kotlin.collections.mutableListOf


class WordSelectionFragment :
    TaskFragmentBase<FragmentWordSelectionBinding>() {


    private lateinit var template: List<String>
    private lateinit var answers: List<String>
    private var selectableTextView=mutableListOf<TextView>()
    private val currentAnswers = mutableListOf<String>()
    private var _sessionClient_test = mutableListOf<String>()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWordSelectionBinding {
        return FragmentWordSelectionBinding.inflate(inflater, container, false)
    }

    override fun checkButtonIsPressed() {
        compareAnswers()
        saveClientTaskSession()
    }

    override fun clearButtonIsPressed() {
        currentAnswers.clear()
        showItems()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFragmentDataFromJSON()
        parsedTemplate()
        parsedAnswer()

        Log.d("DEBUGJSON", "Template = $template")
        Log.d("DEBUGJSON", "Answers = $answers")
        initSessionTest()
        showItems()
        ClientTaskSession
            .getTaskByTypeAndId(TaskType.valueOf(_session.taskType!!), _session.taskID.toString())
            ?.let { task ->
                loadFragmentFromSession(task)
            }
    }

    private fun initSessionTest() {
        val allWords = answers.flatMap { it.split(" ") }
        _sessionClient_test.clear()
        _sessionClient_test.addAll(allWords.shuffled().take(3)) // взять 3 случайных слова
    }

    // ----------------------------
    // Построение UI
    // ----------------------------

    private fun showItems() {
        binding.rootContainer.removeAllViews()

        template.forEach { item ->
            binding.rootContainer.addView(createSentenceRow(item))
        }
    }

    private fun createSentenceRow(text: String): View {
        val flexbox = FlexboxLayout(requireContext()).apply {
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
            setPadding(0, 16, 0, 16)
        }

        val words = text.split(" ")

        words.forEach { word ->
            val _tmp=createSelectableWord(word)
            selectableTextView.add(_tmp)
            flexbox.addView(_tmp)
        }

        return flexbox
    }

    private fun createSelectableWord(word: String): TextView {
        return TextView(requireContext(), null,0,R.style.SelectableWord).apply {
            text = word
            textSize = 18f

            // Белый цвет текста
            //setTextColor(Color.WHITE)

            // Убираем фон-кнопку
            background = null

            // Отступы
            setPadding(8, 4, 8, 4)

            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 8, 8)
            }

            setOnClickListener {
                toggleSelection(this, word)
            }
        }
    }

    // ----------------------------
    // Логика выбора
    // ----------------------------

    private fun toggleSelection(view: TextView, word: String) {
        if (currentAnswers.contains(word)) {
            currentAnswers.remove(word)
            removeUnderline(view)
        } else {
            currentAnswers.add(word)
            applyUnderline(view)
        }
    }

    private fun applyUnderline(textView: TextView) {
        textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        textView.setTextColor(Color.GREEN) // цвет текста не меняем
    }
    private fun markAsError(textView: TextView) {
        textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        textView.setTextColor(Color.RED)
    }

    private fun removeUnderline(textView: TextView) {
        textView.paintFlags = textView.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        val typedArray = context?.obtainStyledAttributes(
            null,
            intArrayOf(android.R.attr.textColor),
            0,
            R.style.SelectableWord
        )

        val color = typedArray?.getColor(0, textView.currentTextColor)
        typedArray?.recycle()

        textView.setTextColor(color!!)
    }

    // ----------------------------
    // Заполнение данных задания
    // ----------------------------
    override fun loadFragmentDataFromJSON(){
        val pf = parentFragment
        if (pf is TaskFragment) {
            qst = pf.getCurrentTask()
            if (qst != null) {
                Log.d("DEBUG", "Task существует")
            } else {
                Log.d("DEBUG", "Task = null! Что-то не так")
            }
        }
        Log.d("DEBUG", qst.toString())
    }

    // ----------------------------
    // Загрузка выбранных слов из "сессии"
    // ----------------------------
    override fun loadFragmentFromSession(bt: BaseTask) {
        if(bt !is TaskWordSelector) return
        if (bt.userAnswers.isEmpty()) return
        currentAnswers.clear()
        Log.e("loadFragmentFromSession", "$currentAnswers")
        currentAnswers.addAll(bt.userAnswers)

        // Проходим по всем FlexboxLayout и подсвечиваем слова
        for (i in 0 until binding.rootContainer.childCount) {
            val flexbox = binding.rootContainer.getChildAt(i) as? FlexboxLayout ?: continue
            for (j in 0 until flexbox.childCount) {
                val textView = flexbox.getChildAt(j) as? TextView ?: continue
                val word = textView.text.toString()
                if (currentAnswers.contains(word)) {
                    Log.d("DEBUG_UNDERLINE", "Подчеркиваем слово: $word")
                    applyUnderline(textView)
                }
            }
        }
    }

    override fun compareAnswers() {
        val selectedSet = currentAnswers.toSet()
        val targetSet = answers.toSet()

        // Проверяем, выбрал ли пользователь все и только правильные слова
        val isCompletelyCorrect = selectedSet == targetSet

        // количество правильных слов, которые дал пользователь
        val correctCount = selectedSet.count { it in targetSet }

        // общее количество правильных слов
        val totalCount = targetSet.size

        // процент выполнения в Double
        val percentage = if (totalCount > 0) {
            // Если есть лишние слова, уменьшаем процент
            val extraCount = (selectedSet - targetSet).size
            val rawPercentage = correctCount.toDouble() / totalCount * 100.0
            val adjustedPercentage = if (extraCount > 0) rawPercentage * totalCount / (totalCount + extraCount) else rawPercentage
            adjustedPercentage
        } else 0.0

        val result = AnswerCheckResult(correctCount, totalCount, percentage)
        ClientTaskSession.updateTaskResult(_session.taskID.toString(), result)

        result.log()

        selectableTextView.forEach { textView ->
            val word = textView.text.toString()
            when {
                word in targetSet && word in selectedSet -> applyUnderline(textView)     // правильно выбран
                word in selectedSet && word !in targetSet -> markAsError(textView)      // выбран, но неправильно
                else -> removeUnderline(textView)                                        // не выбран
            }
        }
    }

    override fun parsedAnswer() {
        val t = qst?.get("answers")
        answers=JsonUtils.getStringList(t)
    }

    override fun parsedTemplate() {
        Log.d("DEBUG", qst?.get("template").toString())
        template = qst?.get("template")?.jsonArray
            ?.map { it.jsonPrimitive.content }
            ?: emptyList()
    }

    override fun parsedBlank() {

    }

    override fun parsedOptions() {

    }

    override fun collectClientAnswer() {
        //
    }

    override fun createBaseTask(): BaseTask {
        return TaskWordSelector(
            id = _session.taskID.toString(),
            taskType = TaskType.valueOf(_session.taskType!!),
            difficulty = _session.classID.toString(),
            language = _session.language!!,
            topic = _session.topic!!,
            userAnswers = currentAnswers.toMutableList()
        )
    }

    override fun updateClientTaskSession(bt: BaseTask): BaseTask {
        if (bt !is TaskWordSelector) {
            throw IllegalArgumentException("Critical Error: updateClientTaskSession expects TaskSentences, got ${bt::class.simpleName}")
        }
        val newAnswers = currentAnswers.toMutableList()
        bt.userAnswers.clear()
        bt.userAnswers.addAll(newAnswers)
        return bt
    }

}
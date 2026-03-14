package com.helper.TaskFragments.InsertALetter

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.forEachIndexed
import com.helper.DataManager.ClientTaskSession
import com.helper.Logic.AnswerCheckResult
import com.helper.Logic.BaseTask
import com.helper.Logic.JSON.JsonUtils
import com.helper.Logic.TaskFillTheBlank
import com.helper.Logic.TaskType
import com.helper.R
import com.helper.TaskFragment
import com.helper.TaskFragments.TaskFragmentBase
import com.helper.databinding.FragmentFillInTheBlankBinding
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class FillInTheBlankFragment : TaskFragmentBase<FragmentFillInTheBlankBinding>() {

    private lateinit var template: List<String>
    private lateinit var answers: List<List<String>>
    private lateinit var blanks: List<String>

    private var holderFillWord=mutableListOf<LinearLayout>()

    var currentAnswers: MutableList<String> = mutableListOf()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFillInTheBlankBinding {
        return FragmentFillInTheBlankBinding.inflate(inflater, container, false)
    }

    override fun checkButtonIsPressed() {
        compareAnswers()
        saveClientTaskSession()
    }

    override fun clearButtonIsPressed() {
        holderFillWord.forEach { layout ->
            for (i in 0 until layout.childCount) {
                val child = layout.getChildAt(i)
                when (child) {
                    is EditText -> {
                        child.text.clear()          // очищаем текст
                        markCorrect(child)          // возвращаем дефолтный стиль
                    }
                    is TextView -> {
                        markCorrect(child)          // возвращаем дефолтный стиль
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFragmentDataFromJSON()
        parsedTemplate()
        parsedAnswer()
        parsedBlank()
        prepareTemplate(template)


        // Если пользователь возвращается пытаемся найти его отвветы
        ClientTaskSession
            .getTaskByTypeAndId(TaskType.valueOf(_session.taskType!!), _session.taskID.toString())
            ?.let { task ->
                loadFragmentFromSession(task)
            }
        Log.d("DEBUG", "HERE_5")

        binding.wordsContainer.setOnClickListener {
            // Снять фокус с любого EditText внутри
            binding.wordsContainer.clearFocus()
            // Скрыть клавиатуру
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.wordsContainer.windowToken, 0)
        }
    }

    // ----------------------------
    // Заполнение данныз для задания
    // ----------------------------
    override fun loadFragmentDataFromJSON(){
        val pf = parentFragment
        if (pf is TaskFragment) {
            qst = pf.getCurrentTask()
            if (qst != null) {
                Log.d("DEBUG", "qst существует")
            } else {
                Log.d("DEBUG", "Task = null! Что-то не так")
            }
        }
        Log.d("DEBUG", qst.toString())
    }

    override fun loadFragmentFromSession(bt: BaseTask) {
        fillBlanksWithTestWords(bt)
    }

    override fun compareAnswers() {
        collectClientAnswer() // собираем ответы пользователя в currentAnswers

        // --- 1. Собираем пользовательские слова и проверяем ---
        val perAnswer: List<Boolean> = holderFillWord.mapIndexed { index, layout ->
            val userWord = getWordFromLayout(layout)       // собираем слово из EditText/TextView
            val correctOptions = answers.getOrNull(index) ?: emptyList()
            userWord in correctOptions                     // true, если слово есть среди вариантов
        }

        // --- 2. Считаем количество правильных и процент ---
        val correctCount = perAnswer.count { it }
        val totalCount = answers.size
        val percentage = if (totalCount == 0) 0.0 else correctCount.toDouble() / totalCount * 100

        // --- 3. Создаём результат ---
        val result = AnswerCheckResult(
            correctCount = correctCount,
            totalCount = totalCount,
            percentage = percentage
        )

        ClientTaskSession.updateTaskResult(_session.taskID.toString(), result)
        result.log()

        // --- 4. Подсветка ошибок/правильных ответов ---
        holderFillWord.forEachIndexed { index, layout ->
            val isCorrect = perAnswer.getOrNull(index) ?: false
            highlightWordLayout(layout, isCorrect)
        }
    }
    private fun highlightWordLayout(layout: LinearLayout, isCorrect: Boolean) {
        for (i in 0 until layout.childCount) {
            val child = layout.getChildAt(i)
            if (isCorrect) markCorrect(child) else markIncorrect(child)
        }
    }

    private fun markCorrect(view: View) {
        when (view) {
            is TextView -> {
                view.paintFlags = view.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
                view.setTextColor(ContextCompat.getColor(view.context, R.color.colorButtonBackground))
            }
            is EditText -> {
                view.paintFlags = view.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
                view.setTextColor(ContextCompat.getColor(view.context, R.color.colorButtonBackground))
            }
        }
    }

    private fun markIncorrect(view: View) {
        when (view) {
            is TextView, is EditText -> {
                view.paintFlags = view.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                (view as TextView).setTextColor(Color.RED)
            }
        }
    }

    override fun parsedAnswer() {
        val t = qst?.get("answers")
        answers=JsonUtils.getListOfStringLists(t)
    }

    override fun parsedTemplate() {
        val element = qst?.get("template") ?: run {
            Log.w("DEBUG_JSON", "Ключ 'template' отсутствует в qst")
            return
        }

        when (element) {
            is JsonArray -> {
                template = element.map { it.jsonPrimitive.content }
            }
            is JsonPrimitive -> {
                Log.w("DEBUG_JSON", "'template' — JsonPrimitive, а ожидался JsonArray: $element")
                template = listOf(element.content) // оборачиваем в список
            }
            is JsonObject -> {
                Log.w("DEBUG_JSON", "'template' — JsonObject, а ожидался JsonArray: $element")
                // здесь можно решить, как обрабатывать объект
            }
            else -> {
                Log.w("DEBUG_JSON", "'template' имеет неизвестный тип: $element")
            }
        }
//        template = qst?.get("template")?.jsonArray
//            ?.map { it.jsonPrimitive.content }
//            ?: return
    }

    override fun parsedBlank() {
        blanks = qst?.get("blanks")?.jsonArray
            ?.map { it.jsonPrimitive.content } ?: emptyList()
    }

    override fun parsedOptions() {
        TODO("Not yet implemented")
    }

    override fun collectClientAnswer() {
        currentAnswers.clear()
        binding.wordsContainer.forEachIndexed { index, view ->
            val layout = view as? LinearLayout ?: return@forEachIndexed
            currentAnswers.add(getWordFromLayout(layout))
        }
    }
    private fun buildAnswerList(): MutableList<String> {
        collectClientAnswer()
        return currentAnswers
    }

    override fun createBaseTask(): BaseTask {
        return TaskFillTheBlank(
            id = _session.taskID.toString(),
            taskType = TaskType.valueOf(_session.taskType!!),
            difficulty = _session.classID.toString(),
            language = _session.language!!,
            topic = _session.topic!!,
            userAnswers = buildAnswerList()
        )
    }

    override fun updateClientTaskSession(bt: BaseTask): BaseTask {
        if (bt !is TaskFillTheBlank) {
            throw IllegalArgumentException("Critical Error: updateClientTaskSession expects TaskSentences, got ${bt::class.simpleName}")
        }
        bt.userAnswers.clear()
        bt.userAnswers.addAll(buildAnswerList())
        return bt
    }

    private fun prepareTemplate(taskWords: List<String>){
        val container = binding.wordsContainer
        container.removeAllViews()

        taskWords.forEachIndexed  {index, word ->
            val layout = createWordLayout(word)
            holderFillWord.add(layout)
            layout.tag = word
            container.addView(layout)
        }
    }


    private fun getWordFromLayout(layout: LinearLayout): String {
        val builder = StringBuilder()
        val fullWord = layout.tag as? String ?: ""
        var fullWordIndex = 0

        for (i in 0 until layout.childCount) {
            val child = layout.getChildAt(i)

            when (child) {
                is EditText -> {
                    // Считаем сколько подряд _ в исходном слове
                    var len = 0
                    while (fullWordIndex + len < fullWord.length && fullWord[fullWordIndex + len] == '_') {
                        len++
                    }

                    val text = child.text.toString()
                    // если пользователь ввёл меньше, чем len, добавляем недостающие _
                    val filled = if (text.length < len) text + "_".repeat(len - text.length) else text
                    builder.append(filled)

                    fullWordIndex += len
                }
                is TextView -> {
                    val text = child.text.toString()
                    builder.append(text)
                    fullWordIndex += text.length
                }
            }
        }

        return builder.toString()
    }

    private fun fillBlanksWithTestWords(bt: BaseTask) {
        val container = binding.wordsContainer
        if(bt !is TaskFillTheBlank) return
        val testAnswers = bt.userAnswers
        //val testAnswers = listOf("Пр_грамма", "Котик", "Молоко", "Солнце")

        for (i in 0 until container.childCount) {
            val wordLayout = container.getChildAt(i) as LinearLayout
            if (i >= testAnswers.size) break

            val testWord = testAnswers[i]

            for (j in 0 until wordLayout.childCount) {
                val child = wordLayout.getChildAt(j)
                if (child is EditText) {
                    val range = child.tag as? IntRange ?: continue

                    var fillText = ""
                    for (pos in range) {
                        val c = if (pos < testWord.length) testWord[pos] else null
                        fillText += if (c != null && c != '_') c else "" // пусто вместо "_"
                    }

                    child.setText(fillText)
                }
            }
        }
    }

    private fun createBlankEditText(length: Int): EditText {
        return EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setEms(length) // ширина поля под length символов
            filters = arrayOf(InputFilter.LengthFilter(length)) // ограничиваем ввод
            inputType = InputType.TYPE_CLASS_TEXT
            gravity = Gravity.CENTER
        }
    }
//    private fun createBlankEditText(length: Int): EditText {
//        // Локальный стиль из XML, применяем только к этим EditText
//        val themedContext = ContextThemeWrapper(requireContext(), R.style.WordLayoutEditTextStyle)
//        val editText = EditText(themedContext).apply {
//            layoutParams = LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            )
//            setEms(length)
//            filters = arrayOf(InputFilter.LengthFilter(length))
//            inputType = InputType.TYPE_CLASS_TEXT
//            gravity = Gravity.CENTER
//
//            // Сбросить внутренние паддинги фона
//            setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
//        }
//        return editText
//    }
    private fun createWordLayout(word: String): LinearLayout {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
        }

        val regex = "_+".toRegex()
        var lastIndex = 0

        regex.findAll(word).forEach { match ->
            val start = match.range.first
            val end = match.range.last

            // текст до пропуска
            if (start > lastIndex) {
                layout.addView(createTextView(word.substring(lastIndex, start)))
            }

            val editText = createBlankEditText(end - start + 1)
            editText.tag = start..end // сохраняем позиции пропуска
            layout.addView(editText)

            lastIndex = end + 1
        }

        // текст после последнего пропуска
        if (lastIndex < word.length) {
            layout.addView(createTextView(word.substring(lastIndex)))
        }

        layout.tag = word // сохраняем исходное слово
        return layout
    }

//     Создаёт TextView для текста
//    private fun createTextView(text: String): TextView {
//        return TextView(requireContext()).apply {
//            this.text = text
//            textSize = 18f
//        }
//    }
    private fun createTextView(text: String): TextView {
        val themedContext = ContextThemeWrapper(requireContext(), R.style.WordLayoutTextViewStyle)
        return TextView(themedContext).apply {
            this.text = text
        }
    }
}
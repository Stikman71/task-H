package com.helper.TaskFragments.InsertALetter


import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.helper.DataManager.ClientTaskSession
import com.helper.Logic.BaseTask
import com.helper.Logic.JSON.JsonUtils
import com.helper.Logic.TaskChecker
import com.helper.Logic.TaskFillTheBlank
import com.helper.Logic.TaskType
import com.helper.R
import com.helper.TaskFragment
import com.helper.TaskFragments.TaskFragmentBase
import com.helper.databinding.FragmentFillInTheBlankBinding
import kotlin.collections.forEachIndexed

class FillInTheBlankFragment : TaskFragmentBase<FragmentFillInTheBlankBinding>() {

    private val allInputs = mutableListOf<EditText>()
    private val inputsPerSentence = mutableListOf<MutableList<EditText>>()
    private var currentAnswers: MutableList<List<String>> = mutableListOf()

    private lateinit var template: List<String>

    lateinit var answers: List<List<String>>



    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFillInTheBlankBinding {
        return FragmentFillInTheBlankBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFragmentDataFromJSON()
        parsedTemplate()
        parsedAnswer()

        showItem()

        ClientTaskSession
            .getTaskByTypeAndId(TaskType.valueOf(_session.taskType!!), _session.taskID.toString())
            ?.let { task ->
                loadFragmentFromSession(task)
            }
    }

    // =========================
    // CORE
    // =========================

    private data class Part(val text: String, val isBlank: Boolean)
    private fun parseTemplate(input: String): List<Part> {
        val result = mutableListOf<Part>()
        val buffer = StringBuilder()

        input.forEach { char ->
            if (char == '_') {
                if (buffer.isNotEmpty()) {
                    result.add(Part(buffer.toString(), false))
                    buffer.clear()
                }
                result.add(Part("", true))
            } else {
                buffer.append(char)
            }
        }

        if (buffer.isNotEmpty()) {
            result.add(Part(buffer.toString(), false))
        }

        return result
    }

    private fun createSlotEditText(): EditText {
        val et = EditText(requireContext())
        val density = resources.displayMetrics.density
        val size = (20 * density).toInt() // компактный слот ~20dp

        et.layoutParams = ViewGroup.MarginLayoutParams(size, size).apply {
            setMargins(1, 0, 1, 0)
        }

        et.setPadding(0, 0, 0, 0)
        et.textSize = 16f
        et.gravity = Gravity.CENTER
        et.setSingleLine()
        et.minHeight = 0
        et.minimumHeight = 0
        et.includeFontPadding = false
        et.filters = arrayOf(InputFilter.LengthFilter(1))

        // Цвет текста берём из ресурсов @color/colorAccent
        val accentColor = ContextCompat.getColor(requireContext(), R.color.colorTextPrimary)
        et.setTextColor(accentColor)

        // Фон по умолчанию
        et.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_letter_slot)

        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Если поле пустое — фон слота, если нет — просто прозрачный фон
                et.background = if (s.isNullOrEmpty()) {
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_letter_slot)
                } else null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return et
    }

    override fun showItem() {
        binding.container.removeAllViews()
        allInputs.clear()
        inputsPerSentence.clear()

        template.forEach { sentence ->

            val lineInputs = mutableListOf<EditText>()

            val lineContainer = FlexboxLayout(requireContext()).apply {
                flexWrap = FlexWrap.WRAP
                alignItems = AlignItems.BASELINE
                setPadding(0, 0, 0, 0)
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = (8 * resources.displayMetrics.density).toInt() // 8dp
                }
            }

            val parts = parseTemplate(sentence)

            parts.forEach { part ->
                if (part.isBlank) {
                    val et = createSlotEditText()
                    allInputs.add(et)
                    lineInputs.add(et)
                    lineContainer.addView(et)
                } else {
                    val tv = TextView(
                        ContextThemeWrapper(requireContext(), R.style.MyTextStyle1122),
                        null,
                        0
                    )
                    tv.text = part.text
                    lineContainer.addView(tv)
                }
            }

            inputsPerSentence.add(lineInputs)
            binding.container.addView(lineContainer)
        }
    }

    // =========================
    // CHECK ANSWERS
    // =========================

    override fun checkButtonIsPressed() {
        collectClientAnswer()
        saveClientTaskSession()
        compareAnswers()
    }

    override fun clearButtonIsPressed() {
        allInputs.forEach { it.setText("") }
    }

    override fun loadFragmentDataFromJSON() {
        val pf = parentFragment
        if (pf is TaskFragment) {
            qst = pf.getCurrentTask()
            if (qst != null) {
                Log.d("DEBUG", "Task существует")
            } else {
                Log.d("DEBUG", "Task = null! Что-то не так")
            }
        }

    }
    override fun parsedTemplate() {
        val jsonTemplate = qst?.get("template")
        Log.d("DEBUG", "Step 1: raw JSON element: $jsonTemplate")
        template = JsonUtils.getStringList(jsonTemplate)
    }
    override fun parsedAnswer() {
        val jsonAnswers = qst?.get("answers")
        answers = JsonUtils.getListOfStringLists(jsonAnswers)
    }
    override fun collectClientAnswer() {
        val allAnswers = mutableListOf<List<String>>()

        inputsPerSentence.forEach { slots ->
            val lineAnswers = slots.map { it.text.toString() } // берём текст из каждого EditText
            allAnswers.add(lineAnswers)
        }

        currentAnswers=allAnswers.toMutableList()
    }

    override fun createBaseTask(): BaseTask {
        return TaskFillTheBlank(
            id = _session.taskID.toString(),
            taskType = TaskType.valueOf(_session.taskType!!),
            difficulty = _session.classID.toString(),
            language = _session.language!!,
            topic = _session.topic!!,
            userAnswers = currentAnswers.map { it.toList() }.toMutableList()
        )
    }
    override fun updateClientTaskSession(bt: BaseTask): BaseTask {
        if (bt !is TaskFillTheBlank) {
            throw IllegalArgumentException("Critical Error: updateClientTaskSession expects TaskSentences, got ${bt::class.simpleName}")
        }
        bt.userAnswers=currentAnswers.map { it.toList() }.toMutableList()
        Log.d("DEBUG", "CA: ${currentAnswers}")
        //  JSON с эталонными ответами для текущего задания
        val je = qst!!.get("answers")!!

        // проверку через TaskChecker
        val result = TaskChecker.checkAnswer(TaskType.FILLTHEBLANK, bt, je)

        // Сохраняем результат в сессии
        ClientTaskSession.updateTaskResult(_session.taskID.toString(), result)
        result.log()
        return bt
    }
    override fun loadFragmentFromSession(bt: BaseTask) {
        if(bt !is TaskFillTheBlank) return
        inputsPerSentence.forEachIndexed { sentenceIndex, slots ->
            val sessionValues = bt.userAnswers.getOrNull(sentenceIndex) ?: emptyList()
            slots.forEachIndexed { slotIndex, et ->
                val value = sessionValues.getOrNull(slotIndex) ?: ""
                et.setText(value)
            }
        }

    }
    override fun compareAnswers() {
        template.forEachIndexed { sentenceIndex, _ ->
            val lineInputs = inputsPerSentence[sentenceIndex]
            val correctAnswerArray = answers[sentenceIndex]

            lineInputs.forEachIndexed { slotIndex, et ->
                val userInput = et.text.toString()

                if (slotIndex < correctAnswerArray.size) {
                    // Подсветка только неправильного слота
                    if (correctAnswerArray.any { it == userInput }) {
                        // правильно убираем подсветку
                        et.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_letter_slot)
                    } else {
                        // неправильно красная подсветка
                        et.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_letter_slot_error)
                    }
                } else {
                    Log.e(
                        "FillInTheBlank",
                        "No answer defined for slot $slotIndex in sentence $sentenceIndex"
                    )
                }
            }
        }
    }

    override fun parsedOptions() {}
    override fun parsedBlank() {}

}
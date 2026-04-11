package com.helper.TaskFragments.sentencesType

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import com.google.android.flexbox.FlexboxLayout
import com.helper.DataManager.ClientTaskSession
import com.helper.Logic.AnswerCheckResult
import com.helper.Logic.BaseTask
import com.helper.Logic.JSON.JsonUtils
import com.helper.Logic.TaskChecker
import com.helper.Logic.TaskScatterWords
import com.helper.Logic.TaskSentences
import com.helper.Logic.TaskType
import com.helper.R
import com.helper.TaskFragment
import com.helper.TaskFragments.TaskFragmentBase
import com.helper.TaskFragments.sentencesType.Models.DragTextView
import com.helper.TaskFragments.sentencesType.layout.ExtendFlexboxLayout
import com.helper.databinding.FragmentScatterIntoGroupsBinding
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ScatterIntoGroupsFragment : TaskFragmentBase<FragmentScatterIntoGroupsBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentScatterIntoGroupsBinding {
        return FragmentScatterIntoGroupsBinding.inflate(inflater, container, false)
    }


    val flexboxListWithBlank = mutableListOf<ExtendFlexboxLayout>()
    // JSON
    private lateinit var template: List<String>
    private var answers: Map<String, List<String>> = mapOf()
    private lateinit var blanks: List<String>
    private lateinit var options: List<String>
    // JSON
    val currentAnswers: MutableMap<String, List<DragTextView>> = mutableMapOf(
        "left" to listOf(),
        "right" to listOf()
    )



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.containerBottom.canBeEmpty(true)
        binding.containerTopLeft.canBeEmpty(true)
        binding.containerTopRight.canBeEmpty(true)
        flexboxListWithBlank.add(binding.containerTopLeft)
        flexboxListWithBlank.add(binding.containerTopRight)

        loadFragmentDataFromJSON()

        ClientTaskSession
            .getTaskByTypeAndId(TaskType.valueOf(_session.taskType!!), _session.taskID.toString())
            ?.let { task ->
                loadFragmentFromSession(task)
            }
    }

    override fun checkButtonIsPressed() {
        collectClientAnswer()
        saveClientTaskSession()
        compareAnswers()
        return
    }

    // Метод для кнопки "Сбросить"
    override fun clearButtonIsPressed() {
        // Проходим по всем значениям currentAnswers, не важно left или right
        currentAnswers.values.flatten().forEach { dragTextView ->
            val removed = dragTextView.removeFromPlaceHolder() // убираем из текущего контейнера
            removed?.defaultStatus()                           // сбрасываем статус
            removed?.insertIntoNewPlace(binding.containerBottom) // вставляем в bottom контейнер
        }

        // Очищаем текущие ответы
        currentAnswers.clear()
    }

    override fun loadFragmentDataFromJSON(){
        val pf = parentFragment
        if (pf is TaskFragment) {
            qst = pf.getCurrentTask()
            if (qst != null) {
            } else {
                Log.d("DEBUG", "Task = null! Что-то не так")
                return
            }
        }
        // Загрузка задний, пропусков, вариантов ответов, ответы
        parsedTemplate()
        parsedBlank()
        parsedOptions()
        parsedAnswer()

        showItem()

    }

        override fun parsedAnswer() {
            val t = qst?.get("answers")
            answers = JsonUtils.getAnswersMap(t)
            answers.forEach { (column, list) ->
                Log.d("DEBUG", "$column -> $list")
            }
        }

    override fun parsedTemplate() {
        template= JsonUtils.getStringList(qst!!.get("template")!!)
    }

    override fun parsedBlank() {
        blanks= qst!!.get("blanks")!!.jsonArray.map { it.jsonPrimitive.content }
    }

    override fun parsedOptions() {
        val optionsJson = qst!!.jsonObject["options"]?.jsonObject
        val columnsMap = optionsJson?.get("columns")?.jsonObject?.mapValues { it.value.jsonPrimitive.content } ?: emptyMap()

        val leftColumnName = columnsMap["left"] ?: "Left Column"
        val rightColumnName = columnsMap["right"] ?: "Right Column"

// Подставляем в TextView
        binding.labelTopLeft.text = leftColumnName
        binding.labelTopRight.text = rightColumnName
    }

    override fun showItem() {
        val context = requireContext()
        val container = binding.containerBottom

        container.removeAllViews() // на всякий случай очищаем

        template.forEach { name ->
            val drag = DragTextView(context, name)

            val params = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 4, 8, 4)
            drag.layoutParams = params

            container.addView(drag)
        }

        container.setPadding(16, 0, 16, 0)

        attachRelayoutOnClick(container)
    }

    override fun collectClientAnswer() {
        // создаём пустые списки для каждой колонки
        val leftAnswers = mutableListOf<DragTextView>()
        val rightAnswers = mutableListOf<DragTextView>()

        flexboxListWithBlank.forEach { flexbox ->
            val textValues = flexbox.getTextValues().filter { !it.text.isNullOrEmpty() }
            if (textValues.isEmpty()) return@forEach

            when (flexbox.id) {
                R.id.container_top_left -> leftAnswers.addAll(textValues)
                R.id.container_top_right -> rightAnswers.addAll(textValues)
            }
        }

        // сохраняем в currentAnswers, чтобы потом можно было проверять
        currentAnswers["left"] = leftAnswers
        currentAnswers["right"] = rightAnswers

        Log.d("DEBUG_SCATTER", "currentAnswers: " +
                "left -> ${leftAnswers.map { it.text }}, " +
                "right -> ${rightAnswers.map { it.text }}")
    }

    override fun createBaseTask(): BaseTask {
        return TaskScatterWords(
            id = _session.taskID.toString(),
            taskType = TaskType.valueOf(_session.taskType!!),
            difficulty = _session.classID.toString(),
            language = _session.language!!,
            topic = _session.topic!!,
            userAnswers = buildAnswerList()
        )
    }

    override fun updateClientTaskSession(bt: BaseTask): BaseTask {
        if (bt !is TaskScatterWords) {
            throw IllegalArgumentException(
                "Critical Error: updateClientTaskSession expects TaskScatterWords, got ${bt::class.simpleName}"
            )
        }

        bt.userAnswers=buildAnswerList()

        val je = qst!!.get("answers")!!

        // проверку через TaskChecker
        val result = TaskChecker.checkAnswer(TaskType.SCATTER, bt, je)

        // Сохраняем результат в сессии
        ClientTaskSession.updateTaskResult(_session.taskID.toString(), result)
        result.log()

        return bt
    }

    private fun buildAnswerList(): MutableMap<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()

        currentAnswers.forEach { (side, dragViews) ->
            result[side] = dragViews.map { it.text.toString() }
        }

        return result
    }


    override fun loadFragmentFromSession(bt: BaseTask) {
        if (bt !is TaskScatterWords) {
            Log.d("loadFragment", "Task is not TaskScatterWords!")
            return
        }

        val answersMap = bt.userAnswers

        // Логируем сразу
        if (answersMap == null) {
            Log.d("loadFragment", "No answers in session (answersMap is null)")
            return
        }

        if (answersMap.isEmpty()) {
            Log.d("loadFragment", "Answers map is empty")
            return
        }

        Log.d("loadFragment", "Loaded answers from session: $answersMap")


        answersMap.forEach { (side, words) ->
            // Находим соответствующий flexbox
            val targetFlexbox = when (side) {
                "left" -> flexboxListWithBlank.find { it.id == R.id.container_top_left }
                "right" -> flexboxListWithBlank.find { it.id == R.id.container_top_right }
                else -> null
            } ?: return@forEach

            words.forEach { word ->
                // Ищем DragTextView в containerBottom
                for (i in 0 until binding.containerBottom.childCount) {
                    val dragTextView = binding.containerBottom.getChildAt(i) as? DragTextView ?: continue
                    if (dragTextView.text == word) {
                        val removed = dragTextView.removeFromPlaceHolder() // убираем из containerBottom
                        removed?.insertIntoNewPlace(targetFlexbox)         // вставляем в нужный flexbox
                        break // нашли нужный элемент, выходим из цикла
                    }
                }
            }
        }
    }

    override fun compareAnswers() {
        // ---------------------------
        // 1. Лог текущих DragTextView
        // ---------------------------
        currentAnswers.forEach { (column, dragList) ->
            val texts = dragList.map { it.text.toString().trim() }
            Log.d("DEBUG_SCATTER", "User answers in '$column': $texts")
        }

        // ---------------------------
        // 2. Лог правильных ответов
        // ---------------------------
        answers.forEach { (column, correctList) ->
            Log.d("DEBUG_SCATTER", "Correct answers in '$column': ${correctList.map { it.trim() }}")
        }

        // ---------------------------
        // 3. Проверяем ответы по колонкам
        // ---------------------------
        val perAnswerLeft = mutableListOf<Boolean>()
        val perAnswerRight = mutableListOf<Boolean>()

        val correctAnswersLeft = answers["left"]?.map { it.trim() } ?: emptyList()
        val correctAnswersRight = answers["right"]?.map { it.trim() } ?: emptyList()

        val leftAnswers = currentAnswers["left"] ?: emptyList()
        val rightAnswers = currentAnswers["right"] ?: emptyList()

        // Проверка left
        leftAnswers.forEach { dragTextView ->
            val word = dragTextView.text.toString().trim()
            val isCorrect = correctAnswersLeft.contains(word)
            perAnswerLeft.add(isCorrect)
            Log.d("DEBUG_SCATTER", "Left: Checking '$word' -> $isCorrect")
        }

        // Проверка right
        rightAnswers.forEach { dragTextView ->
            val word = dragTextView.text.toString().trim()
            val isCorrect = correctAnswersRight.contains(word)
            perAnswerRight.add(isCorrect)
            Log.d("DEBUG_SCATTER", "Right: Checking '$word' -> $isCorrect")
        }


        // ---------------------------
        // 5. Обновляем статусы DragTextView
        // ---------------------------
        leftAnswers.forEachIndexed { index, dragTextView ->
            if (perAnswerLeft.getOrNull(index) == true) dragTextView.correctStatus()
            else dragTextView.incorrectStatus()
        }

        rightAnswers.forEachIndexed { index, dragTextView ->
            if (perAnswerRight.getOrNull(index) == true) dragTextView.correctStatus()
            else dragTextView.incorrectStatus()
        }
    }

    private fun attachRelayoutOnClick(container: ViewGroup) {
        container.setOnClickListener {
            container.requestLayout()
            container.invalidate()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ScatterIntoGroupsFragment()
    }
}



class UnifiedTouchScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : NestedScrollView(context, attrs) {

    var touchListener: ((MotionEvent) -> Unit)? = null

    var blockScroll = false

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        touchListener?.invoke(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (blockScroll) {
            return false // ❗ не перехватываем → скролл не начнётся
        }
        return super.onInterceptTouchEvent(ev)
    }
}
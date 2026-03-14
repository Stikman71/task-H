package com.helper.TaskFragments.sentencesType

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.forEach
import com.helper.DataManager.ClientTaskSession
import com.helper.Logic.BaseTask
import com.helper.Logic.TaskSentences
import com.helper.Logic.TaskType
import com.helper.TaskFragment
import com.helper.TaskFragments.TaskFragmentBase
import com.helper.TaskFragments.sentencesType.Models.DragTextView
import com.helper.TaskFragments.sentencesType.layout.ExtendFlexboxLayout
import com.helper.databinding.FragmentSentenceBinding
import kotlin.toString

import com.google.android.flexbox.FlexboxLayout.LayoutParams
import com.helper.Logic.AnswerCheckResult
import com.helper.Logic.JSON.JsonUtils
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

//class SentenceFragment : Fragment() {
//
//
//    // TODO: TEST
//
//    val _session = AppSession_testing(
//        taskType= TaskType.SENTENCE.toString(),
//        classId = 5,
//        language = "ru",
//        topic = "test",
//        taskID = 1
//
//    )
//
//    // TODO: TEST
//
//    lateinit var binding: FragmentSentenceBinding
//
//    val flexboxList = mutableListOf<ExtendFlexboxLayout>()
//
//    private lateinit var currentTask: CurrentTask
//    val qst=APIQuestion()
//    val currentAnswers = mutableListOf<DragTextView>()
//    private fun flexParams(
//        left: Int = 0,
//        top: Int = 0,
//        right: Int = 0,
//        bottom: Int = 0
//    ) = FlexboxLayout.LayoutParams(
//        FlexboxLayout.LayoutParams.WRAP_CONTENT,
//        FlexboxLayout.LayoutParams.WRAP_CONTENT
//    ).apply {
//        setMargins(left, top, right, bottom)
//    }
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentSentenceBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//
//    @SuppressLint("ClickableViewAccessibility")
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        /*
//        Тип задания
//        Язык
//        Класс
//        Уникальный идентификатор задания
//         */
//
//
//        qst.setData(JsonReader.readJson(
//            requireContext(),
//            folder1 = _session.taskType!!,
//            folder2 = _session.language!!,
//            _class = _session.classId.toString()
//            )
//        )
//        qst.updateTopic(_session.topic.toString())
//        qst.updateQuestion(_session.taskID.toString())
//        val words=prepareTemplate(qst)
//
//
//        Log.d("SentenceFragment", words.toString())
//
//        setContainerTemplate(words)
//        setContainerAnswer(qst.options?.toList())
//
//    }
//
//    private fun prepareTemplate(q:APIQuestion): List<String> {
//        val template = q.template
//        val blanks = q.blanks ?: emptyList()
//
//        val words = template.split(" ").toMutableList()
//
//        val underscoreCount = words.count { it == "_" }
//        if (underscoreCount != blanks.size) {
//            throw IllegalArgumentException(
//                "Количество пропусков (_) ($underscoreCount) не совпадает с размером blanks (${blanks.size})"
//            )
//        }
//        // Индекс для blanks
//        var blankIndex = 0
//        for (i in words.indices) {
//            if (words[i] == "_") {
//                words[i] = blanks[blankIndex]
//                blankIndex++
//            }
//        }
//        return words
//    }
//
//    private fun setContainerTemplate(words: List<String>? = null) {
//
//        val context = requireContext()
//        val container = binding.container1
//
//        val list = words?.takeIf { it.isNotEmpty() }
//            ?: List(10) { "Word_${it + 1}" }
//
//        list.forEach { name ->
//
//            if ('_' in name) {
//
//                val flexbox = ExtendFlexboxLayout(context, name = name).apply {
//                    layoutParams = flexParams()
//                    setMaxChild(1)
//                    setBackgroundColor(Color.BLACK)
//
//                    val drag = DragTextView(context, name)
//                    addView(drag)
//                }
//                flexboxList.add(flexbox)
//                container.addView(flexbox)
//
//            } else {
//
//                val textView = TextView(context).apply {
//                    text = name
//                    layoutParams = flexParams(12, 8, 12, 8)
//                }
//
//                container.addView(textView)
//            }
//        }
//
//        attachRelayoutOnClick(container)
//    }
//
//    private fun setContainerAnswer(words: List<String>? = null) {
//
//        val context = requireContext()
//        val container = binding.container2
//
//        val list = words?.takeIf { it.isNotEmpty() }
//            ?: List(10) { "Word ${it + 1}" }
//
//        list.forEach { name ->
//
//            val drag = DragTextView(context, name)
//
//            container.addView(drag)
//        }
//
//        attachRelayoutOnClick(container)
//    }
//
//    private fun attachRelayoutOnClick(container: ViewGroup) {
//        container.setOnClickListener {
//            container.requestLayout()
//            container.invalidate()
//        }
//    }
//
//    // Метод для кнопки "Сбросить"
//    private fun onResetClicked() {
//        Log.d("ButtonClick", "Сбросить")
//
//        val _tmp = mutableListOf<DragTextView>()
//
//        flexboxList.forEach { flexbox ->
//            _tmp.addAll(flexbox.getTextValues()) // добавляем все тексты из flexbox
//        }
//        if (_tmp.isNotEmpty()) {
//            _tmp.forEach {
//                it.removeFromPlaceHolder()
//                it.insertIntoNewPlace(binding.container2)
//            }
//        }
//
//    }
//
//    // Метод для кнопки "Проверить"
//    private fun onCheckClicked() {
//        Log.d("ButtonClick", "Проверить")
//
//        currentAnswers.clear()
//        flexboxList.forEach { flexbox ->
//            currentAnswers.addAll(flexbox.getTextValues()) // добавляем все тексты из flexbox
//        }
//
//        val correct = qst.answers?.let { answers ->
//            currentAnswers.size == answers.size && currentAnswers.zip(answers).all { (tmpValue, answerValue) ->
//                tmpValue.text == answerValue
//            }
//        } ?: false
//
//        if (correct) {
//            Log.d("CheckResult", "Ответ совпадает полностью!")
//        } else {
//            Log.d("CheckResult", "Ответ не совпадает!")
//        }
//
//        val taskSession=updateClientTaskSession()
//        if (taskSession!=null){
//            taskSession.isCorrect=correct
//        }
//        taskSession?.log()
//    }
//
//    private fun isSessionValid(): Boolean {
//        return _session.taskType != null &&
//                _session.classId != null &&
//                _session.language != null &&
//                _session.topic != null &&
//                _session.taskID != null &&
//                enumValues<TaskType>().any { it.name == _session.taskType }
//    }
//
//    private fun buildAnswerList(): MutableList<String> {
//        return currentAnswers.map {
//            it.text?.toString()?.takeIf { it.isNotEmpty() } ?: "None"
//        }.toMutableList()
//    }
//
//    fun updateClientTaskSession(): BaseTask? {
//
//        if (!isSessionValid()) {
//            println("Ошибка: Некорректные данные.")
//            return null
//        }
//
//        val taskType = TaskType.valueOf(_session.taskType!!)
//        val taskId = _session.taskID.toString()
//        val answers = buildAnswerList()
//
//        val existingTask = ClientTaskSession
//            .getTaskByTypeAndId(taskType, taskId)
//
//        val resultTask: BaseTask = if (existingTask is TaskSentences) {
//            existingTask.userAnswers.clear()
//            existingTask.userAnswers.addAll(answers)
//            Log.d("Task Info", "Task updated")
//            existingTask
//        } else {
//            // ➕ Создаём новую
//            val newTask = TaskSentences(
//                id = taskId,
//                taskType = taskType,
//                difficulty = _session.classId.toString(),
//                language = _session.language!!,
//                topic = _session.topic!!,
//                userAnswers = answers
//            )
//            ClientTaskSession.addTask(newTask)
//            Log.d("Task Info", "Task created")
//            newTask
//        }
//        Log.d("Task Info", "ClientTaskSession Count=${ClientTaskSession.getTaskCount()}")
//
//        return resultTask
//    }
//
//    companion object {
//        @JvmStatic
//        fun newInstance() = SentenceFragment()
//    }
//}

class SentenceFragment : TaskFragmentBase<FragmentSentenceBinding>() {
    val flexboxListWithBlank = mutableListOf<ExtendFlexboxLayout>()
    // JSON
    private lateinit var template: String
    private lateinit var answers: List<String>
    private lateinit var blanks: List<String>
    private lateinit var options: List<String>
    // JSON
    val currentAnswers: MutableMap<Int, DragTextView> = mutableMapOf()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSentenceBinding {
        return FragmentSentenceBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFragmentDataFromJSON()
        ClientTaskSession
            .getTaskByTypeAndId(TaskType.valueOf(_session.taskType!!), _session.taskID.toString())
            ?.let { task ->
                loadFragmentFromSession(task)
            }
    }

    override fun checkButtonIsPressed() {
        Log.d("ButtonClick", "Проверить")
        compareAnswers()

        saveClientTaskSession()

        return
    }

    // Метод для кнопки "Сбросить"
    override fun clearButtonIsPressed() {
        collectClientAnswer()
        Log.d("ButtonClick", "Сбросить")
        Log.d("ButtonClick", currentAnswers.toString())
        currentAnswers.forEach { (_, dragTextView) ->
            Log.d("ButtonClick", "Сбросить")
            val _tmp=dragTextView.removeFromPlaceHolder()
            _tmp?.correctStatus()
            _tmp?.insertIntoNewPlace(binding.container2)

        }
        currentAnswers.clear()  // очищаем предыдущие ответы
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

        val words=prepareTemplate()
        setContainerTemplate(words)
        setContainerAnswer()

    }

    override fun parsedAnswer() {
        val t = qst?.get("answers")
//        answers=if (t != null && t is JsonArray) {
//            t.map { it.jsonPrimitive.content }
//        } else {
//            emptyList()
//        }
        answers=JsonUtils.getStringList(t)
        Log.d("DEBUG", answers.toString())
    }

    override fun parsedTemplate() {
        template= qst!!.get("template")!!.jsonPrimitive.content
    }

    override fun parsedBlank() {
        blanks= qst!!.get("blanks")!!.jsonArray.map { it.jsonPrimitive.content }
    }

    override fun parsedOptions() {
        options= qst!!.get("options")!!.jsonArray.map { it.jsonPrimitive.content }
    }

    override fun collectClientAnswer() {
        currentAnswers.clear()
        flexboxListWithBlank.forEachIndexed  { index, flexbox ->
            val textValues = flexbox.getTextValues()
            if (textValues.isNotEmpty()) {
                if (textValues.all { it.text.isNullOrEmpty() }) {
                    return@forEachIndexed
                }
                currentAnswers[index] = textValues[0]
            }
        }
        Log.d("DEBUG", "currentAnswers: ${currentAnswers.map { (key, value) -> "$key -> ${value.text}" }.joinToString(", ")}")
    }

    override fun createBaseTask(): BaseTask {
        return TaskSentences(
            id = _session.taskID.toString(),
            taskType = TaskType.valueOf(_session.taskType!!),
            difficulty = _session.classID.toString(),
            language = _session.language!!,
            topic = _session.topic!!,
            userAnswers = buildAnswerList()
        )
    }

    override fun updateClientTaskSession(bt: BaseTask): BaseTask {
        if (bt !is TaskSentences) {
            throw IllegalArgumentException("Critical Error: updateClientTaskSession expects TaskSentences, got ${bt::class.simpleName}")
        }
        bt.userAnswers.clear()
        bt.userAnswers.addAll(buildAnswerList())
//        Log.d("TASKINFO", buildAnswerList().toString())
//        Log.d("TASKINFO", "Task updated")
        return bt
    }


    private fun prepareTemplate(): List<String> {
        val words: MutableList<String> = template.split(" ").toMutableList()

        val underscoreCount = words.count { it == "_" }
        if (underscoreCount != blanks.size) {
            throw IllegalArgumentException(
                "Количество пропусков (_) ($underscoreCount) не совпадает с размером blanks (${blanks.size})"
            )
        }
        // Индекс для blanks
        var blankIndex = 0
        for (i in words.indices) {
            if (words[i] == "_") {
                words[i] = blanks[blankIndex]
                blankIndex++
            }
        }
        return words
    }
    private fun setContainerTemplate(words: List<String>? = null) {
        // Заполняем поля с целевым предложением
        val context = requireContext()
        val container = binding.container1

        val list = words?.takeIf { it.isNotEmpty() }
            ?: List(10) { "Word_${it + 1}" }

        list.forEach { name ->
            if ('_' in name) {
                // Создание ExtendFlexboxLayout для элементов с "_"
                val flexbox = ExtendFlexboxLayout(context, name = name).apply {
                    setMaxChild(1)
                    //setBackgroundColor(Color.BLACK)

                    // Создание DragTextView для перемещения текста
                    val drag = DragTextView(context, name)
                    addView(drag)
                }

                flexboxListWithBlank.add(flexbox)
                container.addView(flexbox)
            } else {
                // Создание TextView для обычных элементов
                val textView = TextView(context).apply {
                    text = name
                    // Применяем отступы и выравнивание для TextView
                    val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    layoutParams.setMargins(8, 8, 8, 8)  // Отступы между элементами
                    this.layoutParams = layoutParams
                    gravity = Gravity.CENTER_VERTICAL  // Вертикальное выравнивание по центру
                }
                container.addView(textView)
            }
        }
        attachRelayoutOnClick(container)
    }

    private fun setContainerAnswer() {
        // Поля содержащие варианты ответов для целевого предложения
        val context = requireContext()
        val container = binding.container2

        val list = options.takeIf { it.isNotEmpty() }
            ?: List(10) { "Word ${it + 1}" }

        list.forEach { name ->
            val drag = DragTextView(context, name)
            container.addView(drag)
        }
        attachRelayoutOnClick(container)
    }

    private fun buildAnswerList(): MutableList<String> {
        val result = mutableListOf<String>()
        val totalSize = flexboxListWithBlank.size

        for (index in 0 until totalSize) {
            val t=currentAnswers.getOrElse(index){""}
            if (t is DragTextView) {
                result.add(t.text.toString())
            }else{
                result.add("")
            }
        }
        return result
    }


    override fun loadFragmentFromSession(bt: BaseTask) {
        if (bt == null) {
            Log.d("loadFragment", "Task is null!")
            return
        }
        val wordsToFind = bt.loadAnswers()
        wordsToFind.forEachIndexed { index, word ->
            if (word.isNotEmpty()) {
                // Ищем DragTextView с этим текстом в container2
                binding.container2.forEach {
                    if (it is DragTextView && it.text == word) {
                        it.insertIntoNewPlace(flexboxListWithBlank[index])
                        it.removeFromPlaceHolder()
                        return@forEach
                    }
                }
            } else {
                // TODO:
            }
        }
    }

    override fun compareAnswers() {
        collectClientAnswer() // собираем ответы пользователя в currentAnswers

        // Сравнение по индексам: позиция важна
        val perAnswer: List<Boolean> = answers.mapIndexed { index, correctWord ->
            val userWord = currentAnswers[index]?.text?.toString() ?: ""
            userWord == correctWord
        }

        val correctCount = perAnswer.count { it }          // совпадения по позиции
        val totalCount = answers.size                      // всего правильных слов
        val percentage = if (totalCount > 0) (correctCount.toDouble() / totalCount) * 100 else 0.0

        // создаём объект только с теми полями, которые есть
        val result = AnswerCheckResult(
            correctCount = correctCount,
            totalCount = totalCount,
            percentage = percentage
        )

        ClientTaskSession.updateTaskResult(_session.taskID.toString(), result)
        result.log()

        currentAnswers.forEach { (index, dragTextView) ->
            val isCorrect = perAnswer.getOrNull(index) ?: false
            if (isCorrect) {
                // правильный ответ
                dragTextView.correctStatus()
            } else {
                // неправильный ответ
                dragTextView.incorrectStatus()
            }
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
        fun newInstance() = SentenceFragment()
    }
}
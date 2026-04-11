package com.helper.TaskFragments.sentencesType


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.helper.Logic.JSON.JsonUtils
import com.helper.Logic.TaskChecker
import com.helper.TaskFragments.sentencesType.Models.SentenceAdapter
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive



class SentenceFragment : TaskFragmentBase<FragmentSentenceBinding>() {
    val flexboxListWithBlank = mutableListOf<ExtendFlexboxLayout>()
    // JSON
    private lateinit var template: List<String>
    private lateinit var answers: List<List<String>>
    private lateinit var blanks: List<List<String>>
    private lateinit var options: List<List<String>>
    // JSON
    val currentAnswers: MutableMap<Int, MutableMap<Int, DragTextView>> = mutableMapOf()

    // XML:
    lateinit var recyclerView: RecyclerView

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSentenceBinding {
        return FragmentSentenceBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView=binding.sentencesRecyclerView
        loadFragmentDataFromJSON()
        recyclerView.post {

            ClientTaskSession
                .getTaskByTypeAndId(TaskType.valueOf(_session.taskType!!), _session.taskID.toString())
                ?.let { task ->
                    loadFragmentFromSession(task)
                }
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
        collectClientAnswer()
        resetAnswers()
        currentAnswers.clear()  // очищаем предыдущие ответы
    }

    fun resetAnswers() {
        val adapter = recyclerView.adapter as? SentenceAdapter ?: run {
            Log.d("DEBUG", "Adapter is null")
            return
        }

        Log.d("DEBUG", "Resetting answers for ${currentAnswers.size} sentences")

        currentAnswers.forEach { (sentenceIndex, blanksMap) ->
            Log.d("DEBUG", "Sentence index: $sentenceIndex with ${blanksMap.size} blanks")

            blanksMap.entries.sortedBy { it.key }.forEach { (blankIndex, dragTextView) ->
                Log.d("DEBUG", "Processing blank $blankIndex with text: '${dragTextView.text}'")

                // Удаляем DragTextView из текущего родителя
                val tmp = dragTextView.removeFromPlaceHolder()
                if (tmp != null) {
                    Log.d("DEBUG", "Removed DragTextView '${tmp.text}' from its placeholder")
                } else {
                    Log.d("DEBUG", "DragTextView '${dragTextView.text}' had no parent to remove from")
                }

                // Сбрасываем статус
                dragTextView.defaultStatus()
                Log.d("DEBUG", "Default status applied to DragTextView '${dragTextView.text}'")

                // Получаем holder для текущего предложения
                val holder= getRecyclerHoled(sentenceIndex)
                if (holder != null) {
                    val answersContainer = holder.answersContainer
                    tmp?.insertIntoNewPlace(answersContainer)
                    tmp?.applySafeLayoutParams(50,50,50,50)
                    Log.d("DEBUG", "Inserted DragTextView '${dragTextView.text}' back into answersContainer")
                } else {
                    Log.d("DEBUG", "ViewHolder for sentence $sentenceIndex is not visible yet")
                }
            }
        }

        Log.d("DEBUG", "Reset complete")
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
        Log.d("MyDebug", "template: $template")
        parsedBlank()
        blanks.forEachIndexed { index, list ->
            Log.d("MyDebug", "blanks[$index]: $list")
        }
        parsedOptions()
        options.forEachIndexed { index, list ->
            Log.d("MyDebug", "options[$index]: $list")
        }
        parsedAnswer()
        answers.forEachIndexed { index, list ->
            Log.d("MyDebug", "answers[$index]: $list")
        }


        showItem()

    }

    override fun parsedAnswer() {
        val t = qst?.get("answers")
        answers=JsonUtils.getListOfStringLists(t)
    }

    override fun parsedTemplate() {
        template = qst?.get("template")!!.jsonArray.map { it.jsonPrimitive.content }
    }

    override fun parsedBlank() {
        blanks = qst!!.get("blanks")!!.jsonArray.map { inner ->
            inner.jsonArray.map { it.jsonPrimitive.content }
        }
    }

    override fun parsedOptions() {
        options = qst!!.get("options")!!.jsonArray.map { inner ->
            inner.jsonArray.map { it.jsonPrimitive.content }
        }
    }

    override fun showItem() {

        val items: List<SentenceItem> = template.indices.map { i ->
            SentenceItem(
                template = template[i],
                blanks = blanks.getOrNull(i) ?: listOf(),
                options = options.getOrNull(i) ?: listOf()
            )
        }

        val adapter = SentenceAdapter(items)
        binding.sentencesRecyclerView.adapter = adapter
        binding.sentencesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun collectClientAnswer() {
        currentAnswers.clear()
        val recyclerView = binding.sentencesRecyclerView
        val adapter = recyclerView.adapter as? SentenceAdapter ?: return

        adapter.items.forEachIndexed { sentenceIndex, _ ->
            val holder = recyclerView.findViewHolderForAdapterPosition(sentenceIndex)
                    as? SentenceAdapter.SentenceViewHolder ?: return@forEachIndexed

            val sentenceAnswers = mutableMapOf<Int, DragTextView>()
            var blankIndex = 0

            holder.sentenceFlexbox.children.forEach { view ->
                if (view is ExtendFlexboxLayout) {
                    val textValues = view.getTextValues()
                    val drag = if (textValues.isNotEmpty() && !textValues[0].text.isNullOrEmpty()) {
                        textValues[0]
                    } else {
                        // Создаём пустой DragTextView, если blank пустой
                        DragTextView(requireContext(), "")
                    }
                    sentenceAnswers[blankIndex] = drag
                    blankIndex++
                }
            }

            if (sentenceAnswers.isNotEmpty()) {
                currentAnswers[sentenceIndex] = sentenceAnswers
            }
        }

        // Для проверки: выводим текстовые значения
        currentAnswers.forEach { (sentenceIndex, blanksMap) ->
            val texts = blanksMap.entries.sortedBy { it.key }.map { it.value.text }
            Log.d("DEBUG", "Sentence $sentenceIndex answers: $texts")
        }
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
        if (bt !is TaskSentences) throw IllegalArgumentException("Expected TaskSentences")

        collectClientAnswer()  // собираем текущие DragTextView в currentAnswers

        // Собираем список ответов по flexboxListWithBlank
        bt.userAnswers = buildAnswerList()

        // Лог для проверки
        Log.e("DEBUG", "Saving userAnswers to session: ${bt.userAnswers}, bt hash: ${bt.hashCode()}")

        val je = qst!!.get("answers")!!
        val result = TaskChecker.checkAnswer(TaskType.SENTENCE, bt, je)
        ClientTaskSession.updateTaskResult(_session.taskID.toString(), result)
        result.log()

        return bt
    }

    private fun buildAnswerList(): MutableList<MutableList<String>> {
        val result = mutableListOf<MutableList<String>>()

        currentAnswers.forEach { (sentenceIndex, blanksMap) ->
            val texts = blanksMap.entries
                .sortedBy { it.key }
                .map { it.value.text.toString() } // <-- превращаем CharSequence в String
                .toMutableList()

            result.add(texts)
        }

        // Логируем для проверки
        result.forEachIndexed { sentenceIndex, texts ->
            Log.d("DEBUG|CurrentAnswersTexts", "Sentence $sentenceIndex answers: $texts")
        }

        return result
    }


    override fun loadFragmentFromSession(bt: BaseTask) {
        if (bt !is TaskSentences) {
            Log.d("loadFragment", "Task is null!")
            return
        }

        val wordsToFind = bt.userAnswers

        wordsToFind.forEachIndexed { sentenceIndex, blanks ->
            Log.d("DEBUG|Answers", "=== Предложение $sentenceIndex ===")
            val holder = getRecyclerHoled(sentenceIndex)
            if (holder == null) {
                Log.d("DEBUG|Answers", "Holder для предложения $sentenceIndex = null, itemCount=${recyclerView.adapter?.itemCount}")
                return@forEachIndexed
            }

            val blanksList = holder.blanksList
            val answersContainer = holder.answersContainer

            blanks.forEachIndexed { blankIndex, word ->
                Log.d("DEBUG|Answers", "Обрабатываем blankIndex=$blankIndex, слово='$word'")

                if (word.isEmpty()) {
                    Log.d("DEBUG|Answers", "Слово пустое, пропускаем")
                    return@forEachIndexed
                }

                val dragAnswer = answersContainer.children
                    .filterIsInstance<DragTextView>()
                    .firstOrNull { it.text.toString() == word }

                if (dragAnswer == null) {
                    Log.d("DEBUG|Answers", "DragTextView с текстом '$word' не найден в answersContainer")
                }

                val targetBlank = blanksList.getOrNull(blankIndex)
                if (targetBlank == null) {
                    Log.d("DEBUG|Answers", "Пропуск blankIndex=$blankIndex не найден")
                }

                if (dragAnswer != null && targetBlank != null) {
                    Log.d(
                        "DEBUG|Answers",
                        "Вставляем DragTextView '$word' в пропуск $blankIndex"
                    )
                    dragAnswer.insertIntoNewPlace(targetBlank)
                    dragAnswer.removeFromPlaceHolder()
                }

                Log.d("DEBUG|Answers", "Ответ $blankIndex : $word обработан")
            }
        }
    }

    override fun compareAnswers() {
        // Сравнение по индексам: позиция важна
        for ((sentenceIndex, userMap) in currentAnswers) {
            val correctList = answers.getOrNull(sentenceIndex) ?: continue

            for ((blankIndex, dragTextView) in userMap) {
                val userWord = dragTextView.text?.toString() ?: ""
                val correctWord = correctList.getOrNull(blankIndex) ?: ""

                if (userWord == correctWord) {
                    dragTextView.correctStatus()   // правильный
                } else {
                    dragTextView.incorrectStatus() // неправильный
                }
            }
        }
    }

    private fun attachRelayoutOnClick(container: ViewGroup) {
        container.setOnClickListener {
            container.requestLayout()
            container.invalidate()
        }
    }

    private fun getRecyclerHoled(index: Int): SentenceAdapter.SentenceViewHolder? {
        return recyclerView.findViewHolderForAdapterPosition(index)
                as? SentenceAdapter.SentenceViewHolder
    }

    companion object {
        @JvmStatic
        fun newInstance() = SentenceFragment()
    }
}


data class SentenceItem(
    val template: String,
    val blanks: List<String>,
    val options: List<String> // список drag-ответов для этого предложения
)
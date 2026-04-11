package com.helper.TaskFragments.TextInput

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.helper.DataManager.ClientTaskSession
import com.helper.Logic.BaseTask
import com.helper.Logic.JSON.JsonUtils
import com.helper.Logic.TaskChecker
import com.helper.Logic.TaskFillTheBlank
import com.helper.Logic.TaskScatterWords
import com.helper.Logic.TaskTextInput
import com.helper.Logic.TaskType
import com.helper.R
import com.helper.TaskFragment
import com.helper.TaskFragments.TaskFragmentBase
import com.helper.TaskFragments.TextInput.adapter.TextInputAdapter
import com.helper.databinding.FragmentTaskTextInputBinding
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class TextInputFragment : TaskFragmentBase<FragmentTaskTextInputBinding>() {

    private lateinit var template: List<String>
    private lateinit var answers: List<String>
    private lateinit var blanks: List<String>
    private var options: String=""
    // JSON
    private val currentAnswers = mutableListOf<String>()


    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTaskTextInputBinding {
        return FragmentTaskTextInputBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.setOnTouchListener { rv, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val child = (rv as RecyclerView).findChildViewUnder(event.x, event.y)
                if (child == null) {
                    // Кликнули по пустому месту
                    rv.clearFocus()
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.recyclerView.windowToken, 0)
                }
            }
            false
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        loadFragmentDataFromJSON()

        ClientTaskSession
            .getTaskByTypeAndId(TaskType.valueOf(_session.taskType!!), _session.taskID.toString())
            ?.let { task ->
                loadFragmentFromSession(task)
            }
    }

    override fun checkButtonIsPressed() {
        saveClientTaskSession()

    }

    override fun clearButtonIsPressed() {
        TODO("Not yet implemented")
    }

    override fun loadFragmentDataFromJSON() {
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

    override fun loadFragmentFromSession(bt: BaseTask) {
        if (bt !is TaskTextInput) return

        // Обновляем список currentAnswers
        currentAnswers.clear()
        currentAnswers.addAll(bt.userAnswers)

        // Обновляем адаптер — перерисует все видимые и невидимые элементы
        val adapter = binding.recyclerView.adapter as? TextInputAdapter
        adapter?.let {
            it.notifyDataSetChanged()
        }
    }

    override fun compareAnswers() {
        collectClientAnswer()
    }

    override fun parsedAnswer() {
        //answers=JsonUtils.getStringList(qst!!.get("answers")!!)
    }

    override fun parsedTemplate() {
        template=JsonUtils.getStringList(qst!!.get("template")!!)
    }

    override fun parsedBlank() {
    }

    override fun parsedOptions() {
        val optionsElement = qst?.get("options")
        if (optionsElement is JsonObject) {
            // только если это объект, берём text_task
            options = optionsElement["text_task"]?.jsonPrimitive?.content ?: ""
            Log.e("DEBUG", options)
        }
        // если options нет или не объект — ничего не делаем
    }
    override fun showItem() {
        if(options != "") {
            binding.taskText.text = options
        }

        // Инициализация currentAnswers пустыми строками
        currentAnswers.clear()
        currentAnswers.addAll(List(template.size) { "" })

        val adapter = TextInputAdapter(template, currentAnswers) { index, answer ->
            // можно что-то делать при изменении текста
        }

        binding.recyclerView.adapter = adapter
    }

    override fun collectClientAnswer() {
        //
    }

    override fun createBaseTask(): BaseTask {
        return TaskTextInput(
            id = _session.taskID.toString(),
            taskType = TaskType.valueOf(_session.taskType!!),
            difficulty = _session.classID.toString(),
            language = _session.language!!,
            topic = _session.topic!!,
            userAnswers = buildAnswerList()
        )
    }

    private fun buildAnswerList(): MutableList<String> {
        collectClientAnswer()
        return currentAnswers
    }

    override fun updateClientTaskSession(bt: BaseTask): BaseTask {
        if (bt !is TaskTextInput) {
            throw IllegalArgumentException(
                "Critical Error: updateClientTaskSession expects TaskScatterWords, got ${bt::class.simpleName}"
            )
        }

        bt.userAnswers=currentAnswers

        val je = qst!!.get("answers")!!

        // проверку через TaskChecker
        val result = TaskChecker.checkAnswer(TaskType.TEXTINPUT, bt, je)

        // Сохраняем результат в сессии
        ClientTaskSession.updateTaskResult(_session.taskID.toString(), result)
        result.log()

        return bt
    }

    companion object {
        @JvmStatic
        fun newInstance() = TextInputFragment()
    }
}
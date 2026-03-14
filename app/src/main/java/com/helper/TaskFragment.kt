package com.helper

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.viewbinding.ViewBinding
import com.google.android.material.button.MaterialButton
import com.helper.DataManager.AppSession
import com.helper.DataManager.CheckTask_demo
import com.helper.DataManager.ClientTaskSession
import com.helper.DataManager.DataLC
import com.helper.Logic.TaskFragmentFactory
import com.helper.Logic.TaskType
import com.helper.TaskFragments.EmptyTaskFragment
import com.helper.TaskFragments.TaskFragmentBase
import com.helper.Logic.JSON.JSONHandler
import com.helper.Logic.JSON.PathBuilder
import com.helper.Logic.JSON.sanitizeTopicName
import com.helper.databinding.FragmentTaskBinding
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.getValue


class TaskFragment : Fragment() {

    private  var taskTypes:MutableList<TaskType> = mutableListOf()
    private var taskIndexes:MutableList<String> = mutableListOf()
    private var currentFragment: TaskFragmentBase<out ViewBinding> = EmptyTaskFragment()
    var askJsonTask = JSONHandler()



    private val dataLC: DataLC by activityViewModels()
    lateinit var binding: FragmentTaskBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentTaskBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val task = arguments?.getString(ARG_TASK_TYPE)
        val jsonFileName = when (task) {
            "TASK" -> "task.json"
            "CHECK TASK" -> "check_task.json"
            else -> "task.json" // на всякий случай
        }
        // Чтение из аргементов
        val pathParts = arrayOf(
            "Tasks",
            dataLC.currentSession.value?.language!!,
            "Class_${dataLC.currentSession.value?.classID!!}",
            sanitizeTopicName(dataLC.currentSession.value?.topic!!),
            jsonFileName
        )

        askJsonTask = try {
            val path = PathBuilder.buildPathForTask(pathParts)
            Log.d("DEBUG_JSON", "Загружаем JSON из: $path")

            val handler = JSONHandler.loadFromJson(requireContext(), path)

            // Логируем, сколько top-level задач загрузилось
            Log.d("DEBUG_JSON", "JSON загружен. Всего задач: ${handler.getAllIdTypePairs().size}")

            handler
        } catch (e: Exception) {
            Log.e("DEBUG_JSON", "Ошибка при загрузке JSON: ${e.message}")
            e.printStackTrace()
            JSONHandler() // если ошибка — оставляем пустой handler
        }

        taskIndexes.clear()
        taskTypes.clear()
        askJsonTask.getAllIdTypePairs().forEach { (id, typeStr) ->

            val type = try {
                TaskType.valueOf(typeStr)
            } catch (e: IllegalArgumentException) {
                throw IllegalStateException("Unknown TaskType in JSON: $typeStr for id=$id")
            }
            taskIndexes.add(id)
            taskTypes.add(type)
        }

        val pairsLog = taskIndexes.zip(taskTypes)
            .joinToString(separator = "\n") { (id, type) -> "id = $id, type = $type" }
        Log.d("DEBUG", "All task pairs:\n$pairsLog")

        openTaskFragment(taskTypes[ClientTaskSession.currentIndex])

        binding.btnCompleteTask.setOnClickListener {
            val childFragments = childFragmentManager.fragments
            childFragments.forEach { fragment ->
                childFragmentManager.beginTransaction().remove(fragment).commit()
            }
            Log.d("DEBUG", "CheckTask:$task")

            if (task != "TASK") {
                // все кроме обычного TASK считаем контрольными
                val checkTask = CheckTask_demo(
                    classID = dataLC.currentSession.value?.classID,
                    language = dataLC.currentSession.value?.language,
                    topic = dataLC.currentSession.value?.topic,
                    taskID_global = dataLC.currentSession.value?.taskID_global
                )

                if (checkTask.taskID_global != null) {
                    dataLC.markTaskCompleted(checkTask)
                    //Toast.makeText(requireContext(), "Тест отмечен как пройденный", Toast.LENGTH_SHORT).show()
                }

                // Открываем OutFragment и очищаем стек до MAIN
                (activity as? MainActivity)?.OpenFragment(
                    OutFragment.newInstance(),
                    R.id.content_container,
                    clearUpToTag = "MAIN"            // удаляем все промежуточные фрагменты
                )
            } else {
                // Обычное задание — открываем OutFragment без очистки
                (activity as? MainActivity)?.OpenFragment(
                    OutFragment.newInstance(),
                    R.id.content_container
                )
            }
        }

        binding.btnNext.setOnClickListener { goNext() }
        binding.btnBack.setOnClickListener { goBack() }

        binding.btnReset.setOnClickListener {
            onResetClicked()
        }

        binding.btnCheck.setOnClickListener {
            onCheckClicked()
        }
        updateNavButtons()
    }




    private fun openTaskFragment(type: TaskType) {
        val fragment = TaskFragmentFactory.create(type)
        Log.d(
            "TaskFragment",
            "Открываем $type -> ${fragment::class.simpleName}"
        )
        childFragmentManager.beginTransaction()
            .replace(R.id.taskFragmentContainer, fragment) // фиксированный контейнер
            .commit()
        currentFragment = fragment
    }

    private fun goNext() {
        if (ClientTaskSession.currentIndex < taskTypes.size - 1) {
            ClientTaskSession.currentIndex++
            openTaskFragment(taskTypes[ClientTaskSession.currentIndex])
            updateNavButtons()
        }
    }

    private fun goBack() {
        if (ClientTaskSession.currentIndex > 0) {
            ClientTaskSession.currentIndex--
            openTaskFragment(taskTypes[ClientTaskSession.currentIndex])
            updateNavButtons()
        }
    }
    private fun updateNavButtons() {
        // Проверяем доступность кнопок
        val isBackEnabled = ClientTaskSession.currentIndex > 0
        val isNextEnabled = ClientTaskSession.currentIndex < taskTypes.size - 1
        // Обновляем состояние кнопок
        binding.btnBack.isEnabled = isBackEnabled
        binding.btnNext.isEnabled = isNextEnabled
        dataLC.currentSession.value =
            dataLC.currentSession.value?.copy(taskType = taskTypes[ClientTaskSession.currentIndex].toString())
        dataLC.currentSession.value =
            dataLC.currentSession.value?.copy(taskID = taskIndexes[ClientTaskSession.currentIndex].toInt())

        val jo=getCurrentTask()
        binding.tvTaskTitle.text = jo?.get("description")?.jsonPrimitive?.contentOrNull
    }

    private fun onResetClicked() {
        currentFragment.clearButtonIsPressed()
    }

    private fun onCheckClicked() {
        currentFragment.checkButtonIsPressed()
    }

    fun getCurrentTask(): JsonObject? {
        //return askJsonTask.subset(taskIndexes[currentIndex], taskTypes[currentIndex].toString())!!
        return askJsonTask.getTask(taskIndexes[ClientTaskSession.currentIndex], taskTypes[ClientTaskSession.currentIndex].toString())
    }
    fun getClientSession(): LiveData<AppSession> {
        return dataLC.currentSession
    }


    override fun onDestroy() {
        super.onDestroy()
        ClientTaskSession.currentIndex=0
        Log.d("DEBUG","${this.javaClass.simpleName} is destoyed")
    }

    companion object {
        private const val ARG_TASK_TYPE = "arg_start_task"

        fun newInstance(taskType: String?) = TaskFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TASK_TYPE, taskType)
            }
        }
    }
}
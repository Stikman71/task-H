package com.helper

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.helper.DataManager.ClientTaskSession
import com.helper.DataManager.DataLC
import com.helper.Logic.AnswerCheckResult
import com.helper.Logic.BaseTask
import com.helper.Logic.GradeScale
import com.helper.Logic.TaskChecker
import com.helper.Logic.TaskType
import com.helper.Logic.JSON.JSONHandler
import com.helper.Logic.JSON.JsonUtils
import com.helper.databinding.FragmentOutBinding
import kotlin.getValue

class OutFragment : Fragment() {

    private val dataLC: DataLC by activityViewModels()
    private lateinit var uncompletedTasks: List<Pair<String, TaskType>>
    private val completedTasks: MutableList<Pair<String, TaskType>> = mutableListOf()

    lateinit var binding: FragmentOutBinding

    private  var taskTypes:MutableList<TaskType> = mutableListOf()
    private var taskIndexes:MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentOutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        globalAnswerChecker()
        fillUserCard(
            dataLC.currentClient.value?.name!!,
            dataLC.currentClient.value?.sName!!,
            dataLC.currentSession.value?.classID.toString(),
            dataLC.currentClient.value?.grade!!
        )
    }

    fun fillUserCard(
        name: String,
        surname: String,
        classId: String,
        score: Int
    ) {
        binding.tvName.text = name
        binding.tvSName.text = surname
        binding.tvActivity.text = "Not using"
        binding.tvClassID.text = classId
        binding.tvTaskID.text = "Not using"
        binding.tvScore.text = score.toString()
    }

    private fun globalAnswerChecker() {
        getTaskFromJSON()

        val allTasks = taskIndexes.zip(taskTypes) // List<Pair<String, TaskType>>

        uncompletedTasks = allTasks.filter { (id, type) ->
            ClientTaskSession.getTaskByTypeAndId(type, id) == null
        }

        completedTasks.addAll(allTasks - uncompletedTasks.toSet())

        Log.d("GLOBAL_CHECK", "Всего заданий: ${allTasks.size}")
        Log.d("GLOBAL_CHECK", "Непройдено: ${uncompletedTasks.size}")
        uncompletedTasks.forEach { (id, type) ->
            Log.d("GLOBAL_CHECK", "Не выполнено: id=$id, type=$type")
        }

        val askJsonTask=JSONHandler.loadFromJson()

        completedTasks.forEach { (id, type) ->
            val task = ClientTaskSession.getTaskByTypeAndId(type, id)
            Log.d("GLOBAL_CHECK", "")
            Log.d("GLOBAL_CHECK", "Проверка: id=$id, type=$type")
            val jsonTask = askJsonTask.getTask(id, type.name)
            val answers = jsonTask?.get("answers")

            Log.d(
                "GLOBAL_CHECK",
                "task=${task?.javaClass?.simpleName ?: "NULL"}, " +
                        "jsonTask=${jsonTask?.javaClass?.simpleName ?: "NULL"}, " +
                        "answers=${answers?.javaClass?.simpleName ?: "NULL"}"
            )

            if (task != null && answers != null) {
                TaskChecker.checkAnswer(type, task, answers)
                    .log("GLOBAL_CHECK")
            }

        }
        uncompletedTasks.forEach { (id, type) ->
            fillMissingTasks(id,
                type,
                dataLC.currentSession.value?.classID.toString(),
                dataLC.currentSession.value?.language!!,
                dataLC.currentSession.value?.topic!!
            )
        }
        val _tmp_p=calculateTotalScore()
        val _tmp_g=GradeScale.getGrade(_tmp_p)
        dataLC.currentClient.value?.grade=_tmp_g

        Log.d("DEBUG","TotalScore=${_tmp_p}")

    }

    private fun getTaskFromJSON(){
        val askJsonTask=JSONHandler.loadFromJson()
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
    }

    private fun fillMissingTasks(
                                id: String,
                                taskType: TaskType,
                                difficulty: String,
                                language: String,
                                topic: String){
        Log.e("DEBUG","fillMissingTasks - ${taskType.toString()}")
        val _tmp=BaseTask.getEmptyTask(id,taskType,difficulty,language,topic)
        val askJsonTask=JSONHandler.loadFromJson()
        val tc=JsonUtils.countAnswers(askJsonTask.getTask(id,taskType.toString())?.get("answers"))
        ClientTaskSession.updateTaskResult(id, AnswerCheckResult(0,tc,.0))

        ClientTaskSession.addTask(_tmp)
        completedTasks.add(id to taskType)
    }

    private fun calculateTotalScore(): Float {
        if (completedTasks.isEmpty()) return 0f


        var totalPercentage: Double=.0

        completedTasks.forEach { (id, type) ->
            val percentage: Double = ClientTaskSession.getResult(id)?.percentage ?: .0
            totalPercentage += percentage
        }

        val totalTasks = completedTasks.size
        return (totalPercentage / totalTasks).toFloat()
    }

    companion object {
        @JvmStatic
        fun newInstance() = OutFragment()
    }
}
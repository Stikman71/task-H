package com.helper.DataManager

import android.util.Log
import com.helper.Logic.AnswerCheckResult
import com.helper.Logic.BaseTask
import com.helper.Logic.TaskType

data class Client (
    var name:String="",
    var sName:String="",
    var activity:String="",
    var grade:Int=2
)

object ClientTaskSession{
    val tasks: MutableList<BaseTask> = mutableListOf()
    val results: MutableMap<String, AnswerCheckResult> = mutableMapOf()
    var currentIndex: Int = 0
    fun addTask(t: BaseTask){
        tasks.add(t)
    }
    fun getTaskCount(): Int {
        return tasks.size
    }
    fun removeTasksByType(taskType: TaskType) {
        tasks.removeAll { it.taskType == taskType }
    }
    fun getTasksByType(taskType: TaskType): List<BaseTask> {
        return tasks.filter { it.taskType == taskType }
    }
    fun findByBaseTask(bt: BaseTask): BaseTask? {
        return getTaskByTypeAndId(bt.taskType, bt.id)
    }
    fun getTaskByTypeAndId(taskType: TaskType, id: String): BaseTask? {
        val task = tasks.firstOrNull { it.taskType == taskType && it.id == id }
        if (task == null) {
            Log.d("ClientTaskSession", "Task NOT found: TaskType=$taskType, ID=$id")
        } else {
            Log.d("ClientTaskSession", "Task found: TaskType=$taskType, ID=$id")
        }
        return task
    }

    // Работа с результатами
    fun updateTaskResult(taskId: String, result: AnswerCheckResult) {
        results[taskId] = result
    }
    fun getResult(taskId: String): AnswerCheckResult? = results[taskId]
}


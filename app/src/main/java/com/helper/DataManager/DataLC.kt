package com.helper.DataManager

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


open class DataLC : ViewModel() {
    val changeID: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val currentSession: MutableLiveData<AppSession> by lazy {
        MutableLiveData(AppSession())
    }

    val currentClient: MutableLiveData<Client> by lazy{
        MutableLiveData(Client())
    }

    val completedTasks: MutableLiveData<MutableList<CheckTask_demo>> by lazy {
        MutableLiveData(mutableListOf())
    }
    fun markTaskCompleted(task: CheckTask_demo) {
        val currentList = completedTasks.value ?: mutableListOf()

        val exists = currentList.any {
            it.classID == task.classID &&
                    it.language == task.language &&
                    it.topic == task.topic &&
                    it.taskID_global == task.taskID_global
        }

        if (!exists) {
            currentList.add(task)
            completedTasks.value = currentList
        }
    }
    // Метод для проверки, выполнено ли задание
    fun isTaskCompleted(task: CheckTask_demo): Boolean {
        val currentList = completedTasks.value ?: return false
        return currentList.any {
            it.classID == task.classID &&
                    it.language == task.language &&
                    it.topic == task.topic &&
                    it.taskID_global == task.taskID_global
        }
    }
}


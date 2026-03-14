package com.helper.DataManager

import com.helper.Logic.TaskType

abstract  class CurrentTask(
    open val id: String,
    open val taskType: TaskType,
    open val difficulty: String,
    open val language: String,
    open val topic: String
) {
    abstract var userAnswers: List<String>
    abstract var isChecked: Boolean
}
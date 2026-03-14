package com.helper.DataManager

data class AppSession(
    val taskType:String?=null,
    val classID: Int? = null,
    val language: String? = null,
    val topic: String? = null,
    val taskID: Int? = null,
    val taskID_global:Int?=null
)

data class CheckTask_demo(
    val classID: Int? = null,
    val language: String? = null,
    val topic: String? = null,
    val taskID_global: Int? = null
)


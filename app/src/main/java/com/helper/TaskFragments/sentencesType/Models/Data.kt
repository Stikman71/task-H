package com.helper.TaskFragments.sentencesType.Models

import kotlinx.serialization.Serializable

@Serializable
data class TaskWrapper(
    val SENTENCE: Sentence? = null,
    val FILLTHEBLANK: FillTheBlank? = null
)
@Serializable
data class Sentence(
    val template: String,
    val blanks: List<String>,
    val answers: List<String>,
    val options: List<String>
)
@Serializable
data class FillTheBlank(
    val template: String,
    val blanks: List<String>,
    val answers: Map<String, List<String>>,
    val options: List<String>
)

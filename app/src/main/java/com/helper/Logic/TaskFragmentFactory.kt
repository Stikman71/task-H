package com.helper.Logic

import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.helper.OutFragment
import com.helper.TaskFragments.InsertALetter.FillInTheBlankFragment
import com.helper.TaskFragments.ReadingFragment
import com.helper.TaskFragments.SelectWord.WordSelectionFragment
import com.helper.TaskFragments.TaskButtonsFragment
import com.helper.TaskFragments.TaskFragmentBase
import com.helper.TaskFragments.TaskSliderFragment
import com.helper.TaskFragments.VideoFragment
import com.helper.TaskFragments.sentencesType.SentenceFragment

enum class TaskType {
    HOME,
    READ,
    SLIDER,
    SENTENCE,
    FILLTHEBLANK,
    SELECTWORD,
    BUTTONS,
}

object TaskFragmentFactory {

    fun create(type: TaskType): TaskFragmentBase<out ViewBinding> {
        return when (type) {
            TaskType.SLIDER -> TaskSliderFragment()
            TaskType.FILLTHEBLANK -> FillInTheBlankFragment()
            TaskType.SELECTWORD -> WordSelectionFragment()
//            TaskType.BUTTONS -> TaskButtonsFragment()
//            TaskType.READ-> ReadingFragment()
//            TaskType.HOME -> Fragment()
            TaskType.SENTENCE-> SentenceFragment()
            else -> throw IllegalArgumentException("TaskType $type is not supported")
        }
    }
}
package com.helper.TaskFragments.sentencesType.layout

import com.helper.TaskFragments.sentencesType.Models.DragTextView

interface ISmartInsert {
    fun tryInsert(insertable: DragTextView): Boolean
    fun checkContainer(): Int
}
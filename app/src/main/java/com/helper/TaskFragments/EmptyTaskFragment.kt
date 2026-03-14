package com.helper.TaskFragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.helper.Logic.BaseTask

class EmptyTaskFragment : TaskFragmentBase<ViewBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): ViewBinding {
        throw NotImplementedError("Empty fragment")
    }

    override fun checkButtonIsPressed() {
        // ничего не делаем
    }

    override fun clearButtonIsPressed() {
        // ничего не делаем
    }

    override fun loadFragmentDataFromJSON() {
        TODO("Not yet implemented")
    }

    override fun loadFragmentFromSession(bt: BaseTask) {
    }

    override fun compareAnswers() {
        TODO("Not yet implemented")
    }

    override fun parsedAnswer() {
        TODO("Not yet implemented")
    }

    override fun parsedTemplate() {
        TODO("Not yet implemented")
    }

    override fun parsedBlank() {
        TODO("Not yet implemented")
    }

    override fun parsedOptions() {
        TODO("Not yet implemented")
    }

    override fun collectClientAnswer() {
        TODO("Not yet implemented")
    }

    override fun createBaseTask(): BaseTask {
        TODO("Not yet implemented")
    }

    override fun updateClientTaskSession(bt: BaseTask): BaseTask {
        TODO("Not yet implemented")
    }

}
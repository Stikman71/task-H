package com.helper.TaskFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewbinding.ViewBinding
import com.helper.Logic.BaseTask
import com.helper.R

class TaskSliderFragment : TaskFragmentBase<ViewBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): ViewBinding {
        return object : ViewBinding {
            override fun getRoot(): View {
                return FrameLayout(inflater.context)
            }
        }
    }
    override fun checkButtonIsPressed() {}
    override fun clearButtonIsPressed(){}
    override fun loadFragmentDataFromJSON() {
    }

    override fun loadFragmentFromSession(bt: BaseTask) {
    }

    override fun compareAnswers() {
        TODO("Not yet implemented")
    }

    override fun parsedAnswer() {
    }

    override fun parsedTemplate() {
    }

    override fun parsedBlank() {
    }

    override fun parsedOptions() {
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
    companion object {
        @JvmStatic
        fun newInstance() = TaskSliderFragment()
    }
}
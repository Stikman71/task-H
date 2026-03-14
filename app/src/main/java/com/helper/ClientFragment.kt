package com.helper

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import com.helper.DataManager.DataLC
import com.helper.databinding.FragmentClientBinding
import kotlin.getValue

class ClientFragment : Fragment() {

    private val dataLC: DataLC by activityViewModels()

    lateinit var binding:FragmentClientBinding
    private lateinit var name: EditText
    private lateinit var sName: EditText
    private lateinit var activity: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentClientBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        name = view.findViewById(R.id.etName)
        sName=view.findViewById(R.id.etSName)
        activity=view.findViewById(R.id.etActivity)

        binding.etName.setOnFocusChangeListener {_,hasFocus->
            if(!hasFocus){
                dataLC.currentClient.value=
                    dataLC.currentClient.value?.copy(name=name.text.toString())
            }
        }
        binding.etSName.setOnFocusChangeListener {_,hasFocus->
            if(!hasFocus){
                dataLC.currentClient.value=
                    dataLC.currentClient.value?.copy(sName=sName.text.toString())
            }
        }
        binding.etActivity.setOnFocusChangeListener {_,hasFocus->
            if(!hasFocus){
                dataLC.currentClient.value=
                    dataLC.currentClient.value?.copy(activity=activity.text.toString())
            }
        }
        binding.btnNext.setOnClickListener {
            val updatedClient = dataLC.currentClient.value?.copy(
                name = binding.etName.text.toString(),
                sName = binding.etSName.text.toString(),
                activity = binding.etActivity.text.toString()
            )
            dataLC.currentClient.value = updatedClient

            val task = arguments?.getString(ARG_TASK_TYPE)
            Log.d("Navigate", task.toString())
            if (task != null) {
                val fragment = TaskFragment.newInstance(task)

                (requireActivity() as MainActivity).OpenFragment(
                    fragment,
                    R.id.content_container,
                    clearUpToTag = "MAIN"
                )
            }
        }
    }

    companion object {
        private const val ARG_TASK_TYPE = "arg_task_type"
        fun newInstance(taskType: String?) =
            ClientFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TASK_TYPE, taskType)
                }
            }
    }
}
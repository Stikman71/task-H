package com.helper

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.helper.DataManager.CheckTask_demo
import com.helper.DataManager.DataLC
import com.helper.Logic.ButtonAdapter
import com.helper.Logic.SpinnerItem
import com.helper.Logic.TaskType
import com.helper.TaskFragments.ReadingFragment
import com.helper.TaskFragments.VideoFragment
import com.helper.databinding.FragmentMainBinding
import kotlin.getValue
//data class SpinnerItem(val idName: String, val name: String)

//class MainFragment : Fragment() {
//
//    private val dataLC: DataLC by activityViewModels()
//
//    private lateinit var binding: FragmentMainBinding
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var spinner: Spinner
//
//    private var spinnerValues: List<SpinnerItem> = emptyList()
//    private var spinnerReady: Boolean = false
//
//    private val TAG = "MainFragment"  // для Logcat
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = FragmentMainBinding.inflate(inflater, container, false)
//        Log.d(TAG, "onCreateView: binding inflated")
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        spinner = binding.spinner
//        recyclerView = binding.recyclerMatrix
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        Log.d(TAG, "onViewCreated: views initialized")
//
//        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                Log.d(TAG, "onItemSelected called: position=$position, spinnerReady=$spinnerReady")
//                if (!spinnerReady) return
//
//                val selectedItem = spinnerValues.getOrNull(position)
//                if (selectedItem == null) {
//                    Log.w(TAG, "selectedItem is null for position=$position")
//                    return
//                }
//
//
//                val selectedId:String = selectedItem.idName
//                val selectedName = selectedItem.name
//                Log.d(TAG, "Spinner selected: id=$selectedId, name=$selectedName")
//
//                val currentTopicID = dataLC.currentSession.value?.topic
//                if (currentTopicID != selectedId) {
//                    dataLC.currentSession.value =
//                        dataLC.currentSession.value?.copy(topic = selectedName)
//                    Log.d(TAG, "Updated currentSession.topicID to $selectedId")
//                }
//
//                try {
//                    getButtonsForOption(requireContext(), selectedId.toString())
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error in getButtonsForOption", e)
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                Log.d(TAG, "onNothingSelected called")
//            }
//        }
//
//        dataLC.currentSession.observe(viewLifecycleOwner) { session ->
//            val ctx = context ?: run {
//                Log.w(TAG, "context is null in currentSession observer")
//                return@observe
//            }
//
//            val newSpinnerValues = try {
//                getSpinnerValues(ctx, session?.classID)
//            } catch (e: Exception) {
//                Log.e(TAG, "Error getting spinner values", e)
//                emptyList()
//            }
//
//            if (spinnerValues != newSpinnerValues) {
//                spinnerValues = newSpinnerValues
//                updateSpinner(spinnerValues)
//                spinnerReady = true
//                Log.d(TAG, "Spinner updated with ${spinnerValues.size} items")
//            }
//        }
//    }
//
//    private fun getSpinnerValues(context: Context, classID: Int?): List<SpinnerItem> {
//        if (classID == null) {
//            Log.w(TAG, "getSpinnerValues: classID is null")
//            return emptyList()
//        }
//
//        val arrayName = "class_${classID}_topics"
//        val resID = context.resources.getIdentifier(arrayName, "array", context.packageName)
//        if (resID == 0) {
//            Log.w(TAG, "getSpinnerValues: array resource $arrayName not found")
//            return emptyList()
//        }
//
//        val rawArray = context.resources.getStringArray(resID)
//        Log.d(TAG, "getSpinnerValues: loaded ${rawArray.size} items from $arrayName")
//
//        return rawArray.mapNotNull { item ->
//            val parts = item.split(":", limit = 2)
//            if (parts.size != 2) {
//                Log.w(TAG, "Invalid spinner item format: $item")
//                null
//            } else {
//                val id = parts[0]
//                val name = parts[1]
//                if (id != null) SpinnerItem(id, name) else {
//                    Log.w(TAG, "Invalid id in spinner item: $item")
//                    null
//                }
//            }
//        }
//    }
//
//    private fun updateSpinner(values: List<SpinnerItem>) {
//        val adapter = ArrayAdapter(
//            requireContext(),
//            android.R.layout.simple_spinner_item,
//            values.map { it.name }
//        )
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinner.adapter = adapter
//        Log.d(TAG, "updateSpinner: adapter set with ${values.size} items")
//    }
//
//    private fun getButtonsForOption(context: Context, optionID: String): List<String> {
//        val arrayName = "topic_${optionID}_tasks"
//        val resId = context.resources.getIdentifier(arrayName, "array", context.packageName)
//
//        val buttonItems = if (resId != 0) context.resources.getStringArray(resId).toList()
//        else emptyList()
//        Log.d(TAG, "getButtonsForOption: $arrayName has ${buttonItems.size} items")
//
//        createButtons(buttonItems)
//        return buttonItems
//    }
//
//    private fun createButtons(buttonItems: List<String>) {
//        if (!this::recyclerView.isInitialized) {
//            Log.w(TAG, "createButtons: recyclerView not initialized")
//            return
//        }
//
//        recyclerView.adapter = ButtonAdapter(buttonItems) { id, type ->
//            if (type.isNullOrEmpty()) {
//                Log.w(TAG, "ButtonAdapter callback type is null/empty")
//                return@ButtonAdapter
//            }
//
//            val taskType = runCatching { TaskType.valueOf(type.uppercase()) }.getOrNull()
//            Log.d(TAG, "Button clicked: id=$id, type=$type, taskType=$taskType")
//            Log.d("Navigate","CHECK:${sanitizeTopicName(dataLC.currentSession.value?.topic.toString())}")
//
//
//            val fragment: Fragment = when {
//                type == "READ" -> ReadingFragment.newInstance()
//                taskType != null -> ClientFragment.newInstance(taskType)
//                else -> Fragment() // fallback
//            }
//
//            (activity as? MainActivity)?.OpenFragment(fragment, R.id.content_container)
//        }
//
//        Log.d(TAG, "createButtons: adapter set with ${buttonItems.size} buttons")
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        spinnerValues = emptyList()
//        spinnerReady = false
//        Log.d(TAG, "onDestroyView: cleared spinnerValues and spinnerReady")
//    }
//
//    companion object {
//        @JvmStatic
//        fun newInstance() = MainFragment()
//    }
//}


class MainFragment : Fragment() {

    private val dataLC: DataLC by activityViewModels()

    private lateinit var binding: FragmentMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var spinner: Spinner

    private var spinnerValues: List<SpinnerItem> = emptyList() // var, чтобы можно было обновлять
    private var spinnerReady: Boolean = false

    private val TAG = "MainFragment"
    private var lastClickTime = 0L
    private var toast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        Log.d(TAG, "onCreateView: binding inflated")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        spinner = binding.spinner
        recyclerView = binding.recyclerMatrix
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        Log.d(TAG, "onViewCreated: views initialized")

        // Инициализация Spinner при старте
        spinnerValues = getSpinnerValues(requireContext(), dataLC.currentSession.value?.classID)
        updateSpinner(spinnerValues)
        spinnerReady = true

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (!spinnerReady) return

                val selectedItem = spinnerValues.getOrNull(position) ?: run {
                    Log.w(TAG, "selectedItem is null for position=$position")
                    return
                }

                val selectedBackend = selectedItem.backend
                val selectedDisplay = selectedItem.display
                val selectedId = selectedItem.id

                Log.d(TAG, "Spinner selected: id=$selectedId, backend=$selectedBackend, display=$selectedDisplay")

                // Обновляем session только backend
                val currentTopicBackend = dataLC.currentSession.value?.topic
                if (currentTopicBackend != selectedBackend) {
                    dataLC.currentSession.value =
                        dataLC.currentSession.value?.copy(topic = selectedBackend)
                    Log.d(TAG, "Updated currentSession.topicBackend to $selectedBackend")
                }

                try {
                    getButtonsForOption(requireContext(), selectedId.toString())
                } catch (e: Exception) {
                    Log.e(TAG, "Error in getButtonsForOption", e)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d(TAG, "onNothingSelected called")
            }
        }

        // Подписка на изменения currentSession
        dataLC.currentSession.observe(viewLifecycleOwner) { session ->
            val ctx = context ?: run {
                Log.w(TAG, "context is null in currentSession observer")
                return@observe
            }

            val newSpinnerValues = getSpinnerValues(ctx, session?.classID)

            if (spinnerValues != newSpinnerValues) {
                spinnerValues = newSpinnerValues
                updateSpinner(spinnerValues)
                spinnerReady = true
                Log.d(TAG, "Spinner updated with ${spinnerValues.size} items")
            }
        }
    }

    // ================== Spinner helpers ==================
    private fun getSpinnerValues(context: Context, classID: Int?): List<SpinnerItem> {
        if (classID == null) return emptyList()

        val arrayName = "class_${classID}_topics"
        val resID = context.resources.getIdentifier(arrayName, "array", context.packageName)
        if (resID == 0) {
            Log.w(TAG, "getSpinnerValues: array resource $arrayName not found")
            return emptyList()
        }

        return context.resources.getStringArray(resID).mapNotNull { item ->
            val parts = item.split(":", limit = 3)
            if (parts.size != 3) {
                Log.w(TAG, "Invalid spinner item format: $item. Expected id:backend:display")
                null
            } else {
                val id = parts[0].toIntOrNull()
                val backend = parts[1]
                val display = parts[2]
                if (id == null) {
                    Log.w(TAG, "Invalid id in spinner item: $item")
                    null
                } else SpinnerItem(id, backend, display)
            }
        }
    }

    private fun updateSpinner(values: List<SpinnerItem>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            values.map { it.display }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        Log.d(TAG, "updateSpinner: adapter set with ${values.size} items")
    }

    // ================== Button helpers ==================
    private fun getButtonsForOption(context: Context, optionID: String): List<String> {
        val arrayName = "topic_${optionID}_tasks"
        val resId = context.resources.getIdentifier(arrayName, "array", context.packageName)
        val buttonItems = if (resId != 0) context.resources.getStringArray(resId).toList() else emptyList()
        Log.d(TAG, "getButtonsForOption: $arrayName has ${buttonItems.size} items")
        createButtons(buttonItems)
        return buttonItems
    }

    private fun createButtons(buttonItems: List<String>) {
        if (!this::recyclerView.isInitialized) return

        recyclerView.adapter = ButtonAdapter(buttonItems) { id, type ->
            if (type.isNullOrEmpty()) return@ButtonAdapter

            // Ограничение частоты нажатий — 500 мс
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 500) return@ButtonAdapter
            lastClickTime = currentTime

            val taskType = runCatching { TaskType.valueOf(type.uppercase()) }.getOrNull()
            Log.d(TAG, "Button clicked: id=$id, type=$type, taskType=$taskType")

            dataLC.currentSession.value =
                dataLC.currentSession.value?.copy(taskID_global = id)

            val isOpen = dataLC.isTaskCompleted(
                CheckTask_demo(
                    dataLC.currentSession.value?.classID,
                    dataLC.currentSession.value?.language,
                    dataLC.currentSession.value?.topic,
                    id
                )
            )

            if (type.contains("CHECK TASK") && isOpen) {
                // Контрольное задание уже выполнено, показываем Toast безопасно
                toast?.cancel()  // отменяем предыдущий Toast, если есть
                toast = Toast.makeText(requireContext(), "Задание уже пройдено", Toast.LENGTH_SHORT)
                toast?.show()
            } else {
                // Обычное задание или контрольное, которое ещё не выполнено
                val fragment: Fragment = when {
                    type == "READ" -> ReadingFragment.newInstance()
                    type == "VIDEO" -> VideoFragment.newInstance()
                    type.contains("TASK") -> ClientFragment.newInstance(type)
                    else -> Fragment()
                }

                (requireActivity() as? MainActivity)?.OpenFragment(
                    fragment,
                    R.id.content_container,
                    )
            }
        }
        Log.d(TAG, "createButtons: adapter set with ${buttonItems.size} buttons")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        spinnerValues = emptyList()
        spinnerReady = false
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}
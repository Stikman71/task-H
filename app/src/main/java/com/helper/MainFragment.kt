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
import com.helper.DataManager.ClientTaskSession
import com.helper.DataManager.DataLC
import com.helper.Logic.ButtonAdapter
import com.helper.Logic.SpinnerItem
import com.helper.Logic.TaskType
import com.helper.TaskFragments.ReadingFragment
import com.helper.TaskFragments.VideoFragment
import com.helper.databinding.FragmentMainBinding
import kotlin.getValue


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

        ClientTaskSession.clear()
        Log.e("DEBUG","TEST len ${ClientTaskSession.tasks.size}")
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
        val adapter = ArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_item,           // выбранный элемент
            values.map { it.display }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // стандартный dropdown
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
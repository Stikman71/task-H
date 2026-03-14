package com.helper.TaskFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.helper.DataManager.DataLC
import com.helper.Logic.ButtonAdapter
import com.helper.Logic.JSON.JSONHandler
import com.helper.Logic.JSON.JsonUtils
import com.helper.Logic.JSON.PathBuilder
import com.helper.Logic.JSON.sanitizeTopicName
import com.helper.Logic.TaskType
import com.helper.MainActivity
import com.helper.MainFragment
import com.helper.R
import com.helper.TaskFragment
import com.helper.databinding.FragmentClientBinding
import com.helper.databinding.FragmentReadingBinding
import kotlin.getValue

class ReadingFragment : Fragment() {

    lateinit var binding: FragmentReadingBinding
    private val dataLC: DataLC by activityViewModels()
    var askJsonTask = JSONHandler()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentReadingBinding.inflate(inflater)
        return binding.root
    }

    private fun setupText(text: String) {
        val isReading = true

        binding.textViewTopic.text = text
        binding.textViewTopic.visibility = if (isReading) View.VISIBLE else View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pathParts = arrayOf(
            "Tasks",
            dataLC.currentSession.value?.language ?: "",
            "Class_${dataLC.currentSession.value?.classID ?: ""}",
            sanitizeTopicName(dataLC.currentSession.value?.topic ?: ""),
            "readme.json"
        )

        askJsonTask = try {
            val path = PathBuilder.buildPathForTask(pathParts)
            Log.d("DEBUG_JSON", "Загружаем JSON из: $path")

            val handler = JSONHandler.loadFromJson(requireContext(), path)
            Log.d("DEBUG_JSON", "JSON загружен. Всего задач: ${handler.getAllIdTypePairs().size}")

            handler
        } catch (e: Exception) {
            Log.e("DEBUG_JSON", "Ошибка при загрузке JSON: ${e.message}")
            e.printStackTrace()
            JSONHandler() // если ошибка — оставляем пустой handler
        }

        // Безопасно получаем текст
        val text = runCatching {
            askJsonTask.getTask(0.toString(), "READ")?.let { taskJson ->
                JsonUtils.getStringList(taskJson["text"])
            }?.getOrNull(0)
        }.getOrNull() ?: "Текст или файл не найдены"

        setupText(text)
    }

    companion object {
        fun newInstance() = ReadingFragment()
    }
}
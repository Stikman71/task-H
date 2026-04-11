package com.helper.TaskFragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
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
import java.nio.charset.Charset
import java.util.Locale
import kotlin.getValue

//class ReadingFragment : Fragment() {
//
//    lateinit var binding: FragmentReadingBinding
//    private val dataLC: DataLC by activityViewModels()
//    var askJsonTask = JSONHandler()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding=FragmentReadingBinding.inflate(inflater)
//        return binding.root
//    }
//
//    private fun setupText(text: String) {
//        val isReading = true
//
//        binding.textViewTopic.text = text
//        binding.textViewTopic.visibility = if (isReading) View.VISIBLE else View.GONE
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val pathParts = arrayOf(
//            "Tasks",
//            dataLC.currentSession.value?.language ?: "",
//            "Class_${dataLC.currentSession.value?.classID ?: ""}",
//            sanitizeTopicName(dataLC.currentSession.value?.topic ?: ""),
//            "readme.json"
//        )
//
//        askJsonTask = try {
//            val path = PathBuilder.buildPathForTask(pathParts)
//            Log.d("DEBUG_JSON", "Загружаем JSON из: $path")
//
//            val handler = JSONHandler.loadFromJson(requireContext(), path)
//            Log.d("DEBUG_JSON", "JSON загружен. Всего задач: ${handler.getAllIdTypePairs().size}")
//
//            handler
//        } catch (e: Exception) {
//            Log.e("DEBUG_JSON", "Ошибка при загрузке JSON: ${e.message}")
//            e.printStackTrace()
//            JSONHandler() // если ошибка — оставляем пустой handler
//        }
//
//        // Безопасно получаем текст
//        val text = runCatching {
//            askJsonTask.getTask(0.toString(), "READ")?.let { taskJson ->
//                JsonUtils.getStringList(taskJson["text"])
//            }?.getOrNull(0)
//        }.getOrNull() ?: "Текст или файл не найдены"
//
//        setupText(text)
//    }
//
//    companion object {
//        fun newInstance() = ReadingFragment()
//    }
//}


class ReadingFragment : Fragment() {

    private lateinit var binding: FragmentReadingBinding
    private val dataLC: DataLC by activityViewModels()
    private var askJsonTask = JSONHandler()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // WebView настройки для скролла
        binding.webViewTopic.isVerticalScrollBarEnabled = true
        binding.webViewTopic.scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY
        binding.webViewTopic.setOnTouchListener { v, _ ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        setAppLocale(requireContext(), dataLC.currentSession.value?.language ?: "ru")

        // Попытка загрузки HTML
        val htmlLoaded = loadWebViewDynamic()
        if (!htmlLoaded) {
            loadJsonText()
        }
    }

    private fun setAppLocale(context: Context, language: String) {
        val locale = Locale.Builder().setLanguage(language).build()
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    /** Загрузка текста из JSON */
    private fun loadJsonText() {
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
            JSONHandler.loadFromJson(requireContext(), path)
        } catch (e: Exception) {
            Log.e("DEBUG_JSON", "Ошибка загрузки JSON: ${e.message}")
            e.printStackTrace()
            JSONHandler()
        }

        val text = runCatching {
            askJsonTask.getTask(0.toString(), "READ")?.let { taskJson ->
                JsonUtils.getStringList(taskJson["text"])
            }?.getOrNull(0)
        }.getOrNull() ?: "Файл HTML или JSON не найден"

        // Показ TextView
        binding.scrollViewTopic.visibility = View.VISIBLE
        binding.textViewTopic.text = text
        binding.webViewTopic.visibility = View.GONE
    }

    /** Загрузка HTML */
    private fun loadWebViewDynamic(): Boolean {
        val language = dataLC.currentSession.value?.language ?: "ru"
        val classId = dataLC.currentSession.value?.classID ?: ""
        val topic = sanitizeTopicName(dataLC.currentSession.value?.topic ?: "")

        val folderPath = "Tasks/$language/Class_$classId/$topic"
        val htmlFile = "$folderPath/readme.htm"

        // Проверка существования файла
        val htmlExists = try {
            requireContext().assets.open(htmlFile).close()
            true
        } catch (e: Exception) {
            Log.w("ReadingFragment", "HTML файл не найден: $htmlFile")
            false
        }

        if (!htmlExists) return false

        val htmlData = requireContext().assets.open(htmlFile)
            .bufferedReader(Charset.forName("Windows-1251"))
            .use { it.readText() }

        val baseUrl = "file:///android_asset/$folderPath/"

        binding.webViewTopic.settings.javaScriptEnabled = true
        binding.webViewTopic.settings.loadWithOverviewMode = true
        binding.webViewTopic.settings.useWideViewPort = true

        binding.webViewTopic.loadDataWithBaseURL(
            baseUrl,
            htmlData,
            "text/html",
            "charset=windows-1251",
            null
        )

        // Показ WebView
        binding.webViewTopic.visibility = View.VISIBLE
        binding.scrollViewTopic.visibility = View.GONE
        return true
    }

    private fun sanitizeTopicName(topic: String): String {
        return topic.replace("[^a-zA-Z0-9_]".toRegex(), "_")
    }

    companion object {
        fun newInstance() = ReadingFragment()
    }
}
package com.helper

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.helper.DataManager.DataLC
import com.helper.Logic.ButtonAdapter
import com.helper.databinding.FragmentHelloBinding
import java.util.Locale

class HelloFragment : Fragment() {
    private val dataLC: DataLC by activityViewModels()
    lateinit var binding:FragmentHelloBinding
    private lateinit var recyclerView:RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentHelloBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.list_classes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Устанавливаем язык по умолчанию (только если ещё не выбран)
        val defaultLang = dataLC.currentSession.value?.language ?: "ru"
        dataLC.currentSession.value =
            dataLC.currentSession.value?.copy(language = defaultLang)

        // Меняем локаль приложения
        setAppLocale(requireContext(), defaultLang)

        // Создаём RecyclerView с актуальными ресурсами
        updateUI()

        // Кнопка выбора языка
        binding.buttonLanguage.setOnClickListener {
            val languages = arrayOf("English", "Русский")
            val codes = arrayOf("en", "ru")

            AlertDialog.Builder(requireContext())
                .setTitle("Select language")
                .setItems(languages) { _, which ->
                    val selectedLang = codes[which]

                    // Сохраняем выбор
                    dataLC.currentSession.value =
                        dataLC.currentSession.value?.copy(language = selectedLang)

                    // Меняем локаль приложения
                    setAppLocale(requireContext(), selectedLang)

                    // Обновляем RecyclerView с новым языком
                    updateUI()
                }
                .show()
        }
    }

//    fun executeIndexFromButtonID(button: Button):Int{
//        val rName=resources.getResourceEntryName(button.id)
//        val idStr=rName.substringAfter("button_id_")
//        return idStr.toInt()
//    }

    companion object{
        @JvmStatic
        fun newInstance()= HelloFragment()
    }


    fun setAppLocale(context: Context, language: String) {
        val locale = Locale.Builder().setLanguage(language).build()
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    fun updateUI() {
        // Берём массив строк из ресурсов для текущей локали
        val buttonItems = resources.getStringArray(R.array.option_main).toList()
        println("buttonItems: $buttonItems")

        // Если адаптер уже установлен, обновляем данные
        val adapter = recyclerView.adapter
        if (adapter is ButtonAdapter) {
            adapter.updateData(buttonItems)
        } else {
            // Если адаптера нет — создаём новый
            recyclerView.adapter = ButtonAdapter(buttonItems) { id, type ->
                dataLC.currentSession.value =
                    dataLC.currentSession.value?.copy(classID = id)
                (activity as? MainActivity)?.OpenFragment(
                    MainFragment.newInstance(),
                    R.id.content_container,
                    backStackTag = "MAIN"
                )
            }
        }
    }
}
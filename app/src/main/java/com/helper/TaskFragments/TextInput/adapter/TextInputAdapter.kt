package com.helper.TaskFragments.TextInput.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.helper.databinding.ItemTextInputBinding

class TextInputAdapter(
    private val questions: List<String>,
    private val currentAnswers: MutableList<String>,
    private val onTextChanged: (Int, String) -> Unit
) : RecyclerView.Adapter<TextInputAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTextInputBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount() = questions.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTextInputBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.questionText.text = questions[position]

        val editText = holder.binding.answerInput

        // Убираем старый слушатель, если был
        (editText.tag as? TextWatcher)?.let { editText.removeTextChangedListener(it) }

        // Устанавливаем текст из currentAnswers
        editText.setText(currentAnswers.getOrNull(position) ?: "")

        // Добавляем новый слушатель
        val watcher = editText.doAfterTextChanged { text ->
            currentAnswers[position] = text.toString()
            onTextChanged(position, text.toString())
        }

        // Сохраняем watcher в tag для последующего удаления
        editText.tag = watcher
    }
}
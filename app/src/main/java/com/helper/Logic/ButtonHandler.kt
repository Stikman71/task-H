package com.helper.Logic

import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.helper.R

class ButtonAdapter(
    private var items: List<String>, // строки вида "0:home"
    private val onClick: (id: Int, type: String?) -> Unit
) : RecyclerView.Adapter<ButtonAdapter.ButtonViewHolder>() {

    class ButtonViewHolder(val button: Button) : RecyclerView.ViewHolder(button)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        // создаём MaterialButton с нашим стилем
        val button = MaterialButton(
            ContextThemeWrapper(parent.context, R.style.AppButton_Recycler)
        ).apply {
            // LayoutParams с margin
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also {
                it.setMargins(0, 8, 0, 8)
            }
        }

        // задаём layoutParams с margin
        val params = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 8, 0, 8)
        button.layoutParams = params

        return ButtonViewHolder(button)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        val item = items[position]
        val parts = item.split(":")
        val id = parts[0].toInt()
        val type: String?
        val label: String

        when (parts.size) {
            2 -> {
                // id:label
                type = null
                label = parts[1]
            }
            3 -> {
                // id:type:label
                type = parts[1]
                label = parts[2]
            }
            else -> {
                throw IllegalArgumentException("Неверный формат: $item")
            }
        }

        holder.button.text = label
        holder.button.id = View.generateViewId() // уникальный ID для View

        holder.button.setOnClickListener { onClick(id, type) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }
}

//class ButtonAdapter(
//    private val items: List<String>,   // текст кнопок
//    private val buttonIds: List<Int>,  // массив ID
//    private val onClick: (Button) -> Unit
//) : RecyclerView.Adapter<ButtonAdapter.ButtonViewHolder>() {
//
//    class ButtonViewHolder(val button: Button) : RecyclerView.ViewHolder(button)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
//        val button = Button(parent.context).apply {
//            layoutParams = RecyclerView.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//            ).apply {
//                (this as? ViewGroup.MarginLayoutParams)?.setMargins(0, 8, 0, 8)
//            }
//        }
//        return ButtonViewHolder(button)
//    }
//
//    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
//        holder.button.text = items[position]
//
//        // назначаем ID из массива
//        holder.button.id = buttonIds[position]
//
//        holder.button.setOnClickListener {
//            onClick(holder.button)
//        }
//    }
//
//    override fun getItemCount(): Int = items.size
//}
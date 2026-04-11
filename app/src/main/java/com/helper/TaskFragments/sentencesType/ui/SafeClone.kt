package com.helper.TaskFragments.sentencesType.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import com.helper.TaskFragments.sentencesType.Models.DragTextView
import com.helper.TaskFragments.sentencesType.layout.ISmartInsert

object SafeClone {

    /**
     * Безопасное клонирование DragTextView
     * @return новый объект или null, если что-то пошло не так
     */
    fun cloneDragTextViewSafe(original: DragTextView, context: Context): DragTextView? {
        return try {
            // Создаём новый объект DragTextView с тем же текстом
            val newDrag = DragTextView(context, original.text.toString())

            val origTv = original
            val newTv = newDrag

            // Копируем LayoutParams
            newTv.layoutParams = origTv.layoutParams?.let { lp ->
                when (lp) {
                    is ViewGroup.MarginLayoutParams -> ViewGroup.MarginLayoutParams(lp)
                    else -> ViewGroup.LayoutParams(lp.width, lp.height)
                }
            }

            // Копируем основные свойства
            newTv.visibility = origTv.visibility
            newTv.setPadding(origTv.paddingLeft, origTv.paddingTop, origTv.paddingRight, origTv.paddingBottom)
            newTv.textSize = origTv.textSize / context.resources.displayMetrics.scaledDensity
            newTv.setTextColor(origTv.currentTextColor)
            newTv.setBackgroundColor((origTv.background as? ColorDrawable)?.color ?: Color.TRANSPARENT)
            newTv.alpha = origTv.alpha
            newTv.translationX = 0f
            newTv.translationY = 0f

            // Возвращаем новый объект
            newDrag
        } catch (e: Exception) {
            Log.e("SafeClone", "Failed to clone DragTextView", e)
            null
        }
    }

    fun replaceDragTextViewSafe(first: TextView, second: TextView){
        val _tmp:String=first.text.toString()
        first.setText(second.text.toString())
        second.setText(_tmp)
    }

}
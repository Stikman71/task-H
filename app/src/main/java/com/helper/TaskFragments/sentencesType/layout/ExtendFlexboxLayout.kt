package com.helper.TaskFragments.sentencesType.layout

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.contains
import com.google.android.flexbox.FlexboxLayout
import com.helper.TaskFragments.sentencesType.Models.DragTextView
import com.helper.TaskFragments.sentencesType.ui.SafeClone
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty

class ExtendFlexboxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val name: String="",
    private var maxChild:Int=20,
    private var canBeEmpty: Boolean =false
) : FlexboxLayout(context, attrs, defStyleAttr), ISmartInsert{

    override fun tryInsert(insertable: DragTextView): Boolean {

        val cloned = SafeClone.cloneDragTextViewSafe(
            insertable,
            insertable.context
        ) ?: return false
        val newView = cloned

        newView.applySafeLayoutParams(24,0,24,0)

        if (maxChild == 1 && isNotEmpty()) {

            val existing = getChildAt(0) as? DragTextView ?: return false

            return if (existing.text.contains("_")) {
                removeViewAt(0)
                addView(newView)
                true
            } else {
                existing.defaultStatus()
                SafeClone.replaceDragTextViewSafe(existing, insertable)
                existing.applySafeLayoutParams(24,0,24,0)

                false
            }
        }

        addView(newView)
        return true
    }

    fun setMaxChild(c: Int){
        maxChild=c
    }

    fun canBeEmpty(f: Boolean){
        canBeEmpty=f
    }

    override fun checkContainer(): Int {
        if(isEmpty() && !canBeEmpty){
            this.isClickable = false
            addView(DragTextView(context,name))
        }
        return childCount
    }

    fun getTextValues(): List<DragTextView> {
        val result = mutableListOf<DragTextView>()

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is DragTextView) {
                val text = child.text.toString()
                if ('_' !in text) {  // <- пропускаем, если текст содержит '_'
                    result.add(child)
                }
            }
        }

        return result
    }

}
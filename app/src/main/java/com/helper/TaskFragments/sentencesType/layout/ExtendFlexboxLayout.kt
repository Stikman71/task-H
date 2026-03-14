package com.helper.TaskFragments.sentencesType.layout

import android.content.Context
import android.util.AttributeSet
import android.util.Log
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
    private val name: String="DN",
    private var maxChild:Int=20
) : FlexboxLayout(context, attrs, defStyleAttr), ISmartInsert{

    override fun tryInsert(insertable: DragTextView): Boolean {

        val cloned = SafeClone.cloneDragTextViewSafe(
            insertable,
            insertable.context
        ) ?: return false

        val newView = cloned

        if (maxChild == 1 && isNotEmpty()) {

            val existing = getChildAt(0) as? TextView ?: return false

            return if (existing.text.contains("_")) {
                removeViewAt(0)
                addView(newView)
                true
            } else {
                SafeClone.replaceDragTextViewSafe(existing, insertable)
                false
            }
        }

        addView(newView)
        return true
    }

    fun setMaxChild(c: Int){
        maxChild=c
    }

    override fun checkContainer(): Int {
        if(isEmpty()){
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
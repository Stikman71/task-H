package com.helper.TaskFragments.sentencesType.Models

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import com.helper.R
import com.helper.TaskFragments.sentencesType.layout.ExtendFlexboxLayout
import com.helper.TaskFragments.sentencesType.layout.ISmartInsert

@SuppressLint("ClickableViewAccessibility")
class DragTextView @JvmOverloads constructor(
    context: Context,
    private val name: String,
    attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, R.attr.dragTextViewStyle) {

    private lateinit var oldParent: ExtendFlexboxLayout
    private var fragmentRoot: ViewGroup? = null
    private val parentLocation = IntArray(2)

    private var touchOffsetX = 0f
    private var touchOffsetY = 0f

    private var currentStatus=true;

    init {
        id = generateViewId()
        text = name

        if ('_' !in name) {
            setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> onDown(event)
                    MotionEvent.ACTION_MOVE -> onMove(event)
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> onUp()
                }
                true
            }
        }
    }

    // ---------- DOWN ----------

    private fun onDown(event: MotionEvent) {

        val activity = context as? Activity ?: return
        fragmentRoot = activity.findViewById(R.id.fragment_root) ?: return

        oldParent = parent as? ExtendFlexboxLayout ?: return

        val rootLocation = IntArray(2)
        fragmentRoot!!.getLocationOnScreen(rootLocation)

        // Позиция View внутри root ДО удаления
        val viewLocation = IntArray(2)
        getLocationOnScreen(viewLocation)

        val initialX = viewLocation[0] - rootLocation[0]
        val initialY = viewLocation[1] - rootLocation[1]

        removeFromPlaceHolder()
        fragmentRoot!!.overlay.add(this)

        x = initialX.toFloat()
        y = initialY.toFloat()

        // сохраняем offset пальца внутри view
        touchOffsetX = event.rawX - viewLocation[0]
        touchOffsetY = event.rawY - viewLocation[1]
    }

    fun removeFromPlaceHolder():DragTextView?{
        oldParent = parent as? ExtendFlexboxLayout ?: return null
        (parent as? ViewGroup)?.removeView(this)
        oldParent.checkContainer()
        return this
    }

    // ---------- MOVE ----------

    private fun onMove(event: MotionEvent) {
        if(!currentStatus) {
            correctStatus()
        }

        val rootLocation = IntArray(2)
        fragmentRoot?.getLocationOnScreen(rootLocation) ?: return

        val newX = event.rawX - rootLocation[0] - touchOffsetX
        val newY = event.rawY - rootLocation[1] - touchOffsetY

        x = newX
        y = newY
    }

    // ---------- UP ----------

    private fun onUp() {

        val root = fragmentRoot ?: return

        root.overlay.remove(this)

        val target = findSmartInsertUnder(root)

        val inserted = (target as? ISmartInsert)
            ?.runCatching { tryInsert(this@DragTextView) }
            ?.getOrDefault(false)
            ?: false

        if (!inserted) {
            insertIntoNewPlace(oldParent)
            //(oldParent as? ISmartInsert)?.tryInsert(this)
        }

        destroy()
    }

    fun insertIntoNewPlace(placeHolder: ISmartInsert){
        placeHolder.tryInsert(this)
    }

    // ---------- Поиск ISmartInsert ----------

    private fun findSmartInsertUnder(root: ViewGroup): ISmartInsert? {

        val centerX = x + width / 2f
        val centerY = y + height / 2f
        var found: ISmartInsert? = null

        // Получаем координаты root один раз
        val rootLoc = IntArray(2)
        root.getLocationOnScreen(rootLoc)
        val rootLeft = rootLoc[0]
        val rootTop = rootLoc[1]

        fun traverse(view: View) {
            if (found != null) return
            if (view == this@DragTextView || view.visibility != View.VISIBLE) return

            val loc = IntArray(2)
            view.getLocationOnScreen(loc)
            val left = loc[0]
            val top = loc[1]
            val right = left + view.width
            val bottom = top + view.height

            // Центр dragged view находится внутри view?
            if (centerX + rootLeft >= left && centerX + rootLeft <= right &&
                centerY + rootTop >= top && centerY + rootTop <= bottom
            ) {
                if (view is ISmartInsert) {
                    found = view
                    return
                }
            }

            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    traverse(view.getChildAt(i))
                }
            }
        }

        traverse(root)
        return found
    }

    // ---------- Проверка ----------

    fun correctStatus() {
        currentStatus=true
        setTextColor(ContextCompat.getColor(context, R.color.colorButtonBackground))
        paintFlags = paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        background = ContextCompat.getDrawable(context, android.R.color.transparent)
    }

    fun incorrectStatus() {
        currentStatus=false
        setTextColor(Color.RED)
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    // ---------- Очистка ----------

    fun destroy() {

        (parent as? ViewGroup)?.removeView(this)
        setOnTouchListener(null)
    }
}
package com.helper.TaskFragments.sentencesType.Models

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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

    private var touchOffsetX = 0f
    private var touchOffsetY = 0f

    private var currentStatus = true

    private var isDragEnabled = true // <- флаг для разблокировки

    fun applySafeLayoutParams(left: Int, top: Int, right: Int, bottom: Int) {
        val params = FlexboxLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(left, top, right, bottom)
        this.layoutParams = params
    }

    init {
        id = generateViewId()
        text = name

        if ('_' !in name) {
            setOnTouchListener { v, event ->

                Log.d("TOUCH_DEBUG", "DragTextView $name received event: ${event.actionMasked}, isDragEnabled=$isDragEnabled")

                if (!isDragEnabled) {
                    Log.d("TOUCH_DEBUG", "DragTextView $name is locked — letting parent handle")
                    // если блокирован — позволяем ScrollView обрабатывать
                    return@setOnTouchListener false
                }

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        Log.d("TOUCH_DEBUG", "DragTextView $name ACTION_DOWN at x=${event.x} y=${event.y}")
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                        onDown(event)
                    }

                    MotionEvent.ACTION_MOVE -> {
                        Log.d("TOUCH_DEBUG", "DragTextView $name ACTION_MOVE at x=${event.x} y=${event.y}")
                        onMove(event)
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        Log.d("TOUCH_DEBUG", "DragTextView $name ACTION_UP/CANCEL")
                        v.parent?.requestDisallowInterceptTouchEvent(false)
                        onUp()
                    }
                }
                true
            }
        }
    }

    // ---------- DOWN ----------
    private fun onDown(event: MotionEvent) {

        val activity = context as? Activity ?: return
        fragmentRoot = activity.findViewById(R.id.fragment_root) ?: return

        fragmentRoot?.requestDisallowInterceptTouchEvent(true)

        oldParent = parent as? ExtendFlexboxLayout ?: return

        val rootLocation = IntArray(2)
        fragmentRoot!!.getLocationOnScreen(rootLocation)

        val viewLocation = IntArray(2)
        getLocationOnScreen(viewLocation)

        val initialX = viewLocation[0] - rootLocation[0]
        val initialY = viewLocation[1] - rootLocation[1]

        removeFromPlaceHolder()

        fragmentRoot!!.overlay.add(this)

        x = initialX.toFloat()
        y = initialY.toFloat()

        touchOffsetX = event.rawX - viewLocation[0]
        touchOffsetY = event.rawY - viewLocation[1]
    }

    fun removeFromPlaceHolder(): DragTextView? {
        val parentLayout = parent as? ExtendFlexboxLayout ?: return null
        oldParent = parentLayout

        (parent as? ViewGroup)?.removeView(this)
        parentLayout.checkContainer()

        return this
    }

    // ---------- MOVE ----------
    private fun onMove(event: MotionEvent) {
        if (!currentStatus) {
            defaultStatus()
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
        }

        destroy()

        fragmentRoot?.requestDisallowInterceptTouchEvent(false)
    }

    fun insertIntoNewPlace(placeHolder: ISmartInsert) {
        placeHolder.tryInsert(this)
    }

    // ---------- Поиск ISmartInsert ----------
    private fun findSmartInsertUnder(root: ViewGroup): ISmartInsert? {

        val centerX = x + width / 2f
        val centerY = y + height / 2f
        var found: ISmartInsert? = null

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

    // ---------- Статусы ----------
    fun defaultStatus() {
        currentStatus = true
        setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary))
        paintFlags = paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        background = ContextCompat.getDrawable(context, android.R.color.transparent)
    }

    fun correctStatus() {
        currentStatus = false
        setTextColor(ContextCompat.getColor(context, R.color.colorCorrect))
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        background = ContextCompat.getDrawable(context, android.R.color.transparent)
    }

    fun incorrectStatus() {
        currentStatus = false
        setTextColor(ContextCompat.getColor(context, R.color.colorDanger))
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        background = ContextCompat.getDrawable(context, android.R.color.transparent)
    }

    // ---------- Очистка ----------
    fun destroy() {
        (parent as? ViewGroup)?.removeView(this)
        setOnTouchListener(null)
    }
}
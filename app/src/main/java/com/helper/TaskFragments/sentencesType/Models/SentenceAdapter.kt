package com.helper.TaskFragments.sentencesType.Models

import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import com.helper.R
import com.helper.TaskFragments.sentencesType.SentenceItem
import com.helper.TaskFragments.sentencesType.layout.ExtendFlexboxLayout

class SentenceAdapter(
    private val _items: List<SentenceItem>
) : RecyclerView.Adapter<SentenceAdapter.SentenceViewHolder>() {

    val items: List<SentenceItem> get() = _items

    inner class SentenceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sentenceFlexbox: FlexboxLayout = view.findViewById(R.id.sentenceFlexbox)
        val answersContainer: ExtendFlexboxLayout = view.findViewById(R.id.answersContainer)
        val blanksList: MutableList<ExtendFlexboxLayout> = mutableListOf()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SentenceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sentence, parent, false)
        return SentenceViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: SentenceViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.sentenceFlexbox.removeAllViews()
        holder.blanksList.clear() // чистим предыдущие бланки

        var blankIndex = 0
        val words = item.template.split(" ").toMutableList()
        words.forEach { word ->
            if (word.contains("_") && blankIndex < item.blanks.size) {
                val flexbox = ExtendFlexboxLayout(context, name = item.blanks[blankIndex]).apply {
                    setMaxChild(1)
                    val drag = DragTextView(context, item.blanks[blankIndex])
                    drag.applySafeLayoutParams(24,0,24,0)
                    addView(drag)
                }

                holder.sentenceFlexbox.addView(flexbox)
                holder.blanksList.add(flexbox)
                blankIndex++
            } else {
                val textView = TextView(ContextThemeWrapper(context, R.style.TextViewSentence)).apply {
                    text = word
                    val layoutParams = FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT

                    )
                    layoutParams.setMargins(12, 0, 12, 0)
                    this.layoutParams = layoutParams
                    gravity = Gravity.CENTER_VERTICAL
                }
                holder.sentenceFlexbox.addView(textView)
            }
        }

        // --- Заполняем ответы ---
        holder.answersContainer.removeAllViews()
        if (item.options.isNullOrEmpty()) {
            holder.answersContainer.visibility = View.GONE
        } else {
            holder.answersContainer.visibility = View.VISIBLE

            item.options.distinct().forEach { name ->
                val drag = DragTextView(context, name)
                drag.applySafeLayoutParams(24, 0, 24, 0)
                holder.answersContainer.addView(drag)
            }
        }
    }
}
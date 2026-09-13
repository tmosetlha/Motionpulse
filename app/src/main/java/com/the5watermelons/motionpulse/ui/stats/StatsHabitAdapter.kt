package com.the5watermelons.motionpulse.ui.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.the5watermelons.motionpulse.R
import com.the5watermelons.motionpulse.databinding.ItemStatHabitRowBinding

class StatsHabitAdapter : RecyclerView.Adapter<StatsHabitAdapter.ViewHolder>() {

    private val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    private var items: List<StatsViewModel.HabitWeekly> = emptyList()

    fun submitList(newItems: List<StatsViewModel.HabitWeekly>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStatHabitRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], dayLabels)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemStatHabitRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StatsViewModel.HabitWeekly, dayLabels: List<String>) {
            binding.tvHabitName.text = item.habit.name
            binding.dotsContainer.removeAllViews()

            val context = binding.root.context
            val dotSize = (18 * context.resources.displayMetrics.density).toInt()
            val margin = (6 * context.resources.displayMetrics.density).toInt()

            item.weekDots.forEachIndexed { index, done ->
                val column = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = android.view.Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                    )
                }

                val dot = android.view.View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                        bottomMargin = margin
                    }
                    setBackgroundResource(
                        if (done) R.drawable.dot_filled else R.drawable.dot_empty
                    )
                }

                val label = TextView(context).apply {
                    text = dayLabels.getOrElse(index) { "" }
                    setTextColor(context.getColor(R.color.mp_text_secondary))
                    textSize = 10f
                    gravity = android.view.Gravity.CENTER
                }

                column.addView(dot)
                column.addView(label)
                binding.dotsContainer.addView(column)
            }
        }
    }
}
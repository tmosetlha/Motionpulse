package com.the5watermelons.motionpulse.ui.habits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.the5watermelons.motionpulse.data.local.HabitEntity
import com.the5watermelons.motionpulse.databinding.ItemHabitRowCheckableBinding

class HabitAdapter(
    private val onToggle: (HabitEntity) -> Unit,
    private val onLongPressDelete: (HabitEntity) -> Unit
) : ListAdapter<HabitEntity, HabitAdapter.HabitViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitRowCheckableBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(
        private val binding: ItemHabitRowCheckableBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: HabitEntity) {
            binding.tvHabitName.text = habit.name
            binding.tvStreak.text = if (habit.streakCount > 0) "${habit.streakCount}\uD83D\uDD25" else ""
            binding.vDot.alpha = if (habit.isDoneToday) 1f else 0.35f

            binding.root.setOnClickListener { onToggle(habit) }
            binding.root.setOnLongClickListener {
                onLongPressDelete(habit)
                true
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HabitEntity>() {
            override fun areItemsTheSame(oldItem: HabitEntity, newItem: HabitEntity) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: HabitEntity, newItem: HabitEntity) =
                oldItem == newItem
        }
    }
}
package ru.university.mytaskcalendar.UI

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.university.mytaskcalendar.R

/**
 * Элемент календаря: либо день (dayNumber != null), либо пустая ячейка (dayNumber == null).
 */
data class CalendarDay(
    val dayNumber: Int?,          // null для пустых ячеек в начале/конце месяца
    val dateString: String?,      // "yyyy-MM-dd", null для пустых
    val hasTasks: Boolean = false,
    val isSelected: Boolean = false,
    val isToday: Boolean = false
)

class CalendarAdapter(
    private val onDayClick: (CalendarDay) -> Unit
) : ListAdapter<CalendarDay, CalendarAdapter.DayViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CalendarDay>() {
            override fun areItemsTheSame(old: CalendarDay, new: CalendarDay) =
                old.dateString == new.dateString

            override fun areContentsTheSame(old: CalendarDay, new: CalendarDay) =
                old == new
        }
    }

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tvDay)
        val tvDot: View = view.findViewById(R.id.tvDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = getItem(position)

        if (day.dayNumber == null) {
            // Пустая ячейка
            holder.tvDay.text = ""
            holder.tvDot.visibility = View.INVISIBLE
            holder.itemView.setOnClickListener(null)
            return
        }

        holder.tvDay.text = day.dayNumber.toString()
        holder.tvDot.visibility = if (day.hasTasks) View.VISIBLE else View.INVISIBLE

        // Выделяем выбранный день
        if (day.isSelected) {
            holder.tvDay.setBackgroundColor(Color.parseColor("#6200EE"))
            holder.tvDay.setTextColor(Color.WHITE)
        } else {
            holder.tvDay.setBackgroundColor(Color.TRANSPARENT)
            holder.tvDay.setTextColor(Color.BLACK)
        }

        // Сегодняшний день делаем жирным
        holder.tvDay.setTypeface(
            null,
            if (day.isToday) Typeface.BOLD else Typeface.NORMAL
        )

        holder.itemView.setOnClickListener { onDayClick(day) }
    }
}
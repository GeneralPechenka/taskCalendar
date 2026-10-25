package ru.university.mytaskcalendar.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.university.mytaskcalendar.Repository.TaskRepository
import ru.university.mytaskcalendar.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    // Все задачи — для главного экрана (можно оставить)
    val allTasks: StateFlow<List<Task>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Выбранная дата (по умолчанию — сегодня)
    private val _selectedDate = MutableStateFlow(getTodayDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Задачи на выбранную дату — автоматически обновляются при смене дня
    val tasksForSelectedDate: StateFlow<List<Task>> = _selectedDate
        .flatMapLatest { date -> repository.getTasksByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Список дат месяца, в которых есть задачи (для точек)
    private val _datesWithTasks = MutableStateFlow<Set<String>>(emptySet())
    val datesWithTasks: StateFlow<Set<String>> = _datesWithTasks.asStateFlow()

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun loadDatesForMonth(year: Int, month: Int) {
        val prefix = String.format(Locale.getDefault(), "%04d-%02d", year, month + 1)
        viewModelScope.launch {
            val dates = repository.getDatesWithTasksForMonth(prefix)
            _datesWithTasks.value = dates.toSet()
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch { repository.insert(task) }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { repository.update(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.delete(task) }
    }

    fun loadTask(taskId: Long, onLoaded: (Task?) -> Unit) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            onLoaded(task)
        }
    }

    companion object {
        fun getTodayDate(): String =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
    }
}
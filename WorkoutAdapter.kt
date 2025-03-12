package com.example.myapplication

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.setBackground
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker
import java.time.format.DateTimeFormatter

// Адаптер для RecyclerView, который отображает список тренировок (WorkoutEntity)
class WorkoutAdapter(
    private val workouts: MutableList<WorkoutEntity>, //ДОЛЖНО БІТЬ List а не MutableList
    private val onWorkoutClick: (WorkoutEntity) -> Unit // Обработчик клика

) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {


    private val selectedWorkouts = mutableSetOf<WorkoutEntity>() // Множество выбранных тренировок
    private var selectionMode = false // Флаг режима выбора
    var onSelectionModeChanged: ((Boolean) -> Unit)? =
        null // Лямбда для уведомления об изменении режима выбора



    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutName: TextView = itemView.findViewById(R.id.tv_workout_name)
        val workoutType: TextView = itemView.findViewById(R.id.tv_workout_type)
        val workoutDate: TextView = itemView.findViewById(R.id.tv_workout_date) // Добавляем поле для даты
        val itemContainer: View = itemView.findViewById(R.id.workoutName)   //R.id.item_container
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)

    }

   // override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
       // val workout = workouts[position]
   override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
       val workout = workouts[position]
       val normalizedType = workout.type.trim().lowercase()

       holder.workoutName.text = workout.name
       holder.workoutType.text = workout.type
       holder.workoutDate.text = workout.date.toString()
       // Форматируем дату в нужный формат
       val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
       holder.workoutDate.text = "Дата: ${workout.date.format(dateFormatter)}"

       // Устанавливаем фон в зависимости от выбора и типа тренировки
       setBackground(holder, normalizedType)

       // Обработчик долгого нажатия для входа в режим выбора
       holder.itemView.setOnLongClickListener {
           enterSelectionMode()
           true
       }

       // Обработчик обычного нажатия
       holder.itemView.setOnClickListener {
           if (selectionMode) {
               // Если в режиме выбора, переключаем выбор
               toggleSelection(workout, !selectedWorkouts.contains(workout))
               setBackground(holder, normalizedType) // Обновляем фон
           } else {
               // Если не в режиме выбора, обрабатываем обычный клик
               onWorkoutClick(workout)
           }
       }
   }

    override fun getItemCount(): Int {
        return workouts.size
    }
    //override fun getItemCount() = workouts.size

    /**
     * Возвращает список выбранных тренировок.
     *
     * @return Список выбранных тренировок (List<WorkoutEntity>).
     */
    fun getSelectedWorkouts(): List<WorkoutEntity> {
        return selectedWorkouts.toList()
    }

    /**
     * Удаляет выбранные тренировки из списка.
     */
    fun deleteSelectedWorkouts() {
        workouts.removeAll(selectedWorkouts)
        selectedWorkouts.clear()
        selectionMode = false
        notifyDataSetChanged()
    }

    /**
     * Обновляет данные в адаптере.
     *
     * @param newWorkouts Новый список тренировок.
     */
    fun updateData(newWorkouts: List<WorkoutEntity>) {
        workouts.clear()
        workouts.addAll(newWorkouts)
        notifyDataSetChanged()
    }

    /**
     * Сбрасывает режим выбора: очищает выбранные элементы и уведомляет об изменении.
     */
    fun clearSelection() {
        selectionMode = false
        selectedWorkouts.clear()
        onSelectionModeChanged?.invoke(false)
        notifyDataSetChanged() // Уведомляем адаптер об изменении всех элементов
    }

    /**
     * Переключает выбор тренировки: добавляет или удаляет из выбранных.
     *
     * @param workout Тренировка для переключения.
     * @param isChecked Флаг, указывающий, выбрана ли тренировка.
     */
    private fun toggleSelection(workout: WorkoutEntity, isChecked: Boolean) {
        if (isChecked) {
            selectedWorkouts.add(workout)
        } else {
            selectedWorkouts.remove(workout)
        }
        notifyItemChanged(workouts.indexOf(workout)) // Уведомляем адаптер об изменении элемента
    }

        /**
     * Включает режим выбора и уведомляет об этом.
     */
    private fun enterSelectionMode() {
        selectionMode = true
        onSelectionModeChanged?.invoke(true)
        notifyDataSetChanged()
    }

    /**
     * Возвращает цвет фона в зависимости от типа тренировки и режима выбора.
     *
     * @param holder ViewHolder элемента.
     * @param normalizedType Нормализованный тип тренировки.
     * @return Цвет фона (Int).
     */
   //private fun getBackgroundColor(holder: WorkoutViewHolder, normalizedType: String): Int {
      //  return when {
        //    selectionMode -> ContextCompat.getColor(holder.itemView.context, R.color.selection_mode_background)
         //   normalizedType == "масса" -> ContextCompat.getColor(holder.itemView.context, R.color.massa)
          //  normalizedType == "сила" -> ContextCompat.getColor(holder.itemView.context, R.color.power)
          //  else -> ContextCompat.getColor(holder.itemView.context, R.color.default_background)
       // }
    //}


    private fun setBackground(holder: WorkoutViewHolder, normalizedType: String) {
        val backgroundResource = when {
            selectedWorkouts.contains(workouts[holder.adapterPosition]) -> R.drawable.selection_mode_background
            normalizedType == "масса" -> R.drawable.fon_trening_massa_power
            normalizedType == "сила" -> R.drawable.fon_power
            else -> R.drawable.default_background
        }
        holder.itemView.setBackgroundResource(backgroundResource)
    }
}


package com.example.myapplication

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.myapplication.calendar.CustomCalendarView
import java.time.LocalDate

// Диалоговое окно для добавления новой тренировки
class AddWorkoutDialogFragment(
    private val onWorkoutAdded: (String, String, LocalDate) -> Unit // Колбэк, который вызывается при добавлении тренировки
) : DialogFragment() {

    // Создание диалогового окна
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) // Создаем стандартное диалоговое окно
        dialog.setCanceledOnTouchOutside(false) // Запрещаем закрытие диалога при касании за его пределами
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Устанавливаем прозрачный фон
        return dialog
    }

    // Создание представления (UI) для диалогового окна
    override fun onCreateView(
        inflater: LayoutInflater, // Инфлейтер для создания View
        container: ViewGroup?, // Контейнер для View
        savedInstanceState: Bundle? // Сохраненное состояние (если есть)
    ): View? {
        // Надуваем макет диалогового окна
        val view = inflater.inflate(R.layout.dialog_add_workout, container, false)

        val calendarView = view.findViewById<CustomCalendarView>(R.id.custom_calendar_view)
        val selectedDate = calendarView.getSelectedDate()

        // Находим элементы интерфейса
        val workoutNameInput = view.findViewById<EditText>(R.id.edit_text_workout_name) // Поле для ввода названия тренировки
        val strengthButton = view.findViewById<Button>(R.id.btn_strength) // Кнопка "Сила"
        val massButton = view.findViewById<Button>(R.id.btn_mass) // Кнопка "Масса"
        val cancelButton = view.findViewById<Button>(R.id.btn_cancel_add)



        // Обработка нажатия на кнопку "Сила"
        strengthButton.setOnClickListener {
            val workoutName = workoutNameInput.text.toString().trim() // Получаем текст из поля ввода и убираем лишние пробелы
            val selectedDate = calendarView.getSelectedDate()
            if (workoutName.isNotEmpty()) { // Проверяем, что поле не пустое
                onWorkoutAdded(workoutName, "Сила", selectedDate) // Вызываем колбэк с названием и типом тренировки
                dismiss() // Закрываем диалоговое окно
            } else {
                // Если поле пустое, показываем ошибку
                workoutNameInput.error = "Введіть назву тренування"
            }
        }

        // Обработка нажатия на кнопку "Масса"
        massButton.setOnClickListener {
            val workoutName = workoutNameInput.text.toString().trim() // Получаем текст из поля ввода и убираем лишние пробелы
            val selectedDate = calendarView.getSelectedDate()
            if (workoutName.isNotEmpty()) { // Проверяем, что поле не пустое
                onWorkoutAdded(workoutName, "Масса", selectedDate) // Вызываем колбэк с названием и типом тренировки
                dismiss() // Закрываем диалоговое окно
            } else {
                // Если поле пустое, показываем ошибку
                workoutNameInput.error = "Введіть назву тренування"
            }
        }
        cancelButton.setOnClickListener {
            dismiss() // Закрываем диалоговое окно без сохранения
        }

        return view // Возвращаем созданное представление
    }


}

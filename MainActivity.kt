package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

// Главный экран приложения
class MainActivity : AppCompatActivity() {

    // Метод, вызываемый при создании Activity
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState) // Вызов метода родительского класса
        setContentView(R.layout.activity_main) // Установка макета для Activity

        // Находим кнопки и настраиваем их поведение
        setupButton(R.id.btn_plan, PlanActivity::class.java) // Кнопка "План тренировок"
        setupButton(R.id.btn_results) // Кнопка "Результат упражнений" (пока без перехода)
        setupButton(R.id.btn_timer) // Кнопка "Таймер" (пока без перехода)
        setupButton(R.id.btn_measures) // Кнопка "Замеры" (пока без перехода)
    }

    /**
     * Общий метод для настройки кнопок.
     *
     * @param buttonId ID кнопки из макета.
     * @param targetActivity Класс Activity, на который нужно перейти (может быть null).
     */
    private fun setupButton(buttonId: Int, targetActivity: Class<*>? = null) {
        val button = findViewById<Button>(buttonId) // Находим кнопку по ID
        button.setOnClickListener {
                val intent = Intent(this, targetActivity) // Создаем Intent для перехода
                startActivity(intent) // Запускаем Activity

            // Если targetActivity == null, кнопка пока не имеет функциональности
        }
    }
}
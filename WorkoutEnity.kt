package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Класс сущности (Entity) для таблицы тренировок в базе данных Room.
 * Описывает структуру таблицы и её поля.
 */
@Entity(tableName = "WorkoutEntity") // Указывает, что это сущность Room с именем таблицы "Workoutentity"
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) // Поле id является первичным ключом с автоматической генерацией
    val id: Int = 0, // Уникальный идентификатор тренировки (по умолчанию 0)
    val name: String, // Название тренировки
    val type: String, // Тип тренировки (например, "Сила" или "Масса")
    val date: LocalDate, // Добавляем поле для даты
    val hasExercises: Boolean = false, // Флаг наличия упражнений в тренировке
)


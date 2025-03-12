package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_results")
data class ExerciseResultEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,// ✅ Добавляем ID с авто-генерацией
    val workoutId: Int,
    var muscleGroup: String,
    val exerciseName: String,
    var sets: Int,
    var reps: Int,
    var weight: Float,
    var note: String = "", // ✅ Добавляем поле заметки с дефолтным значением
    var isSelected: Boolean = false // ✅ Добавляем поле для выделения
)

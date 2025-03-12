package com.example.myapplication

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Интерфейс DAO (Data Access Object) для работы с таблицей тренировок (WorkoutEntity) в базе данных Room.
 * Определяет методы для вставки, получения и удаления данных.
 */
@Dao
interface WorkoutDao {

    /**
     * Вставляет тренировку в базу данных.
     * Если тренировка с таким же ID уже существует, она будет заменена (REPLACE стратегия).
     *
     * @param workout Объект тренировки (WorkoutEntity) для вставки.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    /**
     * Получает все тренировки из базы данных.
     *
     * @return Список всех тренировок (List<WorkoutEntity>).
     */
    @Query("SELECT * FROM Workoutentity")
    suspend fun getAllWorkouts(): List<WorkoutEntity>

    /**
     * Удаляет список тренировок из базы данных.
     *
     * @param workouts Список тренировок (List<WorkoutEntity>) для удаления.
     */
    @Delete
    suspend fun deleteWorkouts(workouts: List<WorkoutEntity>)

//ВРЕМЕННО, ДОБАВИЛ КОГДА ДЕЛАЛ ОБРАБОТЧИКА
 //   @Query("SELECT * FROM Workoutentity WHERE id = :workoutId")
   // suspend fun getWorkoutById(workoutId: Int): WorkoutEntity?

}

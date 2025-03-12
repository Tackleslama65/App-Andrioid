package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExerciseResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(exercise: ExerciseResultEntity): Long

    @Query("SELECT * FROM exercise_results WHERE workoutId = :workoutId")
    suspend fun getResultsForWorkout(workoutId: Int): List<ExerciseResultEntity>

    @Query("UPDATE Workoutentity SET hasExercises = :hasExercises WHERE id = :workoutId")
    suspend fun updateHasExercises(workoutId: Int, hasExercises: Boolean)

    @Query("SELECT EXISTS (SELECT 1 FROM exercise_results WHERE workoutId = :workoutId)")
    suspend fun getWorkoutHasExercises(workoutId: Int): Boolean

    @Query("UPDATE exercise_results SET sets = :sets, reps = :reps, weight = :weight, note = :note WHERE id = :exerciseId")
    suspend fun updateExerciseResult(exerciseId: Int, sets: Int?, reps: Int?, weight: Float?, note: String?)

    @Update
    suspend fun updateExerciseResult(exercise: ExerciseResultEntity)

    @Query("DELETE FROM exercise_results WHERE id = :exerciseId")
    suspend fun deleteExerciseById(exerciseId: Int)

    @Query("SELECT * FROM exercise_results WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: Int): ExerciseResultEntity?

    @Query("UPDATE exercise_results SET muscleGroup = :newName WHERE muscleGroup = :oldName")
    suspend fun updateGroupName(oldName: String, newName: String): Int
}



//@Query("UPDATE exercise_results SET sets = :sets, reps = :reps, weight = :weight, note = :note WHERE id = :exerciseId")
    //suspend fun updateExerciseResultWithNote(exerciseId: Int, sets: Int, reps: Int, weight: Float, note: String)


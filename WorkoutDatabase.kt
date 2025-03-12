package com.example.myapplication

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.calendar.DateConverter

/**
 * Абстрактный класс базы данных Room для управления данными о тренировках.
 * Определяет сущности (таблицы) и версию базы данных.
 */
@Database(entities = [WorkoutEntity::class, ExerciseResultEntity::class], version = 1)
@TypeConverters(DateConverter::class) // Регистрация конвертера
abstract class WorkoutDatabase : RoomDatabase() {

    /**
     * Возвращает DAO (Data Access Object) для работы с таблицей тренировок.
     *
     * @return WorkoutDao Интерфейс для выполнения операций с базой данных.
     */
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseResultDao(): ExerciseResultDao
    /**
     * Компаньон-объект для реализации паттерна Singleton.
     * Гарантирует, что база данных будет создана только один раз.
     */
    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null // Переменная для хранения единственного экземпляра базы данных

        /**
         * Возвращает экземпляр базы данных. Если база данных еще не создана, она будет инициализирована.
         *
         * @param context Контекст приложения.
         * @return WorkoutDatabase Единственный экземпляр базы данных.
         */
        fun getInstance(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) { // Синхронизация для потокобезопасности
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Контекст приложения
                    WorkoutDatabase::class.java, // Класс базы данных
                    "workout_database" // Имя базы данных
                ).fallbackToDestructiveMigration() // Разрешает удаление данных при миграции
                    .build() // Создает базу данных
                INSTANCE = instance // Сохраняет экземпляр базы данных
                instance // Возвращает экземпляр базы данных
            }
        }
    }
}

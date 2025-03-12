package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkoutSetupActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnConfirm: Button
    private lateinit var database: WorkoutDatabase
    private var workoutId: Int = -1
    private var groupCount: Int = 1

    private var muscleGroups = mutableListOf<MuscleGroupAdapter.MuscleGroup>()
    private lateinit var adapter: MuscleGroupAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_setup)
        recyclerView = findViewById(R.id.recycler_view_groups)
        btnConfirm = findViewById(R.id.btnConfirm)
        database = WorkoutDatabase.getInstance(this)
        workoutId = intent.getIntExtra("WORKOUT_ID", -1)
        groupCount = intent.getIntExtra("GROUP_COUNT", 1)

        adapter = MuscleGroupAdapter(
            muscleGroups,
            ::showExerciseSelectionDialog,
            ::deleteExerciseById,
            ::updateExerciseResult,
            ::renameGroup // Обработчик переименования группы
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        // Обработчик перемещения упражнений
        adapter.onExerciseMoved = { fromPosition, toPosition, groupNumber ->
            lifecycleScope.launch {
                val dao = database.exerciseResultDao()
                val group = muscleGroups.find { it.number == groupNumber } ?: return@launch
                val exercise = group.exercises[toPosition]
                dao.updateExerciseResult(exercise)
            }
        }

        // Загружаем сохраненные упражнения
        loadSavedExercises(workoutId)


        btnConfirm.setOnClickListener {
            saveWorkoutData()
            finish()
        }
    }

   // private fun showExerciseSelectionDialog(groupNumber: Int) {
     //   val exercises = arrayOf("Жим лёжа", "Приседания", "Становая тяга", "Подтягивания", "Отжимания")
       // val selectedExercises = mutableListOf<String>()

        //val builder = AlertDialog.Builder(this)
          //  .setTitle("Выберите упражнения для Группы $groupNumber")
            //.setMultiChoiceItems(exercises, null) { _, which, isChecked ->
              //  if (isChecked) {
                //    selectedExercises.add(exercises[which])
               // } else {
                 //   selectedExercises.remove(exercises[which])
               // }
            //}
            //.setPositiveButton("OK") { dialog, _ ->
              //  saveSelectedExercises(selectedExercises, groupNumber)
               // dialog.dismiss()
            //}
            //.setNegativeButton("Отмена") { dialog, _ ->
              //  dialog.dismiss()
            //}

        //val dialog = builder.create()

        // Устанавливаем кастомную тему
       // dialog.window?.decorView?.setBackgroundColor(ContextCompat.getColor(this, R.color.background_fon_dialog))

       // dialog.show()
    //}

    private fun showExerciseSelectionDialog(groupNumber: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_exercise_selection, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.muscle_group_spinner)
        val container = dialogView.findViewById<LinearLayout>(R.id.exercise_list_container)


        // Находим кнопки в макете
        val btnOk = dialogView.findViewById<Button>(R.id.button_ok) // Замените R.id.btnOk на ваш ID
        val btnCancel = dialogView.findViewById<Button>(R.id.button_cancel) // Замените R.id.btnCancel на ваш ID

        val muscleGroups = listOf("Базові вправи","Плечи", "Спина", "Грудь", "Бицепс","Трицепс", "Передпліччя", "Ноги")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, muscleGroups)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        // Загрузка упражнений при выборе группы
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                container.removeAllViews()
                val layoutResId = when (muscleGroups[position]) {
                    "Базові вправи" -> R.layout.exercise_baza
                    "Плечи" -> R.layout.exercise_shoulders
                    "Спина" -> R.layout.exercise_back
                    "Грудь" -> R.layout.exercise_breast
                    "Бицепс" -> R.layout.exercise_biceps
                    "Трицепс" -> R.layout.exercise_triceps
                    "Ноги" -> R.layout.exercise_legs
                    "Передпліччя" -> R.layout.exercise_forearm
                    else -> R.layout.exercise_baza
                }
                LayoutInflater.from(this@WorkoutSetupActivity).inflate(layoutResId, container, true)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Создаем AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Обработчик для кнопки "ОК"
        btnOk.setOnClickListener {
            val selectedExercises = mutableListOf<String>()

            // Рекурсивный поиск CheckBox внутри container
            fun findSelectedExercises(view: View) {
                if (view is ViewGroup) {
                    for (i in 0 until view.childCount) {
                        findSelectedExercises(view.getChildAt(i))
                    }
                } else if (view is CheckBox && view.isChecked) {
                    val parentView = view.parent as? ViewGroup

                    // Извлекаем номер группы и номер упражнения из ID CheckBox
                    val checkBoxIdName = resources.getResourceEntryName(view.id) // Получаем имя ID (exercise_1_3)
                    val parts = checkBoxIdName.split("_") // Разбиваем "exercise_1_3" → ["exercise", "1", "3"]

                    if (parts.size == 3) {
                        val groupNumber = parts[1] // Группа (1, 2, 3...)
                        val exerciseNumber = parts[2] // Номер упражнения (1, 2, 3...)

                        // Формируем ID связанного TextView (text_exercise_1_3)
                        val textViewIdName = "text_exercise_${groupNumber}_$exerciseNumber"
                        val textViewId = resources.getIdentifier(textViewIdName, "id", view.context.packageName)

                        // Ищем TextView с этим ID
                        val nameText = parentView?.findViewById<TextView>(textViewId)

                        if (nameText != null) {
                            selectedExercises.add(nameText.text.toString())
                        }
                    }
                }
            }

            findSelectedExercises(container)

            // Определяем номер группы
            //val groupNumber = muscleGroups.indexOf(spinner.selectedItem.toString()) + 1

            // Сохраняем упражнения
            saveSelectedExercises(selectedExercises, groupNumber)
            dialog.dismiss()
        }

        // Обработчик для кнопки "Отмена"
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        // Показываем диалог
        dialog.show()


        // Настройка окна диалога на весь экран
        val window = dialog.window
        window?.let {
            // Устанавливаем параметры окна
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(it.attributes)

            // Устанавливаем ширину и высоту окна на весь экран
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

            // Применяем параметры
            it.attributes = layoutParams
            it.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }


    fun saveSelectedExercises(selectedExercises: List<String>, groupNumber: Int) {
        if (workoutId == -1) return

        lifecycleScope.launch {
            val dao = database.exerciseResultDao()
            val exercises = selectedExercises.map { exerciseName ->
                val exercise = ExerciseResultEntity(
                    workoutId = workoutId,
                    muscleGroup = "Группа $groupNumber",
                    exerciseName = exerciseName,
                    sets = 0,
                    reps = 0,
                    weight = 0f,
                    note = ""
                )
                exercise.id = dao.insertResult(exercise).toInt()
                exercise
            }

            withContext(Dispatchers.Main) {
                val group = muscleGroups.find { it.number == groupNumber }
                if (group != null) {
                    group.exercises.addAll(exercises)
                } else {
                    muscleGroups.add(MuscleGroupAdapter.MuscleGroup(groupNumber, "Группа $groupNumber", exercises.toMutableList()))
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun loadSavedExercises(workoutId: Int) {
        if (workoutId == -1) return

        lifecycleScope.launch {
            try {
                val dao = database.exerciseResultDao()
                val exercises = dao.getResultsForWorkout(workoutId)
                Log.d("WorkoutSetupActivity", "Loaded exercises: $exercises")
                withContext(Dispatchers.Main) {
                    muscleGroups.clear()
                    if (exercises.isNotEmpty()) {
                        exercises.groupBy { it.muscleGroup }.forEach { (group, groupExercises) ->
                            // Попытка извлечь номер группы из имени
                            val groupNumber = extractGroupNumber(group) ?: muscleGroups.size + 1
                            muscleGroups.add(MuscleGroupAdapter.MuscleGroup(groupNumber, group, groupExercises.toMutableList()))
                            Log.d("WorkoutSetupActivity", "Added group $group with ${groupExercises.size} exercises")
                        }
                    } else {
                        createMuscleGroups(groupCount)
                        Log.d("WorkoutSetupActivity", "Created new groups")
                    }
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("WorkoutSetupActivity", "Error loading exercises", e)
            }
        }
    }




    private fun createMuscleGroups(count: Int) {
        for (i in 1..count) {
            // Проверяем, существует ли группа с таким номером
            if (muscleGroups.none { it.number == i }) {
                muscleGroups.add(MuscleGroupAdapter.MuscleGroup(i, "Группа $i", mutableListOf()))
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun deleteExerciseById(exerciseId: Int) {
        lifecycleScope.launch {
            val dao = database.exerciseResultDao()
            dao.deleteExerciseById(exerciseId)
        }
    }

    private fun updateExerciseResult(exerciseId: Int, sets: Int, reps: Int, weight: Float, note: String) {
        lifecycleScope.launch {
            val dao = database.exerciseResultDao()
            val exercise = dao.getExerciseById(exerciseId) ?: return@launch
            exercise.sets = sets
            exercise.reps = reps
            exercise.weight = weight
            exercise.note = note
            dao.updateExerciseResult(exercise)
        }
    }

    private fun saveWorkoutData() {
        if (workoutId == -1) return

        lifecycleScope.launch {
            try {
                val dao = database.exerciseResultDao()
                muscleGroups.forEach { group ->
                    group.exercises.forEach { exercise ->
                        Log.d("WorkoutSetupActivity", "Saving exercise: id=${exercise.id}, sets=${exercise.sets}, reps=${exercise.reps}, weight=${exercise.weight}, note=${exercise.note}")
                        dao.updateExerciseResult(
                            exercise.id,
                            exercise.sets,
                            exercise.reps,
                            exercise.weight,
                            exercise.note
                        )
                    }
                }
                loadSavedExercises(workoutId)
            } catch (e: Exception) {
                Log.e("WorkoutSetupActivity", "Error saving workout data", e)
            }
        }
    }
//Для обработчика переименование группы
    private fun renameGroup(groupNumber: Int, newName: String) {
        lifecycleScope.launch {
            try {
                val dao = database.exerciseResultDao()
                val oldName = "Группа $groupNumber"
                val updatedRows = dao.updateGroupName(oldName, newName)
                Log.d("WorkoutSetupActivity", "Renamed group $oldName to $newName, updated rows: $updatedRows")

                // Обновляем название группы в адаптере
                val group = muscleGroups.find { it.number == groupNumber }
                group?.muscleGroup = newName
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("WorkoutSetupActivity", "Error renaming group", e)
                // Можно показать пользователю сообщение об ошибке
            }
        }
    }
    //Метод extractGroupNumber будет извлекать номер группы из имени, если имя соответствует формату "Группа X". В противном случае, он вернет null.
    private fun extractGroupNumber(groupName: String): Int? {
        val regex = Regex("Группа\\s+(\\d+)")
        val matchResult = regex.matchEntire(groupName)
        return matchResult?.groupValues?.get(1)?.toIntOrNull()
    }




}
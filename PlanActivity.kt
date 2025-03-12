package com.example.myapplication

import android.app.Dialog
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.time.LocalDate

// Экран "План тренировок"
class PlanActivity : AppCompatActivity() {

    private lateinit var workoutAdapter: WorkoutAdapter // Адаптер для RecyclerView
    private lateinit var database: WorkoutDatabase // База данных

    //  private lateinit var workoutList: List<WorkoutEntity>
    private lateinit var recyclerView: RecyclerView
    private lateinit var workoutList: MutableList<WorkoutEntity>

    private lateinit var filterTypeSpinner: Spinner
    private lateinit var searchView: SearchView
    private lateinit var originalWorkoutList: List<WorkoutEntity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plan) // Установка макета для Activity

        workoutList = mutableListOf() // Здесь загрузим тренировки из БД
        workoutAdapter = WorkoutAdapter(workoutList) { workout -> onWorkoutClick(workout) }

        // Инициализация базы данных
        database = WorkoutDatabase.getInstance(this)

        // Настройка RecyclerView
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view_workouts) // Инициализируем recyclerView
        workoutAdapter.onSelectionModeChanged = { isSelectionMode ->
            updateButtonsVisibility(isSelectionMode) // Обновляем видимость кнопок при изменении режима выбора
        }

        recyclerView.adapter = workoutAdapter // Устанавливаем адаптер
        recyclerView.itemAnimator = DefaultItemAnimator() // Добавляем анимацию

        // Настройка RecyclerView
        setupRecyclerView()

        // Добавляем отступы между элементами RecyclerView
        val spacing = resources.getDimensionPixelSize(R.dimen.recycler_spacing)
        recyclerView.addItemDecoration(SpaceItemDecoration(spacing))

        // Кнопка добавления тренировки
        val addButton = findViewById<Button>(R.id.fab)
        addButton.setOnClickListener {
            // Открываем диалоговое окно для добавления тренировки
            val dialog = AddWorkoutDialogFragment { name, type, date ->
                addWorkoutToDatabase(name, type, date) // Добавляем тренировку в базу данных
            }
            dialog.show(supportFragmentManager, "AddWorkoutDialogFragment")
        }

        // Кнопки удаления и отмены выбора
        val deleteButton = findViewById<Button>(R.id.btn_delete_selected)
        val cancelButton = findViewById<Button>(R.id.btn_cancel_selection)

        // Сначала скрываем кнопки
        deleteButton.visibility = View.GONE
        cancelButton.visibility = View.GONE

        // Обработка нажатия на кнопку удаления
        deleteButton.setOnClickListener {
            val selectedWorkouts =
                workoutAdapter.getSelectedWorkouts() // Получаем выбранные тренировки
            if (selectedWorkouts.isNotEmpty()) {
                deleteWorkoutsFromDatabase(selectedWorkouts) // Удаляем из базы данных
                workoutAdapter.deleteSelectedWorkouts() // Удаляем из адаптера
                updateButtonsVisibility(false) // Скрываем кнопки
            }
        }

        // Обработка нажатия на кнопку отмены выбора
        cancelButton.setOnClickListener {
            workoutAdapter.clearSelection() // Сбрасываем выбор
            updateButtonsVisibility(false) // Скрываем кнопки
        }



        // Инициализация Spinner и SearchView
        filterTypeSpinner = findViewById(R.id.filterTypeSpinner)
        searchView = findViewById(R.id.searchView)

        // Настройка Spinner
        val workoutTypes = listOf("Все", "Сила", "Масса")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, workoutTypes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterTypeSpinner.adapter = spinnerAdapter

        // Обработчик выбора типа тренировки
        filterTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Обработчик поиска по названию
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                applyFilters()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                applyFilters()
                return true
            }
        })






        // Загрузка данных из базы данных при старте
        loadWorkoutsFromDatabase()
    }


    private fun setupRecyclerView() {
        val layoutManager = TwoColumnStackFromEndLayoutManager()
        recyclerView.layoutManager = layoutManager


    }

    /**
     * Обновляет видимость кнопок удаления и отмены выбора.
     *
     * @param isSelectionMode Флаг, указывающий, активен ли режим выбора.
     */
    private fun updateButtonsVisibility(isSelectionMode: Boolean) {
        val deleteButton = findViewById<Button>(R.id.btn_delete_selected)
        val cancelButton = findViewById<Button>(R.id.btn_cancel_selection)

        if (isSelectionMode) {
            deleteButton.visibility = View.VISIBLE // Показываем кнопку удаления
            cancelButton.visibility = View.VISIBLE // Показываем кнопку отмены
        } else {
            deleteButton.visibility = View.GONE // Скрываем кнопку удаления
            cancelButton.visibility = View.GONE // Скрываем кнопку отмены
        }
    }

    /**
     * Добавляет тренировку в базу данных.
     *
     * @param name Название тренировки.
     * @param type Тип тренировки.
     */
    private fun addWorkoutToDatabase(name: String, type: String, date: LocalDate) {
        lifecycleScope.launch {
            val workout =
                WorkoutEntity(name = name, type = type, date = date) // Создаем объект тренировки
            database.workoutDao().insertWorkout(workout) // Вставляем тренировку в базу данных
            loadWorkoutsFromDatabase() // Обновляем список тренировок
            //recyclerView.scrollToPosition(0)
        }
    }

    /**
     * Загружает тренировки из базы данных и обновляет адаптер.
     */
    private fun loadWorkoutsFromDatabase() {
        lifecycleScope.launch {
            val workouts = database.workoutDao().getAllWorkouts() // Получаем все тренировки из базы

            originalWorkoutList = workouts // Сохраняем оригинальный список
            workoutList = workouts.toMutableList()
            applyFilters() // Применяем фильтры после загрузки
        }
    }

    /**
     * Удаляет выбранные тренировки из базы данных.
     *
     * @param workouts Список тренировок для удаления.
     */
    private fun deleteWorkoutsFromDatabase(workouts: List<WorkoutEntity>) {
        lifecycleScope.launch {
            database.workoutDao().deleteWorkouts(workouts) // Удаляем тренировки из базы
            loadWorkoutsFromDatabase() // Обновляем список тренировок
        }
    }

    private fun onWorkoutClick(workout: WorkoutEntity) {
        lifecycleScope.launch {
            val dao = database.exerciseResultDao()
            val hasExercises =
                dao.getWorkoutHasExercises(workout.id) // 🔹 Проверяем, есть ли упражнения

            if (hasExercises) {
                // ✅ Если упражнения есть → сразу открываем экран с упражнениями
                val intent = Intent(this@PlanActivity, WorkoutSetupActivity::class.java)
                intent.putExtra("WORKOUT_ID", workout.id)
                startActivity(intent)
            } else {
                // ✅ Если упражнений нет → показываем диалог выбора групп
                showMuscleGroupDialog(workout)
            }
        }
    }

    private fun showMuscleGroupDialog(workout: WorkoutEntity) {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_group_type)
        val btnGroup1 = dialog.findViewById<Button>(R.id.btnGroup1)
        val btnGroup2 = dialog.findViewById<Button>(R.id.btnGroup2)
        val btnGroup3 = dialog.findViewById<Button>(R.id.btnGroup3)
        val btnGroup4 = dialog.findViewById<Button>(R.id.btnGroup4)
        val btnGroup5 = dialog.findViewById<Button>(R.id.btnGroup5)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        val clickListener = View.OnClickListener { view ->
            val groupCount = when (view.id) {
                R.id.btnGroup1 -> 1
                R.id.btnGroup2 -> 2
                R.id.btnGroup3 -> 3
                R.id.btnGroup4 -> 4
                R.id.btnGroup5 -> 5
                else -> 0
            }
            dialog.dismiss()
            openWorkoutSetupScreen(workout.id, groupCount) // Передаём workout.id и groupCount
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Устанавливаем прозрачный фон

        btnGroup1.setOnClickListener(clickListener)
        btnGroup2.setOnClickListener(clickListener)
        btnGroup3.setOnClickListener(clickListener)
        btnGroup4.setOnClickListener(clickListener)
        btnGroup5.setOnClickListener(clickListener)
        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun openWorkoutSetupScreen(workoutId: Int, groupCount: Int) {
        val intent = Intent(this, WorkoutSetupActivity::class.java)
        intent.putExtra("WORKOUT_ID", workoutId)
        intent.putExtra("GROUP_COUNT", groupCount)
        startActivity(intent)
    }

    private fun applyFilters() {
        val selectedType = filterTypeSpinner.selectedItem as String
        val searchQuery = searchView.query.toString()

        val filteredList = originalWorkoutList.filter { workout ->
            // Фильтр по типу тренировки
            val matchesType = selectedType == "Все" || workout.type == selectedType

            // Фильтр по названию тренировки
            val matchesName = workout.name.contains(searchQuery, ignoreCase = true)

            matchesType && matchesName
        }

        // Обновляем адаптер с отфильтрованным списком
        workoutAdapter.updateData(filteredList)
    }

}










// Класс для добавления отступов между элементами RecyclerView
class SpaceItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = spacing
        outRect.right = spacing
        outRect.bottom = spacing
        outRect.top = spacing
    }
}
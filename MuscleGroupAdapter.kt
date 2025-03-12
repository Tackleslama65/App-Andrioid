package com.example.myapplication

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class MuscleGroupAdapter(
    private val groups: MutableList<MuscleGroup>,
    private val onAddExerciseClick: (Int) -> Unit,
    private val onDeleteExerciseClick: (Int) -> Unit,
    private val onExerciseDataChanged: (Int, Int, Int, Float, String) -> Unit,
    private val onRenameGroupClick: (Int, String) -> Unit // Обработчик переименования группы
) : RecyclerView.Adapter<MuscleGroupAdapter.MuscleGroupViewHolder>() {
    // Добавьте атрибуты для управления режимом удаления и выбранными упражнениями
    private var isDeleteMode = false
    private val selectedExercises = mutableListOf<Int>()

    // Добавляем флаг для режима перемещения
    private var isMoveMode = false

    // Интерфейс для обработки перемещения
    var onExerciseMoved: ((Int, Int, Int) -> Unit)? = null

    data class MuscleGroup(
        val number: Int,
        var muscleGroup: String, // Добавляем свойство muscleGroup
        val exercises: MutableList<ExerciseResultEntity>
    )

    class MuscleGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val muscleGroupTitle: TextView = itemView.findViewById(R.id.muscleGroupTitle)
        val btnAddExercise: Button = itemView.findViewById(R.id.btnAddExercise)
        val muscleGroupContainer: LinearLayout = itemView.findViewById(R.id.muscleGroupContainer)
        val btnDeleteCancelContainer: LinearLayout = itemView.findViewById(R.id.btnDeleteCancelContainer)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val btnCancel: Button = itemView.findViewById(R.id.btnCancel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MuscleGroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_muscle_group, parent, false)
        return MuscleGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: MuscleGroupViewHolder, position: Int) {
        val group = groups[position]
        holder.muscleGroupTitle.text = "Группа ${group.number}"
        holder.btnAddExercise.setTag(R.id.btnAddExercise, group.number)
        holder.btnAddExercise.setOnClickListener { onAddExerciseClick(group.number) }

        holder.muscleGroupTitle.text = group.muscleGroup // Используем новое название группы
        // Обработчик нажатия на название группы
        holder.muscleGroupTitle.setOnClickListener {
            showRenameGroupDialog(holder.itemView.context, group.number)
        }

        holder.btnDelete.setOnClickListener {
            val selectedExercises = group.exercises.filter { it.isSelected }
            selectedExercises.forEach { exercise ->
                onDeleteExerciseClick(exercise.id)
            }
            group.exercises.removeAll(selectedExercises)
            notifyDataSetChanged()
        }

        holder.btnCancel.setOnClickListener {
            group.exercises.forEach { it.isSelected = false }
            isDeleteMode = false
            notifyDataSetChanged()
        }

        holder.muscleGroupContainer.removeAllViews()
        group.exercises.forEach { exercise ->
            val exerciseView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.item_exercise, holder.muscleGroupContainer, false)
            exerciseView.setTag(R.id.exercise_id, exercise.id)

            val exerciseNameText = exerciseView.findViewById<TextView>(R.id.exerciseNameText)
            val setsEditText = exerciseView.findViewById<EditText>(R.id.setsEditText)
            val repsEditText = exerciseView.findViewById<EditText>(R.id.repsEditText)
            val weightEditText = exerciseView.findViewById<EditText>(R.id.weightEditText)
            val noteEditText = exerciseView.findViewById<EditText>(R.id.noteEditText)

            exerciseNameText.text = exercise.exerciseName
            setsEditText.setText(if (exercise.sets == 0) "" else exercise.sets.toString())
            repsEditText.setText(if (exercise.reps == 0) "" else exercise.reps.toString())
            weightEditText.setText(if (exercise.weight == 0f) "" else exercise.weight.toString())
            noteEditText.setText(exercise.note)

            val cardViewExercise = exerciseView.findViewById<CardView>(R.id.cardViewExercise)
            cardViewExercise.setCardBackgroundColor(
                if (exercise.isSelected) ContextCompat.getColor(holder.itemView.context, R.color.selection_mode_background)
                else ContextCompat.getColor(holder.itemView.context, R.color.background_fon_yprag)
            )

            // Обработка долгого нажатия на упражнение
            exerciseView.setOnLongClickListener {
                if (!isDeleteMode) {
                    isDeleteMode = true
                    isMoveMode = true // Включаем режим перемещения
                    holder.btnDeleteCancelContainer.visibility = View.VISIBLE
                }
                exercise.isSelected = !exercise.isSelected
                updateExerciseCardColor(cardViewExercise, exercise.isSelected)
                true
            }

            // Обработчик перетаскивания
            holder.itemView.setOnTouchListener { view, event ->
                if (isMoveMode) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // Начало перетаскивания
                            view.alpha = 0.5f
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            // Перемещение элемента
                            view.translationX = event.rawX - view.width / 2
                            view.translationY = event.rawY - view.height / 2
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            // Завершение перетаскивания
                            view.alpha = 1.0f
                            view.translationX = 0f
                            view.translationY = 0f
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }


            // Обработчик клика
            holder.itemView.setOnClickListener {
                if (isDeleteMode || isMoveMode) {
                    // Сбрасываем режимы
                    isDeleteMode = false
                    isMoveMode = false
                    holder.btnDeleteCancelContainer.visibility = View.GONE
                    notifyDataSetChanged()
                }
            }

            // Обработка клика на упражнение
            exerciseView.setOnClickListener {
                if (isDeleteMode) {
                    exercise.isSelected = !exercise.isSelected
                    updateExerciseCardColor(cardViewExercise, exercise.isSelected)
                }
            }

            val textWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val sets = setsEditText.text.toString().toIntOrNull() ?: 0
                    val reps = repsEditText.text.toString().toIntOrNull() ?: 0
                    val weight = weightEditText.text.toString().toFloatOrNull() ?: 0f
                    val note = noteEditText.text.toString()

                    // Обновляем объект ExerciseResultEntity
                    exercise.sets = sets
                    exercise.reps = reps
                    exercise.weight = weight
                    exercise.note = note

                    // Вызываем колбэк для обновления данных в базе
                    onExerciseDataChanged(exercise.id, sets, reps, weight, note)
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

            setsEditText.addTextChangedListener(textWatcher)
            repsEditText.addTextChangedListener(textWatcher)
            weightEditText.addTextChangedListener(textWatcher)
            noteEditText.addTextChangedListener(textWatcher)
            holder.muscleGroupContainer.addView(exerciseView)
        }

        // Обработка отмены режима удаления
        if (!isDeleteMode) {
            holder.btnDeleteCancelContainer.visibility = View.GONE
        }
    }

    // ДИАЛОГ ПЕРЕИМЕНОВАНИЕ ГРУППЫ
    private fun showRenameGroupDialog(context: Context, groupNumber: Int) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_rename_group, null)

        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val input = view.findViewById<EditText>(R.id.edit_text_group_name)
        val btnOk = view.findViewById<Button>(R.id.btn_ok)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

        tvTitle.text = "Перейменувати групу $groupNumber"

        // Добавляем TextWatcher для автоматического преобразования первой буквы в заглавную
        input.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > 0) {
                    val firstChar = s[0]
                    if (firstChar.isLowerCase()) {
                        s.replace(0, 1, firstChar.titlecase().toString())
                    }
                }
            }
        })

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .create()

        btnOk.setOnClickListener {
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                onRenameGroupClick(groupNumber, newName)
                dialog.dismiss()
            } else {
                input.error = "Введіть нову назву групи"
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun updateExerciseCardColor(cardViewExercise: CardView, isSelected: Boolean) {
        cardViewExercise.setCardBackgroundColor(
            if (isSelected) ContextCompat.getColor(cardViewExercise.context, R.color.selection_mode_background)
            else ContextCompat.getColor(cardViewExercise.context, R.color.background_fon_yprag)
        )
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    abstract class ExerciseResultTextWatcher(private val exerciseId: Int) : TextWatcher {
        abstract fun onTextChanged(exerciseId: Int, sets: Int?, reps: Int?, weight: Float?, note: String?)

        override fun afterTextChanged(s: Editable?) {
            val sets = s?.toString()?.toIntOrNull()
            val reps = s?.toString()?.toIntOrNull()
            val weight = s?.toString()?.toFloatOrNull()
            val note = s?.toString()
            onTextChanged(exerciseId, sets, reps, weight, note)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    fun getGroupNumberForPosition(position: Int): Int {
        var currentPosition = 0
        for (group in groups) {
            if (position < currentPosition + group.exercises.size) {
                return group.number
            }
            currentPosition += group.exercises.size
        }
        return -1
    }

    // Включаем режим перемещения
    fun enableMoveMode() {
        isMoveMode = true
        notifyDataSetChanged()
    }

    // Отключаем режим перемещения
    fun disableMoveMode() {
        isMoveMode = false
        notifyDataSetChanged()
    }

    // Перемещение упражнения внутри группы
    fun moveExercise(fromPosition: Int, toPosition: Int, groupNumber: Int) {
        val group = groups.find { it.number == groupNumber } ?: return
        if (fromPosition < 0 || toPosition < 0 || fromPosition >= group.exercises.size || toPosition >= group.exercises.size) {
            return
        }
        Collections.swap(group.exercises, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        onExerciseMoved?.invoke(fromPosition, toPosition, groupNumber)
    }
}

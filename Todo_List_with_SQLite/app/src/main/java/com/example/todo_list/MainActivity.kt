package com.example.todo_list

import android.app.DatePickerDialog
import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()
    private lateinit var deadlineEditText: EditText
    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var completedCheckBox: CheckBox
    private var selectedTaskId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = TaskDatabaseHelper(this)
        taskAdapter = TaskAdapter(this, tasks)
        findViewById<ListView>(R.id.listView).adapter = taskAdapter

        deadlineEditText = findViewById(R.id.deadlineEditText)
        completedCheckBox = findViewById(R.id.completedCheckBox)
        deadlineEditText.setOnClickListener {
            showDatePickerDialog()
        }

        findViewById<Button>(R.id.addButton).setOnClickListener {
            addTask()
        }

        findViewById<Button>(R.id.editButton).setOnClickListener {
            editTask()
        }

        findViewById<Button>(R.id.deleteButton).setOnClickListener {
            deleteTask()
        }

        findViewById<ListView>(R.id.listView).setOnItemClickListener { _, _, position, _ ->
            val task = tasks[position]
            selectedTaskId = task.id
            findViewById<EditText>(R.id.nameEditText).setText(task.name)
            deadlineEditText.setText(task.deadline)
            findViewById<EditText>(R.id.durationEditText).setText(task.duration)
            findViewById<EditText>(R.id.descriptionEditText).setText(task.description)
            completedCheckBox.isChecked = task.completed
        }

        loadTasks()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            deadlineEditText.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun addTask() {
        val name = findViewById<EditText>(R.id.nameEditText).text.toString()
        val deadline = deadlineEditText.text.toString()
        val duration = findViewById<EditText>(R.id.durationEditText).text.toString()
        val description = findViewById<EditText>(R.id.descriptionEditText).text.toString()
        val completed = completedCheckBox.isChecked

        if (name.isNotEmpty() && deadline.isNotEmpty() && duration.isNotEmpty() && description.isNotEmpty()) {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(TaskDatabaseHelper.COLUMN_NAME, name)
                put(TaskDatabaseHelper.COLUMN_DEADLINE, deadline)
                put(TaskDatabaseHelper.COLUMN_DURATION, duration)
                put(TaskDatabaseHelper.COLUMN_DESCRIPTION, description)
                put(TaskDatabaseHelper.COLUMN_COMPLETED, if (completed) 1 else 0)
            }
            val newRowId = db.insert(TaskDatabaseHelper.TABLE_NAME, null, values)
            if (newRowId != -1L) {
                val task = Task(newRowId, name, deadline, duration, description, completed)
                tasks.add(task)
                taskAdapter.notifyDataSetChanged()
                clearInputFields()
            } else {
                Toast.makeText(this, "Error adding task", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editTask() {
        val name = findViewById<EditText>(R.id.nameEditText).text.toString()
        val deadline = deadlineEditText.text.toString()
        val duration = findViewById<EditText>(R.id.durationEditText).text.toString()
        val description = findViewById<EditText>(R.id.descriptionEditText).text.toString()
        val completed = completedCheckBox.isChecked

        if (selectedTaskId != null && name.isNotEmpty() && deadline.isNotEmpty() && duration.isNotEmpty() && description.isNotEmpty()) {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(TaskDatabaseHelper.COLUMN_NAME, name)
                put(TaskDatabaseHelper.COLUMN_DEADLINE, deadline)
                put(TaskDatabaseHelper.COLUMN_DURATION, duration)
                put(TaskDatabaseHelper.COLUMN_DESCRIPTION, description)
                put(TaskDatabaseHelper.COLUMN_COMPLETED, if (completed) 1 else 0)
            }
            val rowsAffected = db.update(TaskDatabaseHelper.TABLE_NAME, values, "${TaskDatabaseHelper.COLUMN_ID}=?", arrayOf(selectedTaskId.toString()))
            if (rowsAffected > 0) {
                val taskIndex = tasks.indexOfFirst { it.id == selectedTaskId }
                if (taskIndex != -1) {
                    tasks[taskIndex] = Task(selectedTaskId!!, name, deadline, duration, description, completed)
                    taskAdapter.notifyDataSetChanged()
                    clearInputFields()
                    selectedTaskId = null
                }
            } else {
                Toast.makeText(this, "Error editing task", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTask() {
        if (selectedTaskId != null) {
            val db = dbHelper.writableDatabase
            val rowsDeleted = db.delete(TaskDatabaseHelper.TABLE_NAME, "${TaskDatabaseHelper.COLUMN_ID}=?", arrayOf(selectedTaskId.toString()))
            if (rowsDeleted > 0) {
                tasks.removeAll { it.id == selectedTaskId }
                taskAdapter.notifyDataSetChanged()
                clearInputFields()
                selectedTaskId = null
            } else {
                Toast.makeText(this, "Error deleting task", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No task selected to delete", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTasks() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            TaskDatabaseHelper.TABLE_NAME,
            null, null, null, null, null, null
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_ID))
                val name = getString(getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_NAME))
                val deadline = getString(getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_DEADLINE))
                val duration = getString(getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_DURATION))
                val description = getString(getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_DESCRIPTION))
                val completed = getInt(getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_COMPLETED)) == 1
                tasks.add(Task(id, name, deadline, duration, description, completed))
            }
        }
        cursor.close()
        taskAdapter.notifyDataSetChanged()
    }

    private fun clearInputFields() {
        findViewById<EditText>(R.id.nameEditText).text.clear()
        deadlineEditText.text.clear()
        findViewById<EditText>(R.id.durationEditText).text.clear()
        findViewById<EditText>(R.id.descriptionEditText).text.clear()
        completedCheckBox.isChecked = false
    }
}
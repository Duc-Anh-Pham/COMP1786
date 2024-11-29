package com.example.todo_list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView

class TaskAdapter(private val context: Context, private val tasks: List<Task>) : BaseAdapter() {

    override fun getCount(): Int {
        return tasks.size
    }

    override fun getItem(position: Int): Any {
        return tasks[position]
    }

    override fun getItemId(position: Int): Long {
        return tasks[position].id
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)

        val task = tasks[position]

        val nameTextView = view.findViewById<TextView>(R.id.nameTextView)
        val deadlineTextView = view.findViewById<TextView>(R.id.deadlineTextView)
        val durationTextView = view.findViewById<TextView>(R.id.durationTextView)
        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionTextView)
        val completedCheckBox = view.findViewById<CheckBox>(R.id.completedCheckBox)

        nameTextView.text = task.name
        deadlineTextView.text = task.deadline
        durationTextView.text = task.duration
        descriptionTextView.text = task.description
        completedCheckBox.isChecked = task.completed

        return view
    }
}
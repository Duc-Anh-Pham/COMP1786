package com.example.todo_list

data class Task(
    val id: Long = 0,
    val name: String,
    val deadline: String,
    val duration: String,
    val description: String,
    val completed: Boolean = false
)
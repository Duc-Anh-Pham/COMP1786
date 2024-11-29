package com.example.todo_list

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_DEADLINE TEXT,
                $COLUMN_DURATION TEXT,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_COMPLETED INTEGER
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "tasks.db"
        const val DATABASE_VERSION = 2
        const val TABLE_NAME = "tasks"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_DEADLINE = "deadline"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_COMPLETED = "completed"
    }
}
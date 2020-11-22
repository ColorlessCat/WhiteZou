package com.colorlesscat.whitezou.functions

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(
    context: Context?,
    name: String? = "main",
    factory: SQLiteDatabase.CursorFactory? = null,
    version: Int = 1
) : SQLiteOpenHelper(context, name, factory, version) {
    val enumToString = { it: Task.Status ->
        when (it) {
            Task.Status.READY -> "ready"
            Task.Status.ALREADY -> "already"
            Task.Status.DOING -> "doing"
            Task.Status.PAUSE -> "pause"
            Task.Status.END -> "end"
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        //SQLite不支持bool型用int代替　
        db!!.execSQL("create table tasks(is_down int,is_part int,down_url text,file_path text,part_path text,name text,size int,part_size text,up_dir text,part_index int,status text,id text,date int);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")

    }

    //id等于空字符串就查询所有　否则只查一条
    fun query(id: String = ""): List<Task> {
        //新知识GET:查询到的结果的顺序不是按照建表时列的顺序来的　而是按照下面的数据
        val cursor = readableDatabase.query(
            "tasks",
            arrayOf(
                "is_down",
                "is_part",
                "down_url",
                "file_path",
                "part_path",
                "name",
                "size",
                "part_size",
                "up_dir",
                "part_index",
                "status",
                "id",
                "date"
            ),
            if (id == "") null else "id = ?",
            if (id == "") null else arrayOf(id),
            null,
            null,
            "date desc"
        )
        //TODO 按时间排序参数该怎么传还不知道
        val result = mutableListOf<Task>()
        var task: Task? = null
        while (cursor.moveToNext()) {
            task = Task.Builder().isDown(cursor.getInt(0) == 1)
                .isPart(cursor.getInt(1) == 1)
                .setDownURL(cursor.getString(2))
                .setFilePath(cursor.getString(3))
                .setPartPath(cursor.getString(4))
                .setName(cursor.getString(5))
                .setSize(cursor.getLong(6))
                .setPartSize(cursor.getString(7))
                .setUpDir(cursor.getString(8))
                .setPartIndex(cursor.getInt(9))
                .setStatus(cursor.getString(10))
                .setId(cursor.getString(11))
                .setDate(SimpleDateFormat("MM月dd日HH时").format(Date(cursor.getLong(12))))
                .build()
            result.add(task)
        }
        return result
    }


    fun add(task: Task): Boolean {
        val values = ContentValues()
        values.put("is_down", if (task.isDown) 1 else 0)
        values.put("is_part", if (task.isPart) 1 else 0)
        values.put("down_url", task.downURL)
        values.put("file_path", task.filePath)
        values.put("part_path", task.partPath)
        values.put("name", task.name)
        values.put("size", task.size)
        values.put("part_size", task.partSize)
        values.put("up_dir", task.upDir)
        values.put("part_index", task.partIndex)
        values.put("status", enumToString(task.status))
        values.put("id", task.id)
        values.put("date", System.currentTimeMillis())
        return writableDatabase.insert("tasks", null, values) == 1L


    }

    fun updateStatus(id: String, isDown: Boolean, status: Task.Status): Boolean {
        val values = ContentValues()
        values.put("status", enumToString(status))
        return writableDatabase.update(
            "tasks",
            values,
            "id = ? and is_down = ?",
            arrayOf(id, if (isDown) "1" else "0")
        ) != 0
    }

    fun updatePartIndex(id: String, index: Int): Boolean {
        val values = ContentValues()
        values.put("part_index", index)
        return writableDatabase.update("tasks", values, "id = ?", arrayOf(id)) != 0
    }

    fun updatePartPath(id: String, partPath: String): Boolean {
        val values = ContentValues()
        values.put("part_path", partPath)
        //update方法的返回值是更改的行的数量
        return writableDatabase.update("tasks", values, "id = ?", arrayOf(id)) != 0
    }

    fun updatePartSize(id: String, partSize: String): Boolean {
        val values = ContentValues()
        values.put("part_size", partSize)
        //update方法的返回值是更改的行的数量
        return writableDatabase.update("tasks", values, "id = ?", arrayOf(id)) != 0
    }

    fun delete(id: String) = writableDatabase.delete("tasks", "id = ?", arrayOf(id)) == 1


}


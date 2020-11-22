package com.colorlesscat.whitezou.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.colorlesscat.whitezou.BuildConfig
import com.colorlesscat.whitezou.R
import com.colorlesscat.whitezou.functions.Task
import com.colorlesscat.whitezou.functions.TaskManager
import kotlinx.android.synthetic.main.item_task.view.*
import java.io.File
import kotlin.concurrent.thread

class TaskListAdapter() :
    RecyclerView.Adapter<TaskListAdapter.ViewHolder>() {
    var isTouching = false

    class ViewHolder(
        val layout: View,
        var icon: AppCompatImageView = layout.item_task_icon,
        var type: AppCompatImageView = layout.item_task_type,
        var name: AppCompatTextView = layout.item_task_name,
        var size: AppCompatTextView = layout.item_task_size,
        var date: AppCompatTextView = layout.item_task_date,
        var speed: AppCompatTextView = layout.item_task_speed,
        var btn: AppCompatImageView = layout.item_task_btn,
        var progress: ProgressBar = layout.item_task_pb
    ) : RecyclerView.ViewHolder(layout)

    val sizeToString = { size: Float ->
        when {
            size < 1024 -> "${String.format("%.1f", size)}B"
            size < 1024 * 1024 -> "${String.format("%.1f", size / 1024)}KB"
            size < 1024 * 1024 * 1024 -> "${String.format("%.1f", size / 1024 / 1024)}MB"
            else -> "${String.format("%.1f", size / 1024 / 2014 / 1024)}GB"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        )


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //初始化适配器之前记得先调用TaskManager的init
        val item = TaskManager.taskList[position]
        holder.name.text = item.name
        holder.size.text =
            if (item.isPart && !item.isDown) "${item.partIndex + 1}/${item.partCount}"
            else "${sizeToString(item.progress.toFloat())} / ${sizeToString(item.size.toFloat())}"
        holder.date.text = item.date
        holder.icon.setImageResource(FileTypeHelper.getImgIDByType(item.name.split(".").last()))
        holder.type.setImageResource(if (item.isDown) R.mipmap.download_item else R.mipmap.upload)
        holder.progress.progress = (item.progressScale * 100F).toInt()
        holder.speed.text =
            if (item.status == Task.Status.READY || item.status == Task.Status.ALREADY) "准备中"
            else if (item.status == Task.Status.DOING) sizeToString(item.speed.toFloat()) + " / S"
            else if (item.status == Task.Status.PAUSE) "0B / S"
            else if (item.isDown) "下载完成" else "上传完成"
        holder.btn.setImageResource(if (item.status == Task.Status.READY || item.status == Task.Status.ALREADY || item.status == Task.Status.DOING) R.mipmap.pause else R.mipmap.start)
        holder.btn.setOnClickListener() {
            if (item.status != Task.Status.PAUSE) {
                item.stop()
                holder.btn.setImageResource(R.mipmap.start)
            } else {
                thread {
                    item.start()
                }
                holder.btn.setImageResource(R.mipmap.pause)
            }
        }
        holder.layout.setOnTouchListener() { v, m ->
            if (m.action == MotionEvent.ACTION_DOWN)
                isTouching = true
            if (m.action == MotionEvent.ACTION_UP)
                isTouching = false
            false
        }

        holder.layout.setOnLongClickListener {
            showDeleteDialog(it.context, item)
            false
        }
        if (item.status == Task.Status.END) {
            holder.btn.visibility = View.INVISIBLE
            holder.size.text = sizeToString(item.size.toFloat())
            holder.progress.visibility = View.INVISIBLE
            holder.layout.setOnClickListener {

                val intent = Intent(Intent.ACTION_VIEW)
                StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
//                var uri = FileProvider.getUriForFile(
//                    holder.btn.context,
//                    BuildConfig.APPLICATION_ID + ".fileprovider",
//                    item.file
//                )
                var uri = Uri.fromFile(item.file)
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.setDataAndType(uri, "*/*")
                holder.btn.context.startActivity(intent)
            }
        } else {
            holder.btn.visibility = View.VISIBLE
            holder.progress.visibility = View.VISIBLE
        }


    }

    private fun showDeleteDialog(context: Context, task: Task) {
        AlertDialog.Builder(context)
            .setMessage("真的要删除这个任务吗？(同时删除文件)")
            .setPositiveButton("是") { _, _ ->
                task.delete()
                TaskManager.taskList.remove(task)
                if (task.isDown) {
                    if (task.isPart) {
                        if (task.file.exists())
                            task.file.delete()

                    } else {
                        task.file.delete()
                    }
                }
                val partDir = File(task.partPath)
                if (partDir.exists()) {
                    for (file in partDir.listFiles()) {
                        file.delete()
                    }
                }
                Toast.makeText(context, "删除完成！", Toast.LENGTH_SHORT).show()
                notifyDataSetChanged()
            }.setNegativeButton("不要") { a, _ ->
                a.dismiss()
            }.show()
    }

    override fun getItemCount(): Int = TaskManager.taskList.size


}
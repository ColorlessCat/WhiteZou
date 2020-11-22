package com.colorlesscat.whitezou.functions

import java.io.File
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.random.Random

/**
@Author ColorlessCat
@Date 2020-08-23 16:18
@Description 管理所有上传或者下载任务
 */

object TaskManager {
    var taskList = emptyList<Task>().toMutableList()
    var doingCount = 0//进行中的任务数量
    var db = DatabaseHelper(ObjectSaver.appContext)
    var net = NetFunctionImpl()

    /**
     *　加载已存在的任务列表
     */
    fun init() {
        taskList = db.query().toMutableList()
        for (t in taskList) {
            if (t.status != Task.Status.END && t.status != Task.Status.PAUSE) {
                thread {
                    t.start()
                }
                doingCount++
            }
        }
    }

    fun addDownloadTask(lan: LanzousFile) {
        var isPart = false
        var downURL = ""
        var size = -1L
        var partPath = ""
        var partSize = ""
        var filePath = ObjectSaver.appPath + "/" + lan.name.replace("_p4rt","")
        var partList: List<LanzousFile>
        //lan里面是没有downURL的，要手动获取
        thread {
            if (lan.isFile) {
                downURL = net.getFileData(lan.id, true)[0]
                size = net.getFileSize(downURL)

            } else {
                isPart = true
                partList = net.getFileList(lan.id)
                var ps = -1L
                for (l in partList) {
                    l.downURL = net.getFileData(l.id, true)[0]
                    downURL += l.downURL + ","
                    ps = net.getFileSize(l.downURL)
                    size += ps
                    partPath += "${ObjectSaver.appPath}/.${lan.name}_dp/${l.name},"
                    partSize += "$ps,"
                }
                downURL = downURL.substring(0, downURL.lastIndex)
                partPath = partPath.substring(0, partPath.lastIndex)
                partSize = partSize.substring(0, partSize.lastIndex)
            }
            val task = Task.Builder()
                .isDown(true)
                .setName(if (isPart) lan.name.substring(0, lan.name.lastIndex - 4) else lan.name)
                .setDate("今天")
                .isPart(isPart)
                .setSize(size)
                .setFilePath(filePath)
                .setPartPath(partPath)
                .setPartSize(partSize)
                .setDownURL(downURL)
                .setId(lan.id)
                .build()
            db.add(task)
            taskList.add(task)
            task.start()
            doingCount++
        }
    }

    fun addUploadTask(file: File, dir: String) {
        //TODO　要在这里创建上传分片需要的文件夹
        var isPart = file.length() > 100 * 1024 * 1024
        thread {
            var id = if (isPart) net.addFolder(
                file.name.replace('-', '_').replace(' ', '_') + "_p4rt",
                "",
                dir
            ) else Random(System.currentTimeMillis()).nextInt().toString()
            if (id == "") return@thread
            var size = file.length()
            //在文件分割前是不知道　partSize和partPath的 所以这两个值会在任务开始后获取
            val task = Task.Builder()
                .setId(id)
                .setFilePath(file.absolutePath)
                .setSize(file.length())
                .setName(file.name)
                .setDate("今天")
                .isPart(isPart)
                .isDown(false)
                .setUpDir(dir)
                .build()
            db.add(task)
            taskList.add(task)

            task.start()

            doingCount++
        }
    }
}
package com.colorlesscat.whitezou.functions

import android.Manifest
import android.app.Application
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

/**
 * @Date 2020-09-04
 * @Author ColorlessCat
 * @Description　代表一个上传或者下载任务
 */
class Task {
    val db = DatabaseHelper(ObjectSaver.appContext)
    private val net = ObjectSaver.net

    var isDown = true
    var isPart = false
    var status = Status.READY
    var downURL = ""
    var filePath = ""
    var size = 0L
    var partSize = ""
    var upDir = "-1"
    var partIndex = 0
    var partPath = ""
    var date: String = "99月99日"//仅用于显示
    var name = ">_<"
    var id = "4396"

    lateinit var file: File

    var oldStatus = Status.READY
    var partCount = -1//仅用作上传分片时显示进度
    var speed = 0
    var time = 0L
    private var oldTime = 0L
    private var oldProgress = 0L
    var progressScale = 0F
    var progress = 0L
    var retryCount = 0
    private var callback = { bytes: Int ->
        progress += bytes
        if (SystemClock.elapsedRealtime() - oldTime >= 1000) {
            speed = (progress - oldProgress).toInt()
            oldTime = SystemClock.elapsedRealtime()
            oldProgress = progress
            if (!isDown && isPart) {
                //分片上传的进度无法精确显示 所以只显示百分比(实现起来很麻烦)
                progressScale = (partIndex + 1) / partCount.toFloat()
            } else
                progressScale = progress / size.toFloat()
        }
        customCallback(progress, progressScale, speed)
        callback1(bytes)

    }
    var customCallback: (progress: Long, progressScale: Float, speed: Int) -> Unit = { _, _, _ -> }
    var callback1: (it: Int) -> Unit = { it: Int ->
        if (it != -1 && status != Status.DOING)
            updateStatus(Status.DOING)
    }//除单文件下载以外　其他三种情况都执行这个逻辑

    companion object {
        val client = OkHttpClient.Builder()
            .callTimeout(1, TimeUnit.HOURS)
            .connectTimeout(1, TimeUnit.HOURS)
            .readTimeout(1, TimeUnit.HOURS)
            .writeTimeout(1, TimeUnit.HOURS)
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
    }

    class Builder(private val task: Task = Task()) {
        fun isDown(isDown: Boolean): Builder {
            task.isDown = isDown
            return this
        }

        fun isPart(isDown: Boolean): Builder {
            task.isPart = isDown
            return this
        }

        fun setDownURL(downURL: String): Builder {
            task.downURL = downURL
            return this
        }

        fun setFilePath(filePath: String): Builder {
            task.filePath = filePath
            task.file = File(filePath)
            return this
        }

        fun setPartPath(partPath: String): Builder {
            task.partPath = partPath
            return this
        }

        fun setName(name: String): Builder {
            task.name = name
            return this
        }

        fun setSize(size: Long): Builder {
            task.size = size
            return this
        }

        fun setPartSize(partSize: String): Builder {
            task.partSize = partSize
            return this
        }

        fun setPartIndex(partIndex: Int): Builder {
            task.partIndex = partIndex
            return this
        }

        fun setId(id: String): Builder {
            task.id = id
            return this
        }

        fun setStatus(status: String): Builder {
            task.status = when (status) {
                "ready" -> Status.READY
                "already" -> Status.ALREADY
                "doing" -> Status.DOING
                "pause" -> Status.PAUSE
                "end" -> Status.END
                else -> Status.READY
            }
            return this
        }

        fun setDate(date: String): Builder {
            task.date = date
            return this
        }

        fun setUpDir(upDir: String): Builder {
            task.upDir = upDir
            return this
        }

        fun setCallback(callback: (progress: Long, progressScale: Float, speed: Int) -> Unit): Builder {
            task.customCallback = callback
            return this
        }

        fun build() = task
    }

    enum class Status {
        READY, ALREADY, DOING, PAUSE, END
    }

    fun start() {
        retryCount++
        status = oldStatus
        try {
            if (isDown)
                if (isPart)
                    startDownloadPart()
                else
                    startDownload()
            else
                if (isPart)
                    startUploadPart()
                else
                    startUpload()
        } catch (e: Exception) {
            if (retryCount >= 3) return
            start()
        }
    }

    fun stop() {
        oldStatus = status
        status = Status.PAUSE
    }

    fun delete() {
        updateStatus(Status.PAUSE)
        db.delete(id)
        if (isPart && !isDown)
            net.deleteFile(id, false)

    }

    private fun startUploadPart() {
        val partDir = File(ObjectSaver.appPath, ".${file.name}_up")
        var partFile: Array<File>?
        if (status == Status.READY) {
            partFile = FileSplitTool.split(file, partDir, 95 * 1024 * 1024, -1) { _, _, _ -> }
            if (partFile == null || partFile.isEmpty()) {
                //分割失败　暂时不做处理
                return
            } else {
                val paths = StringBuilder()
                val sizes = StringBuilder()
                for (f in partFile) {
                    paths.append(f.absolutePath + ",")
                    sizes.append(f.length().toString() + ",")
                }
                paths.deleteCharAt(paths.lastIndex)//去掉最后一个逗号
                sizes.deleteCharAt(sizes.lastIndex)
                db.updatePartPath(id, paths.toString())
                db.updatePartSize(id, sizes.toString())
                updateStatus(Status.ALREADY)
            }
        }
        if (status == Status.PAUSE) return
        if (status == Status.ALREADY || status == Status.DOING) {
            val task = db.query(id)[0]
            val paths = task.partPath.split(",")
            partIndex = task.partIndex
            partCount = paths.size
            partFile = Array(paths.size) { File(paths[it]) }
            for (i in partIndex until partFile.size) {
                if (upload(partFile[i])) {
                    partIndex = i + 1
                    db.updatePartIndex(id, partIndex)
                } else {
                    Log.e(javaClass.name, "upload failed name=$name,partIndex=$i")
                    return
                }

            }
            //验证是否已经全部上传:直接验证文件的数量就行了　因为上传不存在文件不完整的情况
            val lanFiles = net.getFileList(id)
            var isFinish = lanFiles.size == partFile.size
            /**
            直接获取得到的应该是时间倒序的列表

            for (i in lanFiles.lastIndex downTo 0){
            if (lanFiles[i].name!=partFile[lanFiles.lastIndex-i].name)
            isFinish=false
            }
             */
            if (isFinish) {
                updateStatus(Status.END)
            } else {
                Log.e(javaClass.name, "check failed name=$name")
            }


        }
    }

    private fun startDownloadPart() {
        val partDir = File(ObjectSaver.appPath, ".${file.name}_dp")
        var partFile: Array<File>?
        if (status == Status.READY) {
            if (partDir.mkdir() || partDir.exists()) {
                updateStatus(Status.ALREADY)
            } else
                return
        }
        if (status == Status.ALREADY || status == Status.DOING) {
            val task = db.query(id)[0]
            val paths = task.partPath.split(",")
            var downFile: File
            val urls = downURL.split(",")
            val sizeArray = partSize.split(",")
            var url = ""
            partIndex = task.partIndex
            partFile = Array(paths.size) { File(paths[it]) }

            //计算progress的值
            for (f in partFile)
                progress += if (f.exists()) f.length() else 0
            for (i in partIndex until partFile.size) {
                downFile = partFile[i]
                //可能由于某些未知原因，文件下载异常，此时直接删掉文件
                if (downFile.length() > sizeArray[i].toLong()) {
                    downFile.delete()
                }
                try {
                    downFile.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                    return
                }
                url = net.getDownloadLink(urls[i])
                download(url, downFile, sizeArray[i].toLong())
                if (status == Status.PAUSE) return
                //callback1 = {}
                //一个文件下载完以后　验证下载的文件的大小　正确就更新partIndex
                if (downFile.length() == sizeArray[i].toLong())
                    db.updatePartIndex(id, i + 1)
                else {
                    Log.e(javaClass.name, "download failed name=$name,partIndex=$i")
                    return
                }
            }
            var isFinish = true
            //最后再验证一遍文件大小　所有文件大小一致就认为下载完成 开始合并
            for (i in partFile.indices) {
                if (partFile[i].length() != sizeArray[i].toLong())
                    isFinish = false
            }
            if (isFinish) {
                if (FileSplitTool.merge(partFile[0], file) { _, _ -> }) {
                    updateStatus(Status.END)

                } else {
                    Log.e(javaClass.name, "merge failed: name=$name")
                }
            } else {
                Log.e(javaClass.name, "download final check failed: name=$name")
            }

        }


    }

    private fun startDownload() {
        //状态为ready意味着还没有做任何操作
        if (status == Status.READY) {
            try {
                file.createNewFile()
            } catch (e: Exception) {
                Log.e(this::javaClass.name, e.toString())
                return
            }
            updateStatus(Status.ALREADY)

        }
        val url = net.getDownloadLink(downURL)
        callback1 = { it: Int ->
            if (it == -1) {
                //读到-1两种结果　下完或者没下完
                if (size == file.length()) {
                    updateStatus(Status.END)
                }
            } else if (status == Status.ALREADY) {
                //如果读到数据了　更改状态为下载中
                progress = file.length()
                updateStatus(Status.DOING)
            }
        }
        if (status == Status.ALREADY || status == Status.DOING) {
            download(url, file, size)
        }


    }


    private fun download(
        url: String,
        file: File,
        size: Long = -1
    ) {
        val from = if (file.exists()) file.length() else 0
        val req = Request.Builder()
            .addHeader("Range", "bytes=$from-$size")
            .url(url)
            .get()
            .build()
        val input = client.newCall(req).execute().body!!.byteStream()
        val output = FileOutputStream(file, true)
        var len = 0
        val bytes = ByteArray(4 * 1024)
        while (true) {
            if (status == Status.PAUSE) {
                input.close()
                output.close()
                updateStatus(status)

                return
            }
            len = input.read(bytes)
            callback(len)
            if (len == -1) break
            output.write(bytes, 0, len)
        }

    }


    private fun startUpload() {
        if (upload(file))
            updateStatus(Status.END)
        else
            Log.e(javaClass.name, "upload failed: $name")
    }

    fun updateStatus(status: Status) {
        this.status = status
        db.updateStatus(id, isDown, status)
    }

    //最大文件夹名字长度
    private fun upload(file: File): Boolean {

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("task", "1")
            .addFormDataPart("ve", "2")
            .addFormDataPart("folder_id_bb_n", if (isPart) id else upDir)
            .addFormDataPart(
                "upload_file",
                file.name,
                RequestBodyWithProgress(file.asRequestBody(), callback)
            )
            .build()
        val request = Request.Builder()
            .url("https://pc.woozooo.com/fileup.php")
            .header("cookie", ObjectSaver.cookie)
            .post(body)
            .build()
        val res = client.newCall(request).execute().body!!.string()
        return res[res.indexOf("zt") + 4] == '1'
    }


}
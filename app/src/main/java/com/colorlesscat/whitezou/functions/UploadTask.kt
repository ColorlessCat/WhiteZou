package com.colorlesscat.whitezou.functions

import android.os.SystemClock
import okhttp3.*
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.RequestBody.Companion.asRequestBody

/**
@Author ColorlessCat
@Date 2020-08-04 22:52
@Description 代表一次上传任务 breakCount是记录断点续传时，从第几个文件开始断开的。
从0开始计数，-1为未中断。
 */

class UploadTask(val file: File, val fol_id:String="-1") {
    var progress:Long=0
    var size:Long=0
    var speed=0
    companion object {
        private val client = OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.DAYS)
            .build()
    }
    var netFunction= NetFunctionImpl()
    fun start(callback: (writeBytes: Int) -> Unit) {
        size=file.length()
        //小于100M不分割直接上传
        if (file.length() < 100 * 1024 * 1024) {
            upload(file, fol_id,callback)
            return
        }
        val files = FileSplitTool.split(file, File(""),95 * 1024 * 1024, -1) { _, b, _ -> print(b) }
        if (files == null) {
            callback(-1)
            return
        }
        val id=netFunction.addFolder(file.name+"_p4rts","地上本没有路，走的人多了，也便成了路。-迅哥",fol_id)
        if(id=="") return
        for (file in files) {
            Thread.sleep(10000)//上传完一个文件后休眠十秒，以免被监测到上传流量过大
            upload(file, id,callback)
        }
    }

    fun upload(file: File, id:String,callback: (writeBytes: Int) -> Unit): Boolean {

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("task", "1")
            .addFormDataPart("folder_id_bb_n",id)
            .addFormDataPart(
                "upload_file",
                file.name,
                    RequestBodyWithProgress(file.asRequestBody(),callback)
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
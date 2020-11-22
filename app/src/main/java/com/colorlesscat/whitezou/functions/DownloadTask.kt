package com.colorlesscat.whitezou.functions
import android.os.SystemClock
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
@Author ColorlessCat
@Date 2020-08-24 18:47
@Description  一次文件下载任务
 */
open class DownloadTask(
    val cloudFile: LanzousFile,
    var filePath: String,
    val callback: (Long) -> Unit,
    var size: Long = -1,//下载开始以后才能从请求头里读取到
    var status: Status = Status.READY,
    var name: String = cloudFile.name,
    var id:String=cloudFile.id,
    var progress: Long = 0,
    var speed: Int = 0
) {

    //文件的状态 未开始，下载中，完成
    enum class Status {
        READY, DOING, DONE
    }

    companion object {
        private var client = OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.DAYS)
            .build()
        /*client = client.newBuilder().addInterceptor(object : Interceptor {
               override fun intercept(chain: Interceptor.Chain): Response {
                   val response = chain.proceed(chain.request())
                   //size=response.headers.get("Content-Length")
                   size=response.body!!.contentLength()
                   response.newBuilder().body(ResponseBodyWithProgress(response.body!!, callback))
                   return response
               }
           }).build()*/
    }

    fun start() {
            //文件从哪开始下载
            var from = File(filePath).length()
            size=getFileSize()
            println("size is "+size)
            download(from, filePath, callback)
            return

    }

    private fun download(
        from: Long, path: String,
        callback: (Long) -> Unit
    ) {
        var req = Request.Builder()
            .url(cloudFile.downURL)
            .get()
        val output = FileOutputStream(path, true)
        val bytes = ByteArray(4 * 1024)
        //如果是断点续传 文件的大小是一定有值的
        if (from != 0L) {
            req = req.addHeader("Range", "bytes=$from-$size")
        }
        val input = client.newCall(req.build()).execute().body!!.byteStream()
        var len: Int
        while (true) {
            len = input.read(bytes)
            callback(len.toLong())
            if (len == -1) {

                break
            }
            output.write(bytes,0,len)
        }
        input.close()
        output.close()
    }

    fun getFileSize(): Long {
        var req = Request.Builder()
            .url(cloudFile.downURL)
            .get()
        return client.newCall(req.build()).execute().body!!.contentLength()
    }
}
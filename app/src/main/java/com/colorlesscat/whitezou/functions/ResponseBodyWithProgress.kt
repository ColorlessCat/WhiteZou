package com.colorlesscat.whitezou.functions

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.buffer

/**
@Author ColorlessCat
@Date 2020-08-25 10:57
@Description 这个类开始是为了做下载进度的监听写的 但是其实没必要 直接body.byteStream()用输入流读取顺便就把进度监听了
*/

class ResponseBodyWithProgress(val body: ResponseBody, val callback: (readBytes: Long) -> Unit) :
    ResponseBody() {
    var readBytes: Long = 0
    override fun contentLength(): Long = body.contentLength()

    override fun contentType(): MediaType? = body.contentType()

    override fun source(): BufferedSource = object : ForwardingSource(body.source()) {
        override fun read(sink: Buffer, byteCount: Long): Long {
            val length = super.read(sink, byteCount)
            readBytes += length
            callback(readBytes)
            return length
        }
    }.buffer()


}
package com.colorlesscat.whitezou.functions

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import okio.`-DeprecatedOkio`.buffer

class RequestBodyWithProgress(private val body:RequestBody,private val callback:(writtenBytes:Int)->Unit): RequestBody() {
    var writtenBytes=0L
    override fun contentType(): MediaType? =body.contentType()
    override fun contentLength(): Long =body.contentLength()
    override fun writeTo(sink: BufferedSink) {
        val newSink=object:ForwardingSink(sink){
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                callback(byteCount.toInt())
            }
        }.buffer()
        body.writeTo(newSink)
        newSink.flush()
    }
}
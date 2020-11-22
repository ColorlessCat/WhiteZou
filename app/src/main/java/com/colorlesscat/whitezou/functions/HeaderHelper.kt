package com.colorlesscat.whitezou.functions

import java.io.InputStream

/**
 * 读取文件头信息的帮助类
 */
class HeaderHelper {
    companion object {
        val byteArrayToInt =
            { ba: ByteArray -> (ba[0].toInt() shl 24) or (ba[1].toInt() shl 16) or (ba[2].toInt() shl 8) or (ba[3].toInt() and 0xff) }

        private fun read(start: Long = 0, len: Int, inputStream: InputStream): ByteArray {
            val byte = ByteArray(len)
            inputStream.mark(inputStream.available())
            inputStream.skip(start)
            inputStream.read(byte)
            inputStream.reset()
            return byte
        }

        fun readID(inputStream: InputStream): Int = byteArrayToInt(read(0, 4, inputStream))
        fun readNextID(inputStream: InputStream): Int = byteArrayToInt(read(4, 4, inputStream))
        fun readFirstID(inputStream: InputStream): Int = byteArrayToInt(read(8, 4, inputStream))
        fun readFileCount(inputStream: InputStream): Int = byteArrayToInt(read(12, 4, inputStream))
        fun readFlag(inputStream: InputStream): String = String(read(16, 32, inputStream))

        fun readMD5(inputStream: InputStream): String = String(read(48, 32, inputStream))
        fun readAllCount(inputStream: InputStream): Int = byteArrayToInt(read(80, 4, inputStream))
        fun readAllID(inputStream: InputStream, count: Int): IntArray {
            val byte = read(80, 4 * count, inputStream)
            val result = IntArray(count)
            for (i in result.indices)
                result[i] = byteArrayToInt(byte.copyOfRange(i * 4, (i + 1) * 4))
            return result
        }


    }
}
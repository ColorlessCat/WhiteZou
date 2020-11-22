package com.colorlesscat.whitezou.functions

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import kotlin.random.Random

class FileSplitTool {
    companion object {
        const val flag = "Anna is the only god for me.ANNA"
        /**
         * @param file 被分割的文件
         * @param size 分割后的大小
         * @param dir 分割后的文件放在哪个文件夹里
         * @param count 分割成多少片 size和count只能传一个
         * @param callback 分割过程中的回调 step是当前进行到第几步了 0:准备 1:写入 2:完成 count是分割到第几个文件 size是当前文件的写入进度
         * 思路：在分割后的文件的每一个分片的头部，写入一些信息：
         * 1.ID:4Byte ，一个Int型随机数，文件的标识
         * 2.nextID:4Byte，用于定位下一个分片，最后一个分片的值固定为0x00000000
         * 3.firstID:4Byte 片首的ID
         * 4.fileCount:4Byte 标识当前文件是第几个分片,从0x0开始
         * 5.flag:Anna is the only god for me.ANNA 32Byte用于标识一个文件是否是该程序生成的分片
         * 在片首的头部，额外写入一些信息：
         * 1.md5:32Byte 源文件的md5值得16进制字符串，用于判断是否还原成功
         * 2.allCount:4Byte，Int类型，总分片数量
         * 3.list:count*4Byte 按顺序排列的，所有ID的列表
         *
         */

        fun split(
            file: File,
            dir:File,
            size: Int = -1,
            count: Int = -1,
            callback: (step: Int, count: Int, size: Int) -> Unit
        ):Array<File>? {

            callback(0, -1, -1)
            var size = size
            var count = count
            if (size != -1 && count != -1) return null
            val input = file.inputStream()
            if (size != -1) count = (file.length() / size).toInt() + 1//如果给了大小 就给个数赋值
            else if (count != -1) size = (file.length() / count).toInt()//给了个数 就给大小赋值
            val result=Array(count){File("")}
            val md5 = computeFileMd5(file)
            val headers = Array(count) { FileHeader(-1, 0x00000000, -1, -1) }
            val names = Array(count) { "" }
            val ranList = ArrayList<Int>()
            for (i in 0 until count) {
                names[i] = "${file.name}_${i + 1}.mp3"
                result[i]=File(dir,names[i])
                headers[i].count = i
                headers[i].id = generateRandomWithoutSame(ranList)
                headers[i].firstID = headers[0].id
                if (i != 0)
                    headers[i - 1].nextID = headers[i].id
                else {
                    headers[0].md5 = md5
                    headers[0].allCount = count
                    headers[0].allID = IntArray(count)
                }
                headers[0].allID[i] = headers[i].id
            }
            var outputStream: FileOutputStream
            var writeCount = 0 //已经写了多少个字节
            var singleCount = 0//单个文件已经写了几个字节
            //开始写入文件
            val byte = ByteArray(4 * 1024)
            var len = 0
            var off = 0
            var lastSize = file.length() - (count - 1) * size//最后一个文件的大小 如果规定文件大小的话 最后一个文件的大小会比其他文件小
            if (!dir.mkdir()&&!dir.exists()) return null
            for (i in 0 until count) {
                outputStream = result[i].outputStream()
                outputStream.write(headers[i].toByteArray())
                outputStream.flush()
                singleCount = 0
                if (off != 0) {
                    outputStream.write(byte, off, byte.size - len)
                    off = 0
                }
                while (true) {
                    //读多少写多少
                    len = input.read(byte)
                    if (len == -1) return result
                    outputStream.write(byte, 0, len)
                    outputStream.flush()
                    writeCount += len
                    singleCount += len
                    callback(1, i, singleCount)
                    if (singleCount >= size) {
                        off = singleCount - size
                        break
                    }

                }

            }
            return null
        }

        /**
         * @param file 任意一个分片
         * @param outFile 合并完成后的文件
         * @param callback 回调 step : -1 选择的文件不是分片文件 -2 片首丢失 -3 分片缺失 0 准备中 1 合并中 2 验证MD5*/
        fun merge(file: File, outFile: File, callback: (step: Int, progress: Long) -> Unit) :Boolean{
            val fileList = file.parentFile.listFiles()
            val pieceInputList = ArrayList<BufferedInputStream>()
            val idList = ArrayList<Int>()
            val inputStream = BufferedInputStream(file.inputStream())
            val outputStream = outFile.outputStream()
            val findIndexByValue =
                fun(list: ArrayList<Int>, value: Int): Int {
                    for (i in list.indices)
                        if (list[i] == value)
                            return i
                    return -1
                }
            val firstID = HeaderHelper.readFirstID(inputStream)
            if (HeaderHelper.readFlag(inputStream) != flag) {
                callback(-1, -1)
                return false
            }

            idList.add(HeaderHelper.readID(inputStream))
            pieceInputList.add(inputStream)
            var input: BufferedInputStream
            for (f in fileList) {
                if (f.isDirectory || f.name == file.name) continue
                input = BufferedInputStream(f.inputStream())
                if (HeaderHelper.readFlag(input) != flag)
                    continue
                pieceInputList.add(input)
                idList.add(HeaderHelper.readID(input))
            }

            //输入流和ID的集合 在顺序上是一致的 搜索出片首的ID的索引就能进而找出片首的流
            var index = findIndexByValue(idList, firstID)
            if (index == -1) {
                callback(-2, -1)
                return false
            }
            val firstStream = pieceInputList[index]
            val md5 = HeaderHelper.readMD5(firstStream)
            val allCount = HeaderHelper.readAllCount(firstStream)
            val allID = HeaderHelper.readAllID(firstStream, allCount)
            var nextID = firstID
            val firstHeaderLength = 84 + allCount * 4
            val bytes = ByteArray(4 * 1024)
            var len = -1
            var writeBytes = 0L
            var nextIndex = 0
            while (nextID != 0) {
                nextIndex = findIndexByValue(idList, nextID)
                if (nextIndex == -1) {
                    callback(-3, -1)
                    return false
                }
                input = pieceInputList[nextIndex]
                input.mark(Int.MAX_VALUE)
                input.skip(if (nextID == firstID) firstHeaderLength.toLong() else 48)
                while (true) {
                    len = input.read(bytes)
                    if (len == -1) break
                    outputStream.write(bytes, 0, len)
                    outputStream.flush()
                    writeBytes += len
                    callback(1, writeBytes)
                }
                input.reset()
                nextID = HeaderHelper.readNextID(input)
                input.close()
            }
            //写完了 验证一下MD5
            val outMD5 = computeFileMd5(outFile)
            return md5==outMD5
            //println("源文件的MD5为：$md5\n合并后文件的MD5为：$outMD5")

        }

        /*
            生成不会重复的随机数
         */
        private fun generateRandomWithoutSame(list: ArrayList<Int>): Int {
            val num = Random(System.currentTimeMillis()).nextInt()
            Thread.sleep(1)
            return if (list.contains(num)) generateRandomWithoutSame(list)
            else {
                list.add(num)
                num
            }
        }

        private fun computeFileMd5(file: File): String {
            val md5Str = StringBuilder()
            val hex =
                arrayOf(
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
                )
            val md = MessageDigest.getInstance("MD5")
            val input = FileInputStream(file)
            //当读到文件末尾的时候，有可能字节数不足以装满byte数组，多出来的0也会被写入,会影响计算结果
            val byte = ByteArray(4 * 1024)
            var len: Int
            //是否可以刚好装满每一个byte数组
            val divisible = (file.length() % byte.size).toInt() == 0
            var byteCount = 0
            val byteQuantity = (file.length() / byte.size).toInt()
            while (input.read(byte) != -1) {
                len = byte.size
                if (!divisible && byteCount == byteQuantity) {
                    //如果装不满并且已经是最后一个数组
                    len = (file.length() % byte.size).toInt()
                }
                md.update(byte, 0, len)
                byteCount++
            }
            val md5Byte = md.digest()
            for (b in md5Byte) {
                md5Str.append(hex[b.toInt() ushr 4 and 0xf])
                md5Str.append(hex[b.toInt() and 0xf])
            }
            return md5Str.toString()
        }


    }
}


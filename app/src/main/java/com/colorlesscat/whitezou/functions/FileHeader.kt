package com.colorlesscat.whitezou.functions

class FileHeader(var id:Int,var nextID:Int,var firstID:Int,var count:Int,var md5:String="",var allCount:Int=-1,var allID:IntArray= IntArray(0)){
    fun toByteArray():ByteArray{
        val isFirst=count==0
        val result=ByteArray(if (!isFirst) 48 else 84+allID.size*4)
        for(i in result.indices){
            result[i]=when(i/4){
                0-> (id ushr 32-(i%4+1)*8 and 0xff).toByte()
                1-> (nextID ushr 32-(i%4+1)*8 and 0xff).toByte()
                2-> (firstID ushr 32-(i%4+1)*8 and 0xff).toByte()
                3-> (count ushr 32-(i%4+1)*8 and 0xff).toByte()
                4,5,6,7,8,9,10,11-> FileSplitTool.flag.toByteArray()[i-16]//从第16个字节开始，存储flag
                12,13,14,15,16,17,18,19->md5.toByteArray()[i-48]//从第49个字节开始，存储md5
                20->(allCount ushr 32-(i%4+1)*8 and 0xff).toByte()
                else-> (allID[(i-84)/4] ushr 32-(i%4+1)*8 and 0xff).toByte() //前面都是固定长度，认为所有else的情况都是allID的数据,从第85个字节开始
            }
        }
        return result
    }
}
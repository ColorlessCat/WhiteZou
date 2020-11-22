package com.colorlesscat.whitezou.functions

import com.colorlesscat.whitezou.functions.LanzousFile

interface NetFunction {
    fun login(usr:String,pass:String):Boolean
    fun getFileList(id:String="-1"):List<LanzousFile>
    fun getFileData(id:String,isFile:Boolean):Array<String>
    fun deleteFile(id:String,isFile:Boolean):Boolean
    fun addFolder(name: String, description: String, parentId: String = "-1"):String
    fun getDownloadLink(url:String):String
    fun getFileSize(url: String): Long
    fun setDirData(id:String,name:String,des:String):Boolean
    fun removeFile(id:String,dirID:String):Boolean
}
package com.colorlesscat.whitezou.functions

import android.content.Context
import android.os.Environment

/**
@Author ColorlessCat
@Date 2020-08-04 16:27
@Description 存储一些从程序启动开始到程序结束都可能用到的变量 比如登录的Cookie
*/

class ObjectSaver {
    companion object{
        var cookie=""
        val net=NetFunctionImpl()
        var appContext:Context? = null
        var appPath= Environment.getExternalStorageDirectory().absolutePath + "/WhiteZous"
    }
}
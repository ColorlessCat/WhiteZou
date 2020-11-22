package com.colorlesscat.whitezou.views

import com.colorlesscat.whitezou.R

/**
@Author ColorlessCat
@Date 2020-08-22 11:23
@Description 根据文件类型返回对应的图片
 */

class FileTypeHelper {
    companion object {
        fun getImgIDByType(type: String) = when (type) {
            "apk" -> R.mipmap.apk
            "zip", "rar", "7z", "tar", "crx" -> R.mipmap.file_zip
            "exe", "bat" -> R.mipmap.file_exe
            "mp3" -> R.mipmap.file_music
            "lua" -> R.mipmap.file_code
            "doc", "docx" -> R.mipmap.doc
            "ppt", "pptx" -> R.mipmap.ppt
            "xls", "xlsx" -> R.mipmap.xls
            "txt" -> R.mipmap.txt
            "ttf" -> R.mipmap.ttf
            "mp4", "flv", "avi", "swf" -> R.mipmap.file_video
            "deb", "rpm", "rp" -> R.mipmap.installation
            "html" -> R.mipmap.html
            "dll"->R.mipmap.dll
            "jar"->R.mipmap.jar
            "iso"->R.mipmap.iso
            else -> R.mipmap.unknow

        }
    }
}


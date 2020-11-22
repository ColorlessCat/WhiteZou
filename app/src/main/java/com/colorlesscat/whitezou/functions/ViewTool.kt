package com.colorlesscat.whitezou.functions

import android.graphics.Point

/**
 * @Date 2020-10-14
 * @Author ColorlessCat
 * @Description　试图相关的工具
 */
class ViewTool {
    companion object {
        fun getScreenSize(): Point {
            val dm = ObjectSaver.appContext!!.resources.displayMetrics
            return Point(dm.widthPixels, dm.heightPixels)
        }
    }
}
package com.colorlesscat.whitezou.functions

import org.json.JSONObject

/**
@Author ColorlessCat
@Date 2020-08-04 15:35
@Description 数据相关的一些操作
 */

class DataOperation {
    companion object {
        /**
        @author ColorlessCat
        @parameter
        @return
        @Description 蓝奏云的文件列表接口，文件和文件夹是分开的，返回的json的结构稍微有些不同
         */
        fun parseJson2FileList(data: String): ArrayList<LanzousFile> {
            val list = ArrayList<LanzousFile>()
            val jo = JSONObject(data)
            if (jo.getInt("zt") != 1) return list
            val array = jo.getJSONArray("text")
            var jsonObject: JSONObject
            var file: LanzousFile
            var isFile = false
            for (i in 0 until array.length()) {
                jsonObject = array.getJSONObject(i)
                isFile = jsonObject.isNull("fol_id")
                file = LanzousFile(
                    isFile,
                    if (isFile) jsonObject.getString("id") else jsonObject.getString("fol_id"),
                    if (isFile) jsonObject.getString("name_all") else jsonObject.getString("name"),
                    if (isFile) jsonObject.getString("size") else "",
                    if (isFile) jsonObject.getString("time") else "",
                    if (isFile) jsonObject.getString("downs") else ""
                )
                list.add(file)
            }
            return list
        }

        /**
        @author ColorlessCat
        @parameter
        @return 索引0是下载链接 1是密码 2是文件夹描述 无密码则为空字符串
        @Description 从文件信息的json中获取下载页面的链接和密码
         */

        fun parseJson4FileData(data: String): Array<String> {
            val result = Array(3) { "" }
            val jo = JSONObject(data).getJSONObject("info")
            val isFile = jo.isNull("name")
            val isHasPass = jo["onof"] == "1"
            result[0] = if (isFile) "${jo["is_newd"]}/${jo["f_id"]}" else jo["new_url"].toString()
            result[1] = if (isHasPass) jo["pwd"].toString() else ""
            result[2] = if (!isFile) jo["des"].toString() else ""
            return result
        }

        fun parseJson4AddFolder(data: String): String {
            val jo = JSONObject(data)
            if (jo.getString("zt") != "1") return ""
            return jo.getString("text")
        }

        fun parseDownloadLink(data: String): String {
            val jo = JSONObject(data)
            return jo.getString("dom") + "/file/" + jo.getString("url")
        }


    }
}
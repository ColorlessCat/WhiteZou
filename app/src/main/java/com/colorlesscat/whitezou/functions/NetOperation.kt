package com.colorlesscat.whitezou.functions

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit


/**
@Author ColorlessCat
@Date 2020-08-04 01:40
@Description 网络相关的操作
 */
class NetOperation {

    companion object {
        //蓝奏云所有有关账号的操作基本都是这个url
        val url = "https://pc.woozooo.com/doupload.php"
        private val client = OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

        private fun sendPostRequestWithForm(
            url: String,
            parameters: Map<String, String>,
            cookie: String = "",
            referer: String = "https://wwe.lanzous.com"
        ): Response {
            val builder = FormBody.Builder()
            for (p in parameters)
                builder.add(p.key, p.value)
            val req = Request.Builder()
                .url(url)
                .addHeader("Cookie", cookie)
                .addHeader("Referer", referer)
                .post(builder.build())
                .build()
            return client.newCall(req).execute()
        }

        private fun sendGetRequestWithoutPara(url: String): String {
            val req = Request.Builder().url(url).get().build()
            return client.newCall(req).execute().body!!.string()
        }

        /**
        @author ColorlessCat
        @parameter
        @return
        @description 得到phpdisk的值，蓝奏云用这个判定登录状态
         */
        fun getLoginCookie(usr: String, pwd: String): String {
            val url = "https://up.woozooo.com/mlogin.php"
            val para = mapOf(
                "task" to "3",
                "uid" to usr,
                "pwd" to pwd
            )
            val response = sendPostRequestWithForm(url, para, "PHPSESSID=233")
            var cookies = response.headers["Set-Cookie"] ?: return "failed"
            return cookies.substring(0, cookies.indexOf(";") + 1)
        }


        fun getFilesData(
            loginCookie: String,
            isFile: Boolean,
            id: String = "-1",
            pg: Int = 1
        ): String {

            val para = mapOf(
                "task" to if (isFile) "5" else "47",
                "folder_id" to id,
                "pg" to pg.toString()
            )
            return sendPostRequestWithForm(url, para, loginCookie).body!!.string()
        }

        fun addFolder(
            loginCookie: String,
            name: String,
            description: String,
            parentId: String = "-1"
        ): String {

            val para = mapOf(
                "task" to "2",
                "parent_id" to parentId,
                "folder_name" to name,
                "folder_description" to description
            )
          return  sendPostRequestWithForm(url, para, loginCookie).body!!.string()
        }

        fun deleteFile(loginCookie: String, isFile: Boolean, id: String): String {

            val para = mapOf(
                "task" to (if (isFile) "6" else "3"),
                (if (isFile) "file_id" else "folder_id") to id
            )
            return sendPostRequestWithForm(url, para, loginCookie).body!!.string()
        }

        fun getFileData(loginCookie: String, isFile: Boolean, id: String): String {
            val para = mapOf(
                "task" to if (isFile) "22" else "18",
                (if (isFile) "file_id" else "folder_id") to id
            )
            return sendPostRequestWithForm(url, para, loginCookie).body!!.string()
        }

        fun getFileDownloadLink(url: String): String {
            //TODO 无法正常解析带访问密码的链接
            val downSite = sendGetRequestWithoutPara(url)
            val downLinkSiteUrl = "https://wwe.lanzous.com" + downSite.substring(
                downSite.lastIndexOf("src=\"/") + 5,
                downSite.lastIndexOf("frameborder") - 2
            )
            val downLinkSite = sendGetRequestWithoutPara(downLinkSiteUrl)
            //TODO 解析js代码这里　蓝奏云可能经常更改　最好换一种更完美的实现(WebView)
            val sign = Regex("\\w{65,90}_c_c").find(downLinkSite)!!.value
            return sendPostRequestWithForm(
                "https://wwe.lanzous.com/ajaxm.php",
                mapOf("action" to "downprocess", "sign" to sign, "ves" to "1"),
                referer = downLinkSiteUrl
            ).body!!.string()
        }

        fun setPassword(
            loginCookie: String,
            isFile: Boolean,
            id: String,
            pass: String,
            isUse: Boolean
        ): String {
            val para = mapOf(
                "task" to if (isFile) "23" else "16",
                (if (isFile) "file_id" else "folder_id") to id,
                "shows" to if (isUse) "1" else "0",
                "shownames" to pass
            )
            return sendPostRequestWithForm(url, para, loginCookie).body!!.string()

        }

        fun moveFile(loginCookie: String, fileId: String, folderId: String): String {
            val para = mapOf(
                "task" to "20",
                "folder_id" to folderId,
                "file_id" to fileId
            )
            return sendPostRequestWithForm(url, para, loginCookie).body!!.string()
        }

        fun changeDirInfo(loginCookie: String, id: String, name: String, des: String): String {
            val para = mapOf(
                "task" to "4",
                "folder_id" to id,
                "folder_name" to name,
                "folder_description" to des
            )
            return sendPostRequestWithForm(url, para, loginCookie).body!!.string()
        }

        fun changeFileInfo(loginCookie: String, id: String, des: String): String {
            val para = mapOf(
                "task" to "11",
                "file_id" to id,
                "desc" to des
            )
            return sendPostRequestWithForm(url, para, loginCookie).body!!.string()
        }

        fun getRealDownloadLink(url: String): String {
            val req = Request.Builder()
                .url(url)
                .get()
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .build()
            return client.newCall(req).execute().header("location")!!
        }

        fun getDownloadFileSize(url: String): Long {
            val req = Request.Builder()
                .url(url)
                .get()
                .build()
            return client.newCall(req).execute().body!!.contentLength()
        }


        //fun getFileSize(url: String): Long = getDownloadFileSize(getRealDownloadLink(getFileDownloadLink(url)))


    }
}
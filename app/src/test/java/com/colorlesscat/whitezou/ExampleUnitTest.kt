package com.colorlesscat.whitezou

import com.colorlesscat.whitezou.functions.*
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.junit.Test

import org.junit.Assert.*
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        val s="sdfsdfsdf".split(".")
       println(upload(File("/home/null/ip.txt"),"2180158"))

    }
    private fun upload(file: File,id:String): Boolean {

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("task", "1")
            .addFormDataPart("ve","2")
            .addFormDataPart("folder_id_bb_n", id)
            .addFormDataPart(
                "upload_file",
                file.name,
                RequestBodyWithProgress(file.asRequestBody()) {}
            )
            .build()
        val request = Request.Builder()
            .url("https://pc.woozooo.com/fileup.php")
            .header("cookie", "phpdisk_info=UGVXZw1sATkFMgdlDmJRAgdjUFsPZwJgVGYAYAQ1BDRXZF9uVjMAOAc1Vw4MZ1o3BWADNgA6BTYEYQg5VGcEZVBnV20NZgFoBWYHMw5jUToHZVA3D24CZ1QzAGQEYQRkV2dfPFYxAGkHYFcwDF9aMQVgAzAAaQVlBDUIb1RhBDFQYFdl;")
            .post(body)
            .build()
        val res = Task.client.newCall(request).execute().body!!.string()
        println(res)
        return res[res.indexOf("zt") + 4] == '1'
    }
    }

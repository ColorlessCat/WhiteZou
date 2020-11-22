package com.colorlesscat.whitezou

import android.os.Environment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.colorlesscat.whitezou.functions.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.concurrent.thread

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.colorlesscat.whitezou", appContext.packageName)
        println(FileSplitTool.merge(File("/sdcard/WhiteZous/.netease-cloud-music_1.2.1_amd64_ubuntu_20190428.de_dp/netease-cloud-music_1.2.1_amd64_ubuntu_20190428.deb_1.mp3"),File("/sdcard/a.out")){_,_->})



    }
}
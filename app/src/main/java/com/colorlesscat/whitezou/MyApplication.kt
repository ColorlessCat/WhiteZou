package com.colorlesscat.whitezou

import android.app.Application
import com.colorlesscat.whitezou.functions.ObjectSaver

class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        ObjectSaver.appContext=applicationContext
    }
}
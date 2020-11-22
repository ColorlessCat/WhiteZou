package com.colorlesscat.whitezou

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.colorlesscat.whitezou.functions.StoreHelper

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }

    fun onClick(v: View) {
        StoreHelper.saveIsFirst(this, true)
        finish()
    }

    override fun onBackPressed() {
        Toast.makeText(this, "先同意才能走", Toast.LENGTH_SHORT).show()
    }
}
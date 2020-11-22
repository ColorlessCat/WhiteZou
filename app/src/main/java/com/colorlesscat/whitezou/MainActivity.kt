package com.colorlesscat.whitezou

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.colorlesscat.whitezou.functions.ObjectSaver
import com.colorlesscat.whitezou.views.PagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_viewpager.adapter = PagerAdapter(this)
        TabLayoutMediator(main_tab, main_viewpager) { tab, position ->
            tab.text = if (position == 0) "文件" else "任务"
        }.attach()
        main_viewpager.currentItem = 1
        main_viewpager.currentItem = 0
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }

    override fun onBackPressed() {
        //把fragment的这个回调当onBackPressed用，根据Bundle的值判断是否是Main这边调用的
        val bundle = Bundle()
        bundle.putChar("main", 'A')
        val fa1 = supportFragmentManager.fragments[0]
        val fa2 = supportFragmentManager.fragments[1]
        if (fa1.isVisible)
            fa1.onGetLayoutInflater(bundle)
        else
            fa2.onGetLayoutInflater(bundle)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "申请权限失败，软件无法正常运行。", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            val file = File(ObjectSaver.appPath)
            if (!(file.exists() || file.mkdirs())) {
                Toast.makeText(this, "初始化失败。", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}

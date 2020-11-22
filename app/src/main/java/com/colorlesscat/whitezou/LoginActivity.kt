package com.colorlesscat.whitezou

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import com.colorlesscat.whitezou.functions.NetFunctionImpl
import com.colorlesscat.whitezou.functions.StoreHelper
import kotlinx.android.synthetic.main.activity_login.*
import kotlin.concurrent.thread


class LoginActivity : AppCompatActivity() {
    private val netOperation = NetFunctionImpl()
    lateinit var editAccount: AppCompatEditText
    lateinit var editPassword: AppCompatEditText
    lateinit var checkBox: CheckBox
    lateinit var btnLogin: AppCompatButton

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()
        val username = StoreHelper.getUserName(this)
        val password = StoreHelper.getPassword(this)
        if (!(username == null || username == "" || password == null || password == "")) {
            try {
                login(username, password)
            } catch (e: Exception) {
                Toast.makeText(this, "网络错误，检查一下网络链接吧。", Toast.LENGTH_SHORT).show()
            }
        }
        if (!StoreHelper.isFirst(this)) {
            startActivity(Intent(this, AboutActivity::class.java))
        }

    }

    private fun login(username: String, password: String) {
        thread {
            val result = netOperation.login(username, password)
            runOnUiThread {
                if (result) {
                    startActivity(Intent(this, MainActivity::class.java))
                    Toast.makeText(this, "自动登录成功。", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "自动登录失败惹，转为手动登录模式。", Toast.LENGTH_LONG).show()
                    editAccount.setText(username)
                    editPassword.setText(password)
                }
            }
        }
    }

    private fun initView() {
        editAccount = login_edit_account
        editPassword = login_edit_password
        checkBox = login_checkbox
        btnLogin = login_btn_login
    }

    fun onClick(v: View) {
        val acc = editAccount.text.toString()
        val pass = editPassword.text.toString()
        val isRemember = checkBox.isChecked
        thread {
            val result = netOperation.login(acc, pass)
            runOnUiThread {
                if (result) {
                    startActivity(Intent(this, MainActivity::class.java))
                    if (isRemember)
                        StoreHelper.saveAccount(this, acc, pass)
                    finish()
                } else {
                    Toast.makeText(this, "失败惹，检查下输入？", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
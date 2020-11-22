package com.colorlesscat.whitezou

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colorlesscat.whitezou.functions.*
import com.colorlesscat.whitezou.views.FileListAdapter
import com.colorlesscat.whitezou.views.PathViewerAdapter
import com.colorlesscat.whitezou.views.bean.DirPath
import kotlinx.android.synthetic.main.fragment_file.*
import kotlinx.android.synthetic.main.popwindow_filesetting.view.*
import java.io.File
import kotlin.concurrent.thread

class FileListFragment : Fragment() {
    //TODO 实现像oppo自带的文件浏览器一样的可以点击的路径显示　√
    //TODO 做一个好看的加载动画　
    //TODO 实现多选状态下的下载，重命名，移动等操作
    //TODO 修改获取文件列表的逻辑为进入软件时获取所有而非点击时访问api
    //
    lateinit var data: MutableList<LanzousFile>
    lateinit var dirData: MutableList<DirPath>
    lateinit var fileListAdapter: FileListAdapter
    lateinit var dirPathAdapter: PathViewerAdapter
    lateinit var selectList: MutableList<LanzousFile>
    var parentPosition = 0
    private var net = NetFunctionImpl()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_file, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initEvent()
        selectList = emptyList<LanzousFile>().toMutableList()
    }

    private fun initEvent() {
        frag_file_upload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(intent, 0)
        }
        frag_file_download.setOnClickListener {
            if (selectList.size + TaskManager.doingCount > 3) {
                Toast.makeText(context, "超出最大任务数量，少选几个吧", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectList.size == 0)
                return@setOnClickListener
            for (item in selectList) {
                if (item.isFile || item.name.endsWith("_p4rt")) {
                    if (isTaskExists(item))
                        continue
                    TaskManager.addDownloadTask(item)
                }
            }
            Toast.makeText(context, "任务已添加(不包含选中的文件夹)", Toast.LENGTH_SHORT).show()
            cancelSelect()
        }
        frag_file_cut.setOnClickListener {
            Toast.makeText(context, "已剪切，选中「移动到此」移动文件", Toast.LENGTH_SHORT).show()
            cancelSelect()
        }
        frag_file_delete.setOnClickListener {
            if (selectList.size == 0)
                return@setOnClickListener
            thread {
                for (item in selectList) {
                    net.deleteFile(item.id, item.isFile)
                    fileListAdapter.data.remove(item)

                }
                activity!!.runOnUiThread {
                    cancelSelect()
                    Toast.makeText(context, "删除完成", Toast.LENGTH_SHORT).show()
                }
            }
        }
        frag_file_cancel.setOnClickListener {
            cancelSelect()
        }
        frag_file_create_dir.setOnClickListener {
            showCreateDirDialog()
        }

    }

    private fun showCreateDirDialog() {
        val edit = AppCompatEditText(context!!)
        edit.hint = "名字不能超过45个字符哦～"
        AlertDialog.Builder(context!!).setView(edit).setPositiveButton("创建") { _, _ ->

            val name = edit.text.toString()
            if (name == "") {
                Toast.makeText(context, "名字似乎是必须的呢......", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            thread {
                val result = net.addFolder(name, "", dirData.last().id)
                activity!!.runOnUiThread {
                    Toast.makeText(
                        context,
                        "创建${if (result != "") "成功" else "失败"}",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (result != "") {
                        data.add(0, LanzousFile(false, result, name, "", "", ""))
                        fileListAdapter.notifyDataSetChanged()
                    }
                }
            }
        }.setNegativeButton("返回") { a, _ ->
            a.dismiss()
        }.show()
    }

    private fun cancelSelect() {
        fileListAdapter.isSelecting = false
        fileListAdapter.notifyDataSetChanged()
        frag_file_card.visibility = View.INVISIBLE
    }

    private fun initData() {

        thread {
            data = ObjectSaver.net.getFileList().toMutableList()
            activity!!.runOnUiThread {
                frag_file_recycler.layoutManager =
                    LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                frag_dir_path_recycler.layoutManager =
                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                fileListAdapter = FileListAdapter(data, this)
                frag_file_recycler.adapter = fileListAdapter
                dirData = mutableListOf()
                dirData.add(DirPath("我的文件", "-1", data))
                dirPathAdapter = PathViewerAdapter(dirData, this)
                frag_dir_path_recycler.adapter = dirPathAdapter
            }
        }
    }

    fun updateCurrentDir(newDirPosition: Int) {
        fileListAdapter.data = dirData[newDirPosition].data.toMutableList()
        fileListAdapter.notifyDataSetChanged()
        frag_file_recycler.scrollToPosition(parentPosition)
        for (i in newDirPosition + 1..dirData.lastIndex) {
            dirData.removeAt(newDirPosition + 1)
        }
        dirPathAdapter.notifyDataSetChanged()
    }


    fun showSettingWindow(view: View, item: LanzousFile) {

        val pop = PopupWindow(context)
        val contentView =
            LayoutInflater.from(context).inflate(R.layout.popwindow_filesetting, null)
        if (item.isFile || item.name.endsWith("_p4rt")) {
            //不支持重命名文件
            contentView.pop_rename.setTextColor(0xdbdbdb)

            contentView.pop_rename.isClickable = false
        } else {
            //不支持移动文件夹
            contentView.pop_remove.text = "移动到这"

        }
        contentView.pop_share.setOnClickListener {
            pop.dismiss()
            thread {
                val link = net.getFileData(item.id, item.isFile)[0]
                activity!!.runOnUiThread {
                    val cm =
                        context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("null", link))
                    Toast.makeText(context, "已经把分享链接复制到剪切板惹", Toast.LENGTH_SHORT).show()
                }
            }
        }
        contentView.pop_downlaod.setOnClickListener {
            if (TaskManager.doingCount > 3) {
                Toast.makeText(context, "超出任务数量上限。", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!item.isFile && !item.name.endsWith("_p4rt")) {
                Toast.makeText(context, "暂不支持下载文件夹。", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isTaskExists(item)) {
                Toast.makeText(context, "下载任务已存在。", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            TaskManager.addDownloadTask(item)
            Toast.makeText(context, "任务已添加。", Toast.LENGTH_SHORT).show()
            pop.dismiss()
        }
        contentView.pop_change_des.setOnClickListener {
            Toast.makeText(context, "下个版本加这个功能。", Toast.LENGTH_SHORT).show()
        }
        contentView.pop_delete.setOnClickListener {
            thread {
                val b = net.deleteFile(item.id, item.isFile)
                activity!!.runOnUiThread {
                    Toast.makeText(context, "删除${if (b) "成功" else "失败"}", Toast.LENGTH_SHORT)
                        .show()
                    if (b) {
                        data.remove(item)
                        fileListAdapter.data = data
                        fileListAdapter.notifyDataSetChanged()
                    }
                    pop.dismiss()
                }
            }
        }
        contentView.pop_remove.setOnClickListener {
            pop.dismiss()
            if (item.isFile) {
                selectList.clear()
                selectList.add(item)
                Toast.makeText(context, "已剪切，选中「移动到此」移动文件", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectList.isEmpty()) return@setOnClickListener
            thread {
                for (file in selectList) {
                    if (!file.isFile) continue
                    net.removeFile(file.id, item.id)
                    data.remove(file)
                }
                activity!!.runOnUiThread {
                    fileListAdapter.notifyDataSetChanged()
                    Toast.makeText(context, "移动完成！", Toast.LENGTH_SHORT).show()
                }
            }
        }
        contentView.pop_rename.setOnClickListener {
            showRenameDialog(item)
            pop.dismiss()
        }
        pop.setBackgroundDrawable(
            AppCompatResources.getDrawable(
                context!!,
                R.color.colorInvisible
            )
        )
        pop.isOutsideTouchable = true

        pop.contentView = contentView
        //宽度给屏幕宽度的一半 长度给屏幕长度的2.5分之一
        val size = ViewTool.getScreenSize()
        pop.width = size.x / 2
        pop.height = (size.y / 2.5).toInt()
        pop.showAsDropDown(view, -500, 0)
    }

    var timeStamp = 0L
    fun onBackPressed() {
        if (dirData.size > 1)
            updateCurrentDir(dirData.lastIndex - 1)
        else {
            if (System.currentTimeMillis() - timeStamp < 1000)
                activity!!.finish()
            else {
                Toast.makeText(context, "再按一次就真的退出了", Toast.LENGTH_SHORT).show()
                timeStamp = System.currentTimeMillis()
            }
        }
    }


    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        if (savedInstanceState != null && savedInstanceState.getChar("main") == 'A')
            onBackPressed()
        return super.onGetLayoutInflater(savedInstanceState)
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            TaskManager.addUploadTask(
                File(URIHelper.getPath(context, data!!.data!!)),
                dirPathAdapter.data.last().id
            )
        }
    }

    fun showRenameDialog(item: LanzousFile) {
        val edit = AppCompatEditText(context!!)
        edit.setText(item.name)
        edit.hint = "名字不能超过45个字符哦～"
        var des = ""
        thread {
            des = net.getFileData(item.id, item.isFile)[2]
        }
        val dialog = AlertDialog.Builder(context!!)
            .setCancelable(true)
            .setView(edit)
            .setPositiveButton("确认") { _, _ ->
                val text = edit.text.toString()
                if (text == "") {
                    Toast.makeText(context!!, "文件名不可为空。", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (item.isFile || item.name.endsWith("_p4rt")) {
                    Toast.makeText(context!!, "文件不支持重命名操作(不会真的有人开会员吧～)", Toast.LENGTH_SHORT)
                        .show()
                    return@setPositiveButton
                }
                thread {
                    var isSuccess = net.setDirData(item.id, text, des)
                    activity!!.runOnUiThread {
                        Toast.makeText(
                            context!!,
                            "修改${if (isSuccess) "成功" else "失败"}",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (isSuccess) {
                            item.name = text
                            fileListAdapter.notifyDataSetChanged()
                        }

                    }
                }
            }.show()
    }

    private fun isTaskExists(task: LanzousFile): Boolean {
        for (t in TaskManager.taskList)
            if (t.isDown && t.id == task.id)
                return true
        return false
    }

}
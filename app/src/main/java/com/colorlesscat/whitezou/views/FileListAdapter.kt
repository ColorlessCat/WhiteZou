package com.colorlesscat.whitezou.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isInvisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colorlesscat.whitezou.FileListFragment
import com.colorlesscat.whitezou.R
import com.colorlesscat.whitezou.functions.LanzousFile
import com.colorlesscat.whitezou.functions.ObjectSaver
import com.colorlesscat.whitezou.views.bean.DirPath
import kotlinx.android.synthetic.main.fragment_file.*
import kotlinx.android.synthetic.main.item_file.view.*
import kotlin.concurrent.thread

class FileListAdapter(var data: MutableList<LanzousFile>, val fa: FileListFragment) :
    RecyclerView.Adapter<FileListAdapter.ViewHolder>() {
    lateinit var itemView: View
    lateinit var parentData: List<LanzousFile>//上层目录的数据

    var isSelecting = false

    class ViewHolder(
        private val itemView: View,
        val icon: AppCompatImageView = itemView.file_item_icon,
        val name: AppCompatTextView = itemView.item_file_tv_name,
        val date: AppCompatTextView = itemView.item_file_tv_date,
        val size: AppCompatTextView = itemView.item_file_tv_size,
        val count: AppCompatTextView = itemView.item_file_tv_count,
        val lock: AppCompatImageView = itemView.file_item_lock,
        val option: AppCompatImageButton = itemView.file_item_option,
        val checkBox: AppCompatCheckBox = itemView.file_item_checkbox

    ) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        itemView.tag = position
        holder.name.text = item.name
        holder.date.text = item.date
        holder.lock.visibility = if (item.password == "") View.INVISIBLE else View.VISIBLE
        holder.checkBox.isChecked = fa.selectList.contains(item)
        if (!item.isFile) {
            holder.icon.setImageResource(R.mipmap.fol)
            if (item.name.endsWith("_p4rt")) {
                val partName = item.name.substring(0, item.name.lastIndex - 4)
                holder.name.text = partName
                holder.icon.setImageResource(
                    FileTypeHelper.getImgIDByType(
                        partName.split(".").last()
                    )
                )
            }

            holder.count.visibility = View.INVISIBLE
            holder.size.visibility = View.INVISIBLE
        } else {
            holder.count.visibility = View.VISIBLE
            holder.size.visibility = View.VISIBLE
            holder.icon.setImageResource(FileTypeHelper.getImgIDByType(item.name.split(".").last()))
            holder.count.text = "下载${item.downCount}次"
            holder.size.text = item.size
        }
        if (isSelecting) {
            holder.option.visibility = View.INVISIBLE
            holder.checkBox.visibility = View.VISIBLE
        } else {
            holder.option.visibility = View.VISIBLE
            holder.checkBox.visibility = View.INVISIBLE
        }
        holder.option.setOnClickListener {
            val item = data[holder.adapterPosition]
            fa.showSettingWindow(it, item)
        }
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            val item = data[holder.adapterPosition]
            if (isChecked && !fa.selectList.contains(item))
                fa.selectList.add(item)
            else
                fa.selectList.remove(item)

        }
        itemView.setOnClickListener {
            parentData = data
            val item = data[holder.adapterPosition]
            if (!item.isFile&&!item.name.endsWith("_p4rt")) {
                fa.parentPosition=(fa.frag_file_recycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                //fa.parentPosition=holder.adapterPosition
                enterFolder(item)
                return@setOnClickListener
            }
            //Todo 如果是文件就弹出一个详细信息的窗口
        }
        itemView.setOnLongClickListener {
            isSelecting = true
            fa.frag_file_card.visibility = View.VISIBLE
            notifyDataSetChanged()
            false
        }

    }


    private fun enterFolder(item: LanzousFile) {
        thread {
            data = ObjectSaver.net.getFileList(item.id).toMutableList()
            fa.dirData.add(DirPath(item.name, item.id, data))
            fa.activity!!.runOnUiThread {
                fa.dirPathAdapter.notifyDataSetChanged()
                notifyDataSetChanged()
            }
        }
    }
}
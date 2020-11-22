package com.colorlesscat.whitezou.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.colorlesscat.whitezou.FileListFragment
import com.colorlesscat.whitezou.R
import com.colorlesscat.whitezou.views.bean.DirPath
import kotlinx.android.synthetic.main.item_path.view.*

class PathViewerAdapter(var data: MutableList<DirPath>, val fa: FileListFragment) : RecyclerView.Adapter<PathViewerAdapter.ViewHolder>() {
    class ViewHolder(private val itemView: View, val name: AppCompatTextView = itemView.item_path_name) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PathViewerAdapter.ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_path, parent, false))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.name.text = item.name
        holder.name.setOnClickListener {

            if (holder.adapterPosition == data.lastIndex) return@setOnClickListener
            fa.updateCurrentDir(holder.adapterPosition)
        }
    }
}
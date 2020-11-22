package com.colorlesscat.whitezou

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colorlesscat.whitezou.functions.TaskManager
import com.colorlesscat.whitezou.views.TaskListAdapter
import kotlinx.android.synthetic.main.fragment_task.*
import kotlin.concurrent.thread


class TaskListFragment : Fragment() {
    lateinit var adapter: TaskListAdapter
    val handler = Handler { msg ->
        if (!adapter.isTouching)
            adapter.notifyDataSetChanged()
        false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_task, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    fun init() {
        task_recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        adapter = TaskListAdapter()
        task_recycler.adapter = adapter
        TaskManager.init()
        thread {
            while (true) {
                handler.sendEmptyMessage(0)
                Thread.sleep(1000)
            }
        }
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        if (savedInstanceState != null && savedInstanceState.getChar("main") == 'A')
            onBackPressed()
        return super.onGetLayoutInflater(savedInstanceState)
    }

    private var timeStamp = 0L
    private fun onBackPressed() {
        if (System.currentTimeMillis() - timeStamp < 1000)
            activity!!.finish()
        else {
            Toast.makeText(context, "再按一次就真的退出了", Toast.LENGTH_SHORT).show()
            timeStamp = System.currentTimeMillis()
        }
    }

}
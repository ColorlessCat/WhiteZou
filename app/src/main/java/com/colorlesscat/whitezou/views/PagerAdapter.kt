package com.colorlesscat.whitezou.views

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.colorlesscat.whitezou.FileListFragment
import com.colorlesscat.whitezou.TaskListFragment

class PagerAdapter(fa: AppCompatActivity) : FragmentStateAdapter(fa) {
    lateinit var tf: TaskListFragment
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        //为了让TaskListFragment的生命周期方法被调用　这里一开始就初始化两个Fragment
        if (position == 0)
            tf = TaskListFragment()
        return if (position == 0) FileListFragment() else tf
    }


}
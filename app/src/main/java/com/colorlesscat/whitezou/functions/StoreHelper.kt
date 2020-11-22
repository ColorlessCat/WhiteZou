package com.colorlesscat.whitezou.functions

import android.content.Context

class StoreHelper {
    companion object{

        private fun put(context: Context, key:String, value:String){
            val sp=context.getSharedPreferences("data",Context.MODE_PRIVATE)
            val ed=sp.edit()
            ed.putString(key,value)
            ed.apply()
        }
        private fun get(context:Context,key:String):String?{
            val sp=context.getSharedPreferences("data",Context.MODE_PRIVATE)
            return sp.getString(key,"")
        }
        fun saveAccount(context:Context,name:String,pass:String){
            put(context, "name", name)
            put(context, "pass", pass)
        }
        fun getUserName(context: Context)=context.getSharedPreferences("data",Context.MODE_PRIVATE).getString("name","")
        fun getPassword(context: Context)=context.getSharedPreferences("data",Context.MODE_PRIVATE).getString("pass","")
        fun saveIsFirst(context: Context,bool:Boolean){
            put(context, "is_first", bool.toString())
        }
        fun isFirst(context: Context)=context.getSharedPreferences("data",Context.MODE_PRIVATE).getString("is_first","false")=="true"
    }
}
package com.colorlesscat.whitezou.functions


data class LanzousFile(
    val isFile: Boolean,
    val id:String,
    var name: String,
    val size: String,
    val date: String,
    val downCount: String,
    var downURL:String="",
    val password:String=""
)
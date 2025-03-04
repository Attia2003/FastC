package com.example.fastaf.eror

data class ViewEror(
    val message:String?=null,

    val psoActionName:String?=null,
    val posActionClick: onActionClickDialog?=null,
    val negActionName:String?=null,
    val negActionClick: onActionClickDialog?=null,
    val throwable: Throwable?=null,
    val isCancelable:Boolean = true


)

fun interface onActionClickDialog{
    fun onActionclick()
}


package com.example.alarmproject

data class Alarm(var ID: Int, var name:String, var time: String, var day:String,
                 var dayFlag: Boolean, var state:Boolean, var group: Array<Alarm?>?)
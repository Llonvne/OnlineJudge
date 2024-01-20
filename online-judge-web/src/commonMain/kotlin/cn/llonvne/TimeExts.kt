package cn.llonvne

import kotlinx.datetime.LocalDateTime

fun LocalDateTime.ll() =
    "${year}年${monthNumber}月${dayOfMonth}日${hour}时${minute}分${second}秒"
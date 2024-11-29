package cn.llonvne.exts

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime

fun LocalDateTime.Companion.now() =
    java.time.LocalDateTime
        .now()
        .toKotlinLocalDateTime()

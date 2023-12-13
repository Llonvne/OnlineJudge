package cn.llonvne.gojudge.api.task.gpp

import cn.llonvne.gojudge.api.task.Input

data class GppInput(override val code: String, override val stdin: String) : Input


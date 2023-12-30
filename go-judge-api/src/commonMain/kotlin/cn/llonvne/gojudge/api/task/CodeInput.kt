package cn.llonvne.gojudge.api.task

import cn.llonvne.gojudge.api.task.Input

data class CodeInput(override val code: String, override val stdin: String) : Input


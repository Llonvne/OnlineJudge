package cn.llonvne.gojudge.api.task

data class CodeInput(
    override val code: String,
    override val stdin: String,
) : Input

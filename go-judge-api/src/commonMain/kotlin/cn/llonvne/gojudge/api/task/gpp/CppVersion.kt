package cn.llonvne.gojudge.api.task.gpp

enum class CppVersion {
    Cpp98,
    Cpp11,
    Cpp14,
    Cpp17,
    Cpp20,
    Cpp23
}

fun CppVersion.asArg() = "-std=c++" + this.name.substring(3)
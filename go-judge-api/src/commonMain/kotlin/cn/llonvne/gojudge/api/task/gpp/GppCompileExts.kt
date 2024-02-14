package cn.llonvne.gojudge.api.task.gpp

import cn.llonvne.gojudge.api.task.AbstractTask

/**
 * G++ 编译器命令
 */
private fun gppCompileCommand(source: String, output: String, cppVersion: CppVersion): List<String> {
    return listOf("/usr/bin/g++", source, "-o", output, cppVersion.asArg())
}

/**
 * 将传入的文件名转换为 G++编译命令
 */
internal fun buildGppCompileCommand(filenames: AbstractTask.Filenames, cppVersion: CppVersion) =
    gppCompileCommand(
        filenames.source.asString(),
        filenames.compiled.asString(),
        cppVersion
    )
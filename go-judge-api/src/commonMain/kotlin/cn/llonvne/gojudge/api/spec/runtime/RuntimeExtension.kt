package cn.llonvne.gojudge.api.spec.runtime

import cn.llonvne.gojudge.api.task.AbstractTask

fun useGpp(source: String, output: String): List<String> {
    return listOf("/usr/bin/g++", source, "-o", output)
}

fun useGpp(filenames: AbstractTask.Filenames) =
    useGpp(
        filenames.source.asString(),
        filenames.compiled.asString()
    )

fun useStdOutErrForFiles(stdin: String = "", max: Int = 10240) = mutableListOf(
    GoJudgeFile.MemoryFile(
        content = stdin
    ),
    GoJudgeFile.Collector(
        name = "stdout",
        max = max
    ),
    GoJudgeFile.Collector(
        name = "stderr",
        max = max
    )
)

fun useStdOutErrForCopyOut() = mutableListOf("stdout", "stderr")

fun useMemoryCodeCopyIn(sourceFilename: String, content: String) = mutableMapOf(
    sourceFilename to GoJudgeFile.MemoryFile(content)
)

fun useFileIdCopyIn(fileId: String, newName: String) = mapOf(newName to GoJudgeFile.PreparedFile(fileId))

fun Cmd.default() {
    procLimit = 50
    memoryLimit = 104857600
    cpuLimit = 10000000000
}
package cn.llonvne.gojudge.api.spec.runtime

/**
 * 传入 stdin,
 * 通过收集器收集 stdout 和 stderr
 */
internal fun useStdOutErrForFiles(stdin: String = "", max: Int = 10240) = mutableListOf(
    GoJudgeFile.MemoryFile(
        content = stdin
    ), GoJudgeFile.Collector(
        name = "stdout", max = max
    ), GoJudgeFile.Collector(
        name = "stderr", max = max
    )
)

/**
 * 导出 stdout,stderr
 */
internal fun useStdOutErrForCopyOut() = mutableListOf("stdout", "stderr")

/**
 * 将代码[content]作为内存文件以[sourceFilename]作为文件名导入
 */
internal fun useMemoryCodeCopyIn(sourceFilename: String, content: String) = mutableMapOf(
    sourceFilename to GoJudgeFile.MemoryFile(content)
)

/**
 * 将 [fileId] 导入到以 [newName] 作为文件名
 */
internal fun useFileIdCopyIn(fileId: String, newName: String) = mapOf(newName to GoJudgeFile.PreparedFile(fileId))

val useUsrBinEnv = listOf("PATH=/usr/bin:/bin")

/**
 * cmd 的默认设置
 */
internal fun Cmd.default() {
    procLimit = 50
    memoryLimit = 104857600
    cpuLimit = 10000000000
}

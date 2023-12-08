package cn.llonvne.gojudge.api.ext

import cn.llonvne.gojudge.api.*


internal fun request(
    requestId: String? = null,
    pipeMap: List<PipeMap>? = null,
    cmdListBuilder: CmdListBuilder.() -> Unit
): RequestType.Request {

    val cmd = CmdListBuilder()

    cmd.cmdListBuilder()

    return RequestType.Request(requestId, cmd = cmd.build(), pipeMapping = pipeMap)
}

internal suspend fun GoJudgeService.run(
    requestId: String? = null,
    pipeMap: List<PipeMap>? = null,
    cmdListBuilder: CmdListBuilder.() -> Unit
) = run(request(requestId, pipeMap, cmdListBuilder))

internal class CmdListBuilder {
    private val cmds = mutableListOf<Cmd>()

    fun cmd(builder: Cmd.() -> Unit) {
        val cmd = Cmd(emptyList())
        cmd.default()
        cmd.builder()
        cmds.add(cmd)
    }

    internal fun build(): List<Cmd> {
        return cmds
    }
}

internal fun useGpp(source: String, output: String): List<String> {
    return listOf("/usr/bin/g++", source, "-o", output)
}

internal val useUsrBinEnv = listOf("PATH=/usr/bin:/bin")

internal fun useStdOutErrForFiles(stdin: String = "", max: Int = 10240) = mutableListOf(
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

internal fun useStdOutErrForCopyOut() = mutableListOf("stdout", "stderr")

internal fun useMemoryCodeCopyIn(sourceFilename: String, content: String) = mutableMapOf(
    sourceFilename to GoJudgeFile.MemoryFile(content)
)

internal fun useFileIdCopyIn(fileId: String, newName: String) = mapOf(newName to GoJudgeFile.PreparedFile(fileId))

private fun Cmd.default() {
    procLimit = 50
    memoryLimit = 104857600
    cpuLimit = 10000000000
}
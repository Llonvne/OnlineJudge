package cn.llonvne.gojudge.api

fun request(
    requestId: String? = null,
    pipeMap: List<PipeMap>? = null,
    cmdListBuilder: CmdListBuilder.() -> Unit
): RequestType.Request {

    val cmd = CmdListBuilder()

    cmd.cmdListBuilder()

    return RequestType.Request(requestId, cmd = cmd.build(), pipeMapping = pipeMap)
}

suspend fun GoJudgeService.run(
    requestId: String? = null,
    pipeMap: List<PipeMap>? = null,
    cmdListBuilder: CmdListBuilder.() -> Unit
) = run(request(requestId, pipeMap, cmdListBuilder))

class CmdListBuilder {
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

fun useGpp(source: String, output: String): List<String> {
    return listOf("/usr/bin/g++", source, "-o", output)
}

val useUsrBinEnv = listOf("PATH=/usr/bin:/bin")

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

private fun Cmd.default() {
    procLimit = 50
    memoryLimit = 104857600
    cpuLimit = 10000000000
}
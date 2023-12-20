package cn.llonvne.gojudge.services.runtime

import cn.llonvne.gojudge.api.spec.runtime.Cmd
import cn.llonvne.gojudge.api.spec.runtime.PipeMap
import cn.llonvne.gojudge.api.spec.runtime.RequestType
import cn.llonvne.gojudge.api.spec.runtime.default
import cn.llonvne.gojudge.internal.GoJudgeClient
import cn.llonvne.gojudge.internal.run

class CmdListBuilder {
    private val commands = mutableListOf<Cmd>()

    internal fun build(): List<Cmd> {
        return commands
    }

    fun add(cmd: Cmd) {
        this.commands.add(cmd)
    }
}

fun request(
    requestId: String? = null,
    pipeMap: List<PipeMap>? = null,
    cmdListBuilder: CmdListBuilder.() -> Unit
): RequestType.Request {

    val cmd = CmdListBuilder()

    cmd.cmdListBuilder()

    return RequestType.Request(requestId, cmd = cmd.build(), pipeMapping = pipeMap)
}

fun cmd(build: Cmd.() -> Unit): Cmd {
    val cmd = Cmd(emptyList())
    cmd.default()
    cmd.build()
    return cmd
}

suspend fun GoJudgeClient.run(
    requestId: String? = null,
    pipeMap: List<PipeMap>? = null,
    cmdListBuilder: CmdListBuilder.() -> Unit
) = run(request(requestId, pipeMap, cmdListBuilder))


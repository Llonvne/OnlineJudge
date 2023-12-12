package cn.llonvne.gojudge.services.runtime

import cn.llonvne.gojudge.api.spec.Cmd
import cn.llonvne.gojudge.api.spec.GoJudgeFile
import cn.llonvne.gojudge.api.spec.PipeMap
import cn.llonvne.gojudge.api.spec.RequestType
import cn.llonvne.gojudge.exposed.RuntimeService
import cn.llonvne.gojudge.exposed.run

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

fun request(
    requestId: String? = null,
    pipeMap: List<PipeMap>? = null,
    cmdListBuilder: CmdListBuilder.() -> Unit
): RequestType.Request {

    val cmd = CmdListBuilder()

    cmd.cmdListBuilder()

    return RequestType.Request(requestId, cmd = cmd.build(), pipeMapping = pipeMap)
}

suspend fun RuntimeService.run(
    requestId: String? = null,
    pipeMap: List<PipeMap>? = null,
    cmdListBuilder: CmdListBuilder.() -> Unit
) = run(request(requestId, pipeMap, cmdListBuilder))


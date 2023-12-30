package cn.llonvne.gojudge.docker

import arrow.fx.coroutines.Resource
import cn.llonvne.gojudge.api.spec.bootstrap.GoJudgeEnvSpec
import org.testcontainers.containers.Container


class GoJudgeResolver(private val spec: GoJudgeEnvSpec) {
    fun resolve(): Resource<ContainerWrapper> {
        return configureGoJudgeContainer(spec = spec)
    }
}

fun ContainerWrapper.toJudgeContext() = object : JudgeContext {
    override suspend fun exec(command: String): Container.ExecResult {
        return this@toJudgeContext.exec(command)
    }
}
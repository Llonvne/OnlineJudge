package cn.llonvne.gojudge.docker

import arrow.fx.coroutines.Resource
import cn.llonvne.gojudge.api.spec.GoJudgeEnvSpec


class GoJudgeResolver(private val spec: GoJudgeEnvSpec) {
    fun resolve(): Resource<ContainerWrapper> {
        return configureGoJudgeContainer(spec = spec)
    }
}
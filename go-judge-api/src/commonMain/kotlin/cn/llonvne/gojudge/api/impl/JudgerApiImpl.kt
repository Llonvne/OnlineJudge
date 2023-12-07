package cn.llonvne.gojudge.api.impl

import cn.llonvne.gojudge.api.JudgerApi
import io.ktor.resources.*

@Resource("/")
internal class JudgerApiImpl : JudgerApi {
    override fun getJudgerVersion() {
        TODO("Not yet implemented")
    }
}
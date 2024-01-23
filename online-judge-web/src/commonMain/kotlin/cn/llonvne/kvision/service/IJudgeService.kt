package cn.llonvne.kvision.service

import cn.llonvne.gojudge.api.SupportLanguages
import cn.llonvne.gojudge.api.task.Output
import io.kvision.annotations.KVService

@KVService
interface IJudgeService {
    suspend fun judge(languages: SupportLanguages, stdin: String, code: String): Output
}
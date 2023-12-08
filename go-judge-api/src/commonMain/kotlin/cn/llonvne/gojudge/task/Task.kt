package cn.llonvne.gojudge.task

import cn.llonvne.gojudge.api.*
import cn.llonvne.gojudge.api.ext.request
import cn.llonvne.gojudge.api.ext.useFileIdCopyIn
import cn.llonvne.gojudge.api.ext.useGpp
import cn.llonvne.gojudge.api.ext.useMemoryCodeCopyIn
import cn.llonvne.gojudge.api.ext.useStdOutErrForCopyOut
import cn.llonvne.gojudge.api.ext.useStdOutErrForFiles
import cn.llonvne.gojudge.api.ext.useUsrBinEnv
import java.util.UUID

interface Task<I, R> {
    suspend fun run(input: I, service: GoJudgeService): R
}




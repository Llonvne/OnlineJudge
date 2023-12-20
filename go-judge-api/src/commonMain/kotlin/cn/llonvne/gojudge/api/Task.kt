package cn.llonvne.gojudge.api

import cn.llonvne.gojudge.internal.GoJudgeClient

interface Task<I, R> {
    suspend fun run(input: I, service: GoJudgeClient): R
}




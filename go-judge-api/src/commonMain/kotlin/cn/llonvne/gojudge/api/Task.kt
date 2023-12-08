package cn.llonvne.gojudge.api

interface Task<I, R> {
    suspend fun run(input: I, service: GoJudgeService): R
}




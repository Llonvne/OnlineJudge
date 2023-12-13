package cn.llonvne.gojudge.api

import cn.llonvne.gojudge.exposed.RuntimeService

interface Task<I, R> {
    suspend fun run(input: I, service: RuntimeService): R
}




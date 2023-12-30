package cn.llonvne.gojudge.docker

import org.testcontainers.containers.Container.ExecResult

interface JudgeContext {
    suspend fun exec(command: String): ExecResult
}
package cn.llonvne.gojudge.docker

import org.testcontainers.containers.Container.ExecResult

/**
 * 在Docker虚拟机中执行代码
 *
 * PS:由于Docker为阻塞式API，所有像Docker的请求都会被切换到一个独立的线程，上层无需手动管理
 */
interface JudgeContext {
    suspend fun exec(command: String): ExecResult
}

/**
 * 将 [JudgeContext] 作为上下文接收器传入
 */
operator fun JudgeContext.invoke(
    operation: context(JudgeContext)
    () -> Unit,
) {
    operation(this)
}

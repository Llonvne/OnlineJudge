package cn.llonvne.entity.problem.context

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Problem(
    // 题目 ID
    val problemId: Int? = null,
    // 作者 ID
    val authorId: Int,
    val ownerId: Int,
    // 题目名字
    val problemName: String,
    // 题目描述
    val problemDescription: String,
    // 时间限制
    val timeLimit: Long,
    // 内存限制
    val memoryLimit: Long,
    val visibility: ProblemVisibility,
    val type: ProblemType,
    /**
     * [ProblemContext] 序列化后的类型
     */
    val contextJson: String,
    // --- 数据库信息区 ---//
    val version: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    suspend fun <R> onIdNotNull(
        onNull: R,
        action: suspend (id: Int, problem: Problem) -> R,
    ): R {
        if (problemId != null) {
            return action(problemId, this)
        }
        return onNull
    }

    val context = Json.decodeFromString<ProblemContext>(contextJson)
}

package cn.llonvne.entity.problem.context

import kotlinx.serialization.Serializable

/**
 * 题目内容
 */
@Serializable
data class ProblemContext(val problemCtxId: Int? = null, val context: String) {
    companion object
}


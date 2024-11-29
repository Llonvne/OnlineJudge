package cn.llonvne.entity.problem.context

import kotlinx.serialization.Serializable

/**
 * 题目内容
 */
@Serializable
data class ProblemContext(
    val overall: String,
    val inputDescription: String,
    val outputDescription: String,
    val hint: String,
    val testCases: ProblemTestCases,
)

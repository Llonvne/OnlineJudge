package cn.llonvne.entity.contest

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class ContestContext(
    val problems: List<ContestProblem>
) {
    @Serializable
    data class ContestProblem(
        val problemId: Int,
        val weight: Int,
        val alias: String
    )

    fun json() = contestContextJson.encodeToString(this)

    companion object {
        fun empty() = ContestContext(problems = listOf())
    }
}
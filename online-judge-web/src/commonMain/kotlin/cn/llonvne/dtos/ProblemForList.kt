package cn.llonvne.dtos

import cn.llonvne.entity.Author
import cn.llonvne.entity.problem.ProblemTag
import cn.llonvne.entity.problem.context.Problem
import cn.llonvne.entity.types.ProblemStatus
import kotlinx.serialization.Serializable

@Serializable
data class ProblemForList(
    val problem: Problem,
    val problemId: Int,
    val author: Author,
    val status: ProblemStatus,
    val tags: List<ProblemTag>
)


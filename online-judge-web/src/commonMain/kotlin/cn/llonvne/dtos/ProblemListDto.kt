package cn.llonvne.dtos

import cn.llonvne.entity.Author
import cn.llonvne.entity.problem.Problem
import cn.llonvne.entity.problem.ProblemTag
import cn.llonvne.entity.types.ProblemStatus
import kotlinx.serialization.Serializable

@Serializable
data class ProblemListDto(
    val problem: Problem,
    val author: Author,
    val status: ProblemStatus,
    val tags: List<ProblemTag>
)

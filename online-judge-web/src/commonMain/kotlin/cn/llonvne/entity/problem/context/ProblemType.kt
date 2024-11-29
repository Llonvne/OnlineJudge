package cn.llonvne.entity.problem.context

import kotlinx.serialization.Serializable

@Serializable
enum class ProblemType {
    Individual,
    Group,
}

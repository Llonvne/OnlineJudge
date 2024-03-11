package cn.llonvne.entity.problem

import kotlinx.serialization.Serializable

@Serializable
enum class ProblemListShowType {
    All,
    Accepted,
    Attempted,
    Favorite
}
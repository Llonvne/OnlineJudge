package cn.llonvne.entity.types

import kotlinx.serialization.Serializable

@Serializable
enum class ProblemStatus {
    Accepted,
    WrongAnswer,
    NotLogin,
    NotBegin,
}

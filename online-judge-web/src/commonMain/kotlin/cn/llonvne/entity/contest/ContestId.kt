package cn.llonvne.entity.contest

import kotlinx.serialization.Serializable

@Serializable
sealed interface ContestId

@Serializable
class IntId(
    val id: Int,
) : ContestId

@Serializable
class HashId(
    val hash: String,
) : ContestId

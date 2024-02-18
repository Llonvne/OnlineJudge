package cn.llonvne.entity.group

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val groupId: Int? = null,
    val groupName: String,
    val groupShortName: String,
    val groupHash: String,
    val version: Int? = null,
    val visibility: GroupVisibility,
    val type: GroupType,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)





